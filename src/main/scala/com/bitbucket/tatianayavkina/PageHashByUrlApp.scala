package com.bitbucket.tatianayavkina

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import com.typesafe.scalalogging.LazyLogging
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer}
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

  val hashingFuture : Future[Seq[HashCalculatingResult]] =
    FileIO.fromPath(Paths.get(args(0)))
      .via(Framing.delimiter(ByteString(System.lineSeparator), 1024, true))
      .map(_.utf8String)
      .filter(PageUrlValidator.isValid)
      .mapAsync(4)(PageHashRunner.getPageHash)
      .runWith(Sink.seq)

  val hashingBlockingResult : Seq[HashCalculatingResult] = Await.result(hashingFuture, 20.seconds)

  hashingBlockingResult.runWith(Flow[String]
    .map(_.toString)
    .map(ByteString(_))
    .toMat(FileIO.toPath(Paths.get("result.txt"))))

  sys.exit(0)
}
