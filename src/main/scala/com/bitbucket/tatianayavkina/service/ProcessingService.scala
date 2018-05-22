package com.bitbucket.tatianayavkina.service

import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicLong

import akka.NotUsed
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString
import com.bitbucket.tatianayavkina.model.HashCalculatingResult

import scala.concurrent.{ExecutionContext, Future}

object ProcessingService {

  def buildAndRun(sourceFile: String, resultFile: String, processedTaskCounter: AtomicLong)(implicit actorMaterializer: ActorMaterializer, executionContext: ExecutionContext) =
    getPipelineSource(sourceFile)
      .via(getParseFlow)
      .via(getProcessedCountFlow(processedTaskCounter))
      .runWith(getPipeOut("result.txt"))

  private def getPipelineSource(sourceFile: String): Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(Paths.get(sourceFile))
      .via(Framing.delimiter(ByteString(System.lineSeparator), 1024, true))

  private def getParseFlow(implicit executionContext: ExecutionContext): Flow[ByteString, HashCalculatingResult, NotUsed] =
    Flow[ByteString]
      .map(_.utf8String)
      .filter(UrlValidatorService.isValid)
      .mapAsync(4)(PageHashRunner.getPageHash)

  private def getProcessedCountFlow(counter: AtomicLong): Flow[HashCalculatingResult, HashCalculatingResult, NotUsed] =
    Flow[HashCalculatingResult]
      .map(result => {counter.incrementAndGet(); result})

  private def getPipeOut(resultFile: String): Sink[HashCalculatingResult, Future[IOResult]] = Flow[HashCalculatingResult]
    .map(_.toString)
    .map(ByteString(_))
    .toMat(FileIO.toPath(Paths.get(resultFile)))(Keep.right)
}
