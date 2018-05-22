package com.bitbucket.tatianayavkina

import scala.concurrent.Await
import scala.concurrent.duration._
import com.typesafe.scalalogging.LazyLogging
import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.bitbucket.tatianayavkina.service._

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

  val sourceFile: String = args(0)
  val resultFile: String = "result.txt"
  val processedTaskCounter: AtomicLong = new AtomicLong()
  val taskCount = Await.result(TaskCounter.countTasks(sourceFile), 10.seconds)

  ProcessingService.buildAndRun(sourceFile, resultFile, processedTaskCounter)
  Waiter.wait(processedTaskCounter, taskCount, 500)

  logger.info("All tasks processed")
  sys.exit(0)
}
