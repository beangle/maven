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
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings

/**
 * @author chaostone
 */
object Repository {

  val local = SystemInfo.user.home + "/.m2/repository"

  val mirrors = Collections.newBuffer[Mirror]

  private def init() {
    SystemInfo.properties.get("M2_REMOTES") foreach { remotes =>
      Strings.split(remotes) foreach { name =>
        val remote =
          name match {
            case "central" => "http://central.maven.org/maven2"
            case "aliyun"  => "http://maven.aliyun.com/nexus/content/groups/public"
            case _         => name
          }
        mirrors += new Mirror(remote)
      }
    }
  }
  def local(filePath: String): File = {
    new File(local + filePath)
  }

  def localExists(filePath: String): Boolean = {
    new File(local + filePath).exists()
  }

  def localPath(filePath: String): String = {
    local + filePath
  }

  def exists(filePath: String): Boolean = {
    mirrors.find(x => x.exists(filePath)).isDefined
  }

  def get(filePath: String): File = {
    mirrors.find(x => x.exists(filePath)) match {
      case Some(m) => m.get(filePath)
      case None    => null
    }
  }

}
