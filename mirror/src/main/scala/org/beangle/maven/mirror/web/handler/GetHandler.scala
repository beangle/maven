/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.maven.mirror.web.handler

import java.io.FileInputStream
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.spi
import org.beangle.maven.mirror.service.Mirror
import org.beangle.webmvc.execution.Handler
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import java.io.IOException
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.web.io.RangedWagon
import java.io.File
import org.beangle.commons.activation.MimeTypeProvider
import org.beangle.webmvc.api.util.CacheControl
import java.text.SimpleDateFormat
import java.util.Arrays
/**
 * @author chaostone
 */
class GetHandler extends Handler {
  val wagon = new RangedWagon
  def handle(request: HttpServletRequest, response: HttpServletResponse): Any = {
    val filePath = PathHelper.getFilePath(request)
    if (filePath.endsWith("/")) {
      val localFile = Mirror.local(filePath)
      if (localFile.exists) listDir(filePath, localFile, response) else response.setStatus(HttpServletResponse.SC_NOT_FOUND)
    } else {
      if (Mirror.exists(filePath)) {
        val localFile = Mirror.local(filePath)
        if (localFile.isDirectory) {
          listDir(filePath, localFile, response)
        } else {
          val file = Mirror.get(filePath)
          val ext = Strings.substringAfterLast(filePath, ".")
          if (Strings.isNotEmpty(ext)) MimeTypeProvider.getMimeType(ext) foreach (m => response.setContentType(m.toString))
          if (!filePath.contains("SNAPSHOT")) CacheControl.expiresAfter(10, response)
          wagon.copy(new FileInputStream(file), request, response)
        }
      } else {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      }
    }
  }

  private def listDir(uri: String, dir: File, response: HttpServletResponse): Unit = {
    response.setCharacterEncoding("utf-8")
    val formater = new SimpleDateFormat("yyyy-MM-dd HH:mm")
    val writer = response.getWriter
    writer.write(s"<!DOCTYPE html><head><title>Index of $uri</title></head><body>")
    writer.write(s"<h1>Index of $uri</h1><hr/><pre>")
    if (uri != "/") writer.write("<a href=\"../\">../</a>\n")
    val buffer = new StringBuilder(200)
    val items = dir.list()
    Arrays.sort(items.asInstanceOf[Array[Object]])
    items foreach { fileName =>
      buffer.clear()
      val item = new File(dir.getAbsolutePath + File.separator + fileName)
      if (!item.isHidden && fileName.charAt(0) != '_') {
        if (item.isDirectory) {
          buffer ++= ("<a href=\"" + fileName + "/\">" + s"$fileName/</a>")
          if (fileName.length < 59) buffer ++= " " * (59 - fileName.length)
        } else {
          writer.write("<a href=\"" + fileName + "\">" + s"$fileName</a>")
          if (fileName.length < 60) buffer ++= " " * (60 - fileName.length)
        }
        val lastModified = new java.util.Date(item.lastModified)
        buffer ++= (formater.format(lastModified))
        if (item.isDirectory) {
          buffer ++= "                   -"
        } else {
          val fileSize = item.length.toString
          buffer ++= (" " * (20 - fileSize.length))
          buffer ++= fileSize
        }
        buffer ++= ("\n")
        writer.write(buffer.toString)
      }
    }
    writer.write("</pre><hr></body></html>")
  }

}