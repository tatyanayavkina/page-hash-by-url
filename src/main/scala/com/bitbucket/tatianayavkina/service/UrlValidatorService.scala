package com.bitbucket.tatianayavkina.service

import java.net.{URI, URISyntaxException}

object UrlValidatorService {

  def isValid(url: String): Boolean = {
    try {
      new URI(url)
      true
    } catch {
      case e: URISyntaxException => false
    }
  }
}
