package com.bitbucket.tatianayavkina

import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object PageHashByUrlApp extends App {

  if (args.length == 0) {
    Console.err.println("Enter file with url list")
  }

  Future.sequence(Source.fromFile(args(0))
      .getLines()
      .toList
      .filter(url => PageUrlValidator.isValid(url))
      .map(PageHashRunner.getPageHash))
  .onComplete {
    case Success(result) => Console.out.println(result)
    case Failure(ex) => throw ex
  }
}
