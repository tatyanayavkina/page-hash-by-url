package com.bitbucket.tatianayavkina

import java.security.MessageDigest

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.io.Source

object PageHashRunner {

  def getPageHash(pageUrl: String)(implicit executionContext: ExecutionContext): Future[PageHash] = {
    Future {
      val content = Source.fromURL(pageUrl).mkString
      val hash = calculateHash(content)
      PageHash(pageUrl, hash)
    }
  }

  private def calculateHash(content: String): String = {
    MessageDigest.getInstance("MD5").digest(content.getBytes).map(0xFF & _).map { "%02x".format(_) }.foldLeft("") {_ + _}
  }
}
