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

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{ Strings, SystemInfo }
import org.beangle.maven.artifact.{ MirrorRepos, Repo, Repos }

object RepoService {
  var repos: Repos = _

  private def init() {
    val mirrors = Collections.newBuffer[Repo.Mirror]
    var cacheable = false

    val local = SystemInfo.user.home + "/.m2/repository"

    SystemInfo.properties.get("M2_CACHEABLE") foreach { c =>
      cacheable = java.lang.Boolean.valueOf(c)
    }

    SystemInfo.properties.get("M2_REMOTES") foreach { remotes =>
      Strings.split(remotes) foreach { name =>
        var pattern: String = "*"
        val remoteName =
          if (name.contains("@")) {
            pattern = Strings.substringBefore(name, "@")
            Strings.substringAfter(name, "@")
          } else {
            name
          }
        val remote =
          remoteName match {
            case "central" => Repo.Remote.CentralURL
            case "aliyun"  => Repo.Remote.AliyunURL
            case _         => name
          }
        mirrors += new Repo.Mirror(remote, remote, pattern)
      }
    }

    var backend: Repo.Mirror = null
    if (mirrors.isEmpty) {
      backend = new Repo.Mirror("central", Repo.Remote.CentralURL)
    } else {
      backend = mirrors.last
      mirrors -= backend
    }
    this.repos = new MirrorRepos(Repo.local(local), mirrors.toList, backend)
    repos.cacheable = cacheable
  }

  init()
}
