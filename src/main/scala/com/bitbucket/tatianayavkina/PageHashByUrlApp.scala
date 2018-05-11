package com.bitbucket.tatianayavkina

import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success}

object PageHashByUrlApp extends App {

  val urlFilePath : String = args.apply(0)

  Future.sequence(Source.fromFile(urlFilePath)
    .mkString
    .split("\r\n")
    .filter(url => !PageUrlValidator.isValid(url))
    .map(PageHashRunner.getPageHash))
  .map(_.flatten)
  .onComplete {
    case Success(result) =>
    case Failure(ex) => throw ex
  }
}
