package com.bitbucket.tatianayavkina.model

sealed trait HashCalculatingResult
final case class PageHash(url: String, hash: String) extends HashCalculatingResult
final case class PageHashError(url: String, error: String) extends HashCalculatingResult

