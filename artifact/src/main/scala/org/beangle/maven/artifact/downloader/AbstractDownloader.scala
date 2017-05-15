/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.maven.artifact.downloader

import java.io.{ Closeable, File, IOException }
import java.net.{ HttpURLConnection, URL }
import java.net.HttpURLConnection._

abstract class AbstractDownloader(val name: String, val url: String, protected val location: String) extends Downloader {
  protected var status: Downloader.Status = null

  def contentLength: Long = {
    if ((null == status)) 0 else status.total
  }

  def downloaded: Long = {
    if ((null == status)) 0 else status.count.get
  }

  def start(): Unit = {
    val file = new File(location)
    if (file.exists()) return

    val resourceURL = new URL(url)
    val urlStatus = touchUrl(resourceURL)
    if (null == urlStatus._2) {
      println("\r" + toString(urlStatus._1) + " " + url)
      return
    }
    file.getParentFile.mkdirs()
    downloading(urlStatus._2)
  }

  protected def downloading(resource: URL): Unit

  private def toString(httpCode: Int): String = {
    httpCode match {
      case HTTP_OK           => "OK"
      case HTTP_FORBIDDEN    => "Access denied!"
      case HTTP_NOT_FOUND    => "Not Found"
      case HTTP_UNAUTHORIZED => "Access denied"
      case code: Any         => String.valueOf(code)
    }
  }

  protected def touchUrl(url: URL): Tuple2[Int, URL] = {
    try {
      val hc = url.openConnection().asInstanceOf[HttpURLConnection]
      hc.setRequestMethod("HEAD")
      hc.setDoOutput(true)
      val rc = hc.getResponseCode
      rc match {
        case HTTP_OK => (rc, url)
        case HTTP_MOVED_TEMP | HTTP_MOVED_PERM =>
          val loc = hc.getHeaderField("Location")
          if (null == loc) {
            (rc, null)
          } else {
            if (loc.startsWith("http:") || loc.startsWith("https:")) {
              (rc, new URL(loc))
            } else {
              (rc, new URL(url.getProtocol + "://" + url.getHost + ":" + url.getPort + loc))
            }
          }
        case _ => (rc, null)
      }
    } catch {
      case e: IOException => (-1, null)
    }
  }

  protected def finish(elaps: Long) {
    val printurl = "\r" + name + " " + url + " "
    if (elaps == 0) println(printurl + (status.total / 1024) + "KB")
    else println(printurl + (status.total / 1024) + "KB(" + ((status.total / 1024.0 / elaps * 100000.0).toInt / 100.0) + "KB/s)")
  }
}
