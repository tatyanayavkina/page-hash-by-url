package com.bitbucket.tatianayavkina

import scala.concurrent.Await
import scala.concurrent.duration._
import com.typesafe.scalalogging.LazyLogging
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import com.bitbucket.tatianayavkina.model.{HashCalculatingResult, PageHash, PageHashError}

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

  val hashingFuture =
    FileIO.fromPath(Paths.get(args(0)))
      .via(Framing.delimiter(ByteString(System.lineSeparator), 1024, true))
      .map(_.utf8String)
      .filter(PageUrlValidator.isValid)
      .mapAsync(4)(PageHashRunner.getPageHash)
      .runWith(Sink.seq)

  val hashingBlockingResult = Await.result(hashingFuture, 20.seconds)
  HashFileWriter.addToFile(processResults(hashingBlockingResult))

  sys.exit(0)

  private def processResults(hashingBlockingResult: Seq[HashCalculatingResult]): String = {
    val stringBuilder: StringBuilder = new StringBuilder
    for(result <- hashingBlockingResult) {
      result match {
        case PageHash(url, hash) => stringBuilder.append(s"$url, $hash\n")
        case PageHashError(url, error) => stringBuilder.append(s"$url, $error\n")
      }
    }

    stringBuilder.toString()
  }
}
