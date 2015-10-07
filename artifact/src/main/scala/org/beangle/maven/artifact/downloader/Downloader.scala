package org.beangle.maven.artifact.downloader

import java.util.concurrent.atomic.AtomicLong

object Downloader {
  class Status(val total: Long) {
    val count = new AtomicLong(0)
  }
}

trait Downloader {

  def url: String

  def start(): Unit

  def downloaded: Long

  def contentLength: Long

  def name: String
}
