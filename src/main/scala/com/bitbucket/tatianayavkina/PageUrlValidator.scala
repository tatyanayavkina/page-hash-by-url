package com.bitbucket.tatianayavkina

import java.net.{URI, URISyntaxException}

object PageUrlValidator {

  def isValid(url: String): Boolean = {
    try {
      new URI(url)
      true
    } catch {
      case e: URISyntaxException => false
    }
  }
}
