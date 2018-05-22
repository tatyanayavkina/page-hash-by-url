package com.bitbucket.tatianayavkina.service

import java.io.InputStream
import java.security.MessageDigest

import com.bitbucket.tatianayavkina.UrlStreamProvider
import com.bitbucket.tatianayavkina.model.{HashCalculatingResult, PageHash, PageHashError}
import com.typesafe.scalalogging.LazyLogging
import javax.xml.bind.DatatypeConverter

import scala.concurrent.{ExecutionContext, Future}

object PageHashRunner extends LazyLogging {

  val bufferSize = 4096

  def getPageHash(pageUrl: String)(implicit executionContext: ExecutionContext): Future[HashCalculatingResult] = {
    Future {
      var stream: InputStream = null
      try {
        stream = UrlStreamProvider.getInputStream(pageUrl)
        val hashCalculator: MessageDigest = MessageDigest.getInstance("MD5")
        val hash = calculateHash(stream, hashCalculator)
        PageHash(pageUrl, hash)
      } catch {
        case e: Exception => PageHashError(pageUrl, s"${e.getClass.getSimpleName}:${e.getMessage}")
      } finally {
        if (stream != null) {
          stream.close()
        }
      }
    }
  }

  private def calculateHash(inputStream: InputStream, calculator: MessageDigest): String = {
    val buffer = new Array[Byte](bufferSize)
    var writtenBytes = 0
    while ({writtenBytes >= 0}) {
      calculator.update(buffer, 0, writtenBytes)
      writtenBytes = inputStream.read(buffer)
    }

    DatatypeConverter.printHexBinary(calculator.digest())
  }

}
