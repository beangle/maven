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
package org.beangle.maven.artifact

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.beangle.maven.artifact.util.Delta
import org.beangle.commons.io.IOs
import java.io.FileReader
import java.io.FileInputStream
import org.beangle.commons.lang.Charsets
import org.beangle.commons.collection.Collections

object Repo {

  abstract class Repository {
    def id: String
    def base: String
    def layout: Layout
    def exists(filePath: String): Boolean
    def exists(a: Artifact): Boolean = {
      exists(base + layout.path(a))
    }
    def url(p: Product): String = {
      p match {
        case a: Artifact => base + layout.path(a)
        case d: Diff     => base + layout.path(d)
      }
    }
  }

  class Local(ibase: String = null, val layout: Layout = Maven2) extends Repository {
    def id = "local"
    def pattern = "*"
    val base = findLocalBase(layout, ibase)
    new File(this.base).mkdirs()

    override def exists(path: String): Boolean = {
      new File(base + path).exists()
    }

    def file(filePath: String): File = {
      new File(base + filePath)
    }

    def file(product: Product): File = {
      new File(url(product))
    }

    def verifySha1(artifact: Artifact): Boolean = {
      val sha1 = artifact.sha1
      if (exists(artifact) && exists(sha1)) {
        val sha1sum = Delta.sha1(url(artifact))
        val sha1inFile = IOs.readString(new FileInputStream(url(sha1)), Charsets.UTF_8)
        if (!sha1sum.startsWith(sha1inFile)) {
          println("Error sha1 for " + artifact + ",Remove it.")
          new File(url(artifact)).delete()
          false
        } else {
          true
        }
      } else {
        true
      }
    }

    def lastest(artifact: Artifact): Option[Artifact] = {
      val parent = new File(url(artifact)).getParentFile().getParentFile()
      if (parent.exists()) {
        val siblings = parent.list().toList
        val versions = Collections.newBuffer[String]
        for (sibling <- siblings) {
          if (!sibling.contains("SNAPSHOT")
            && new File(parent.getAbsolutePath() + File.separator + sibling).isDirectory()) {
            if (sibling.compareTo(artifact.version) < 0) {
              versions += sibling
            }
          }
        }
        versions.sorted
        if (versions.isEmpty) None
        else Some(artifact.forVersion(versions(versions.size - 1)))
      } else {
        None
      }
    }

  }

  object Remote {
    val CentralURL = "http://central.maven.org/maven2"
    val AliyunURL = "http://maven.aliyun.com/nexus/content/groups/public"
  }

  class Remote(val id: String, var base: String, val layout: Layout = Maven2) extends Repository {
    this.base = normalizeUrl(base)
    def this() {
      this("central", Remote.CentralURL, Maven2)
    }

    override def exists(filePath: String): Boolean = {
      try {
        val headConnection = new URL(base + filePath).openConnection().asInstanceOf[HttpURLConnection]
        headConnection.setRequestMethod("HEAD")
        headConnection.setDoOutput(true)
        headConnection.getResponseCode() == HttpURLConnection.HTTP_OK
      } catch {
        case e: Throwable => false
      }
    }
  }

  class Mirror(id: String, base: String, val pattern: String,
               layout: Layout, val cacheable: Boolean) extends Remote(id, base, layout) {
    def matches(filePath: String): Boolean = {
      (pattern == "*" || filePath.startsWith(pattern))
    }
    override def exists(filePath: String): Boolean = {
      if (matches(filePath)) super.exists(filePath) else false
    }

  }

  private def normalizeUrl(baseUrl: String): String = {
    val httpBase = if (!(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) "http://" + baseUrl else baseUrl
    if (httpBase.endsWith("/")) httpBase.substring(0, httpBase.length - 1) else httpBase
  }

  private def findLocalBase(layout: Layout, base: String): String = {
    if (null == base) {
      if (layout == Maven2) {
        System.getProperty("user.home") + "/.m2/repository"
      } else if (layout == Ivy2) {
        System.getProperty("user.home") + "/.ivy2/cache"
      } else {
        throw new RuntimeException("Do not support layout $layout,Using maven2 or ivy2")
      }
    } else {
      if (base.endsWith("/")) base.substring(0, base.length - 1) else base
    }
  }

}
