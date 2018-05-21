package com.bitbucket.tatianayavkina

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import com.typesafe.scalalogging.LazyLogging
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicLong

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl._
import akka.util.ByteString
import com.bitbucket.tatianayavkina.model.HashCalculatingResult

object PageHashByUrlApp extends App with LazyLogging {

  if (args.length == 0) {
    logger.error("Enter file with url list")
    sys.exit(-1)
  }

  implicit val actorSystem = ActorSystem("system")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  val processedTaskCount: AtomicLong = new AtomicLong()

  sys.addShutdownHook {
    actorSystem.terminate()
    Await.ready(actorSystem.whenTerminated, 30.seconds)
  }

  def getPipelineSource: Source[ByteString, Future[IOResult]] = {
    FileIO.fromPath(Paths.get(args(0)))
      .via(Framing.delimiter(ByteString(System.lineSeparator), 1024, true))
  }

  def getParseFlow: Flow[ByteString, HashCalculatingResult, NotUsed] = {
    Flow[ByteString]
      .map(_.utf8String)
      .filter(PageUrlValidator.isValid)
      .mapAsync(4)(PageHashRunner.getPageHash)
  }

  def buildAndRun() = {
    getPipelineSource
      .via(getParseFlow)
      .map(_.toString)
      .map(ByteString(_))
      .alsoToMat(FileIO.toPath(Paths.get("result.txt")))(Keep.right)
      .runWith(Sink.foreach((_) => processedTaskCount.incrementAndGet))
  }

  def getTaskCount(fileName: String): Long = {
    Await.result(FileIO.fromPath(Paths.get(args(0)))
        .via(Framing.delimiter(ByteString(System.lineSeparator), 1024, true))
        .map(_.utf8String)
        .filter(PageUrlValidator.isValid)
        .runWith(Sink.fold[Long, String](0)((acc, _) => acc + 1)),
      10.seconds)
  }

  def waitTasks(): Unit = {
    while (processedTaskCount.get() < taskCount) {
      logger.info("Processed {}/{} tasks", processedTaskCount.get(), taskCount)
      Thread.sleep(500)
    }
  }

  val taskCount = getTaskCount(args(0))
  buildAndRun()
  waitTasks()

  logger.info("All tasks processed")
  sys.exit(0)
}
