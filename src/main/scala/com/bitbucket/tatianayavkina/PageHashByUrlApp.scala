package com.bitbucket.tatianayavkina

import scala.concurrent.Future
import scala.io.Source

object PageHashByUrlApp extends App {

  val urlFilePath : String = args.apply(0)

  val result: Array[Future[String]] = Source.fromFile(urlFilePath)
    .mkString
    .split("\r\n")
    .filterNot(PageUrlValidator.isValid)
    .map(PageHashRunner.getPageHash)


}
