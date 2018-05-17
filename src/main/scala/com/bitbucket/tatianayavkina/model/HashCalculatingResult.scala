package com.bitbucket.tatianayavkina.model

sealed trait HashCalculatingResult
final case class PageHash(url: String, hash: String) extends HashCalculatingResult {
  override def toString: String = {
    s"$url, $hash\n"
  }
}
final case class PageHashError(url: String, error: String) extends HashCalculatingResult {
  override def toString: String = {
    s"$url, $error\n"
  }
}

