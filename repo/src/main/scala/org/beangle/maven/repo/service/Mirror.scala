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
package org.beangle.maven.repo.service

import org.beangle.commons.lang.SystemInfo
import java.io.File
import org.beangle.maven.artifact.downloader.RangeDownloader

/**
 * @author chaostone
 */
class Mirror(val remote: String) {

  import Repository._
  def exists(filePath: String): Boolean = {
    if (localExists(filePath)) true
    else {
      new RangeDownloader("download", remote + filePath, localPath(filePath)).start()
      localExists(filePath)
    }
  }

  def get(filePath: String): File = {
    val localPath = Repository.local + filePath
    val localFile = new File(localPath)
    if (localFile.exists) localFile
    else {
      new RangeDownloader("download", remote + filePath, localPath).start()
      localFile
    }
  }

}
