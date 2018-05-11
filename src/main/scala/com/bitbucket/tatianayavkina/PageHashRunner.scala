package com.bitbucket.tatianayavkina

import java.security.MessageDigest

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

object PageHashRunner {

  def getPageHash(pageUrl: String): Future[PageHash] = {
    Future {
      val content = Source.fromURL(pageUrl).mkString
      val hash = calculateHash(content)
      Console.out.print("hash=", hash)
      PageHash(pageUrl, hash)
    }
  }

  private def calculateHash(content: String): String = {
    MessageDigest.getInstance("MD5").digest(content.getBytes).map(0xFF & _).map { "%02x".format(_) }.foldLeft("") {_ + _}
  }
}
