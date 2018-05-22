package com.bitbucket.tatianayavkina.service

import java.nio.file.Paths

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

object TaskCounter {

  def countTasks(fileName: String)(implicit actorMaterializer: ActorMaterializer, executionContext: ExecutionContext): Future[Long] = {
    FileIO.fromPath(Paths.get(fileName))
      .via(Framing.delimiter(ByteString(System.lineSeparator), 1024, true))
      .map(_.utf8String)
      .filter(UrlValidatorService.isValid)
      .runWith(Sink.fold[Long, String](0)((acc, _) => acc + 1))
  }
}
