package com.bitbucket.tatianayavkina

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import com.typesafe.scalalogging.LazyLogging
import java.nio.file.Paths

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

  def getPipeOut: Sink[HashCalculatingResult, Future[IOResult]] = Flow[HashCalculatingResult]
      .map(_.toString)
      .map(ByteString(_))
      .toMat(FileIO.toPath(Paths.get("result.txt")))(Keep.right)

  def buildAndRun: Future[IOResult] = {
    getPipelineSource
      .via(getParseFlow)
      .runWith(getPipeOut)
  }

  Await.result(buildAndRun, 40.seconds)

  sys.exit(0)
}
