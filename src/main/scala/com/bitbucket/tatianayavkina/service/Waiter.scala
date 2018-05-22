package com.bitbucket.tatianayavkina.service

import java.util.concurrent.atomic.AtomicLong

import com.typesafe.scalalogging.LazyLogging

object Waiter extends LazyLogging {

  def wait(processedTaskCount: AtomicLong, taskCount: Long, pauseMs: Int): Unit = {
    while (processedTaskCount.get() < taskCount) {
      logger.info(s"Processed ${processedTaskCount.get()}/$taskCount tasks")
      Thread.sleep(pauseMs)
    }
  }
}
