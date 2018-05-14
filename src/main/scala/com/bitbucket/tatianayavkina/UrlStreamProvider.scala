package com.bitbucket.tatianayavkina

import java.io.InputStream
import java.net.{URL, URLConnection}


object UrlStreamProvider {

  def getInputStream(urlString: String): InputStream = {
    val connection: URLConnection = getConnection(urlString)
    connection.getInputStream
  }

  private def getConnection(urlString: String): URLConnection = {
    val url: URL = new URL(urlString)
    val connection: URLConnection  = url.openConnection
    connection.setReadTimeout(2000)
    connection.setConnectTimeout(4000)
    connection.setAllowUserInteraction(false)
    connection
  }
}
