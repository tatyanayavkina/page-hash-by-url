package com.bitbucket.tatianayavkina

import java.io.InputStream
import java.security.MessageDigest

import com.typesafe.scalalogging.LazyLogging
import javax.xml.bind.DatatypeConverter

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object PageHashRunner extends LazyLogging {

  val bufferSize = 4096

  def getPageHash(pageUrl: String)(implicit executionContext: ExecutionContext): Future[PageHash] = {
    Future {
      val stream: InputStream = UrlStreamProvider.getInputStream(pageUrl)
      try {
        val hashCalculator: MessageDigest = MessageDigest.getInstance("MD5")
        val hash = calculateHash(stream, hashCalculator)
        PageHash(pageUrl, hash)
      } catch {
        case e: Exception => PageHash(pageUrl, "")
      } finally {
        stream.close()
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
