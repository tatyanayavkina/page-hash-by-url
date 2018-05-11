package com.bitbucket.tatianayavkina

import scala.concurrent.Await
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.util.Try
import com.typesafe.scalalogging.LazyLogging
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorAttributes
import akka.stream.ActorMaterializer
import akka.stream.Supervision
import akka.stream.scaladsl._
import akka.util.ByteString

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

//  val hashingFuture = Future.sequence(
//    Source.fromFile(args(0))(Codec.UTF8)
//      .getLines()
//      .toList
//      .filter(PageUrlValidator.isValid)
//      .map(PageHashRunner.getPageHash)
//  )

  val hashingFuture =
    FileIO.fromPath(Paths.get(args(0)))
      .via(Framing.delimiter(ByteString(System.lineSeparator), 1024, true))
      .map(_.utf8String)
      .filter(PageUrlValidator.isValid)
      .mapAsync(4)(PageHashRunner.getPageHash)
      .withAttributes(ActorAttributes.supervisionStrategy({ exception =>
        logger.warn(s"An exception occurred in hashing flow, dropping a url and continue", exception)
        Supervision.Resume
      }))
      .runWith(Sink.seq)

  val hashingBlockingResult = Try(Await.result(hashingFuture, 20.seconds))
  hashingBlockingResult match {
    case Success(result) => logger.info(s"result:\n${result.mkString("\n")}\n")
    case Failure(ex) => logger.error(s"A fatal exception during hashing process")
  }

  sys.exit(0)
}
