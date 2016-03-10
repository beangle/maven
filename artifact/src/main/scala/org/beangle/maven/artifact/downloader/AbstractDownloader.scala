/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
    if (null != urlStatus) {
      println("\r" + urlStatus + " " + url)
      return
    }
    file.getParentFile.mkdirs()
    downloading()
  }

  protected def downloading(): Unit

  protected def touchUrl(url: URL): String = {
    try {
      val headConnection = url.openConnection().asInstanceOf[HttpURLConnection]
      headConnection.setRequestMethod("HEAD")
      headConnection.setDoOutput(true)
      headConnection.getResponseCode match {
        case HttpURLConnection.HTTP_OK           => null
        case HttpURLConnection.HTTP_FORBIDDEN    => "Access denied!"
        case HttpURLConnection.HTTP_NOT_FOUND    => "Not Found"
        case HttpURLConnection.HTTP_UNAUTHORIZED => "Access denied"
        case code: Any                           => String.valueOf(code)
      }
    } catch {
      case e: IOException => "Error transferring file: " + e.getMessage
    }
  }

  protected def finish(elaps: Long) {
    val printurl = "\r" + name + " " + url + " "
    if (elaps == 0) println(printurl + (status.total / 1024) + "KB")
    else println(printurl + (status.total / 1024) + "KB(" + ((status.total / 1024.0 / elaps * 100000.0).toInt / 100.0) + "KB/s)")
  }
}
