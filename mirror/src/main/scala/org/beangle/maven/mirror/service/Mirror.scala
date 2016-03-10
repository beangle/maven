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
package org.beangle.maven.mirror.service

import org.beangle.commons.lang.SystemInfo
import java.io.File
import org.beangle.maven.artifact.downloader.RangeDownloader

/**
 * @author chaostone
 */
object Mirror {
  val local = SystemInfo.user.home + "/.m2/repository"
  val remote = "http://central.maven.org/maven2"

  def local(filePath: String): File = {
    new File(local + filePath)
  }

  def exists(filePath: String): Boolean = {
    val localPath = local + filePath
    val localFile = new File(localPath)
    if (localFile.exists) true
    else {
      new RangeDownloader("download", remote + filePath, localPath).start()
      localFile.exists
    }
  }

  def get(filePath: String): File = {
    val localPath = local + filePath
    val localFile = new File(localPath)
    if (localFile.exists) localFile
    else {
      new RangeDownloader("download", remote + filePath, localPath).start()
      localFile
    }
  }

}