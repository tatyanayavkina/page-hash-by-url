package com.bitbucket.tatianayavkina

import java.io.FileWriter

object HashFileWriter {

  def addToFile(line: String): Unit = {
    val fileWriter = new FileWriter("result.txt")
    try {
      fileWriter.write(line)
    } finally {
      fileWriter.close()
    }
  }
}
