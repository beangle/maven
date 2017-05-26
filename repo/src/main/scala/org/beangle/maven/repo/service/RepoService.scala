package org.beangle.maven.repo.service

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{ Strings, SystemInfo }
import org.beangle.maven.artifact.{ MirrorRepos, Repo, Repos }

object RepoService {
  var repos: Repos = _

  private def init() {
    val mirrors = Collections.newBuffer[Repo.Mirror]
    var cacheable = true

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
