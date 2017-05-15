package org.beangle.maven.artifact

import scala.reflect.api.Mirror
import java.io.File
import org.beangle.maven.artifact.downloader.RangeDownloader

trait Repos {

  def find(path: String): Repo.Remote

  def local: Repo.Local

  def download(path: String, remote: Repo.Remote): File = {
    val localFile = local.file(path)
    if (!localFile.exists()) {
      new RangeDownloader("download", remote.base + path, path).start()
    }
    localFile
  }
}

class ProxyRepos(val local: Repo.Local, val remote: Repo.Remote) extends Repos {
  override def find(path: String): Repo.Remote = {
    remote
  }
}

class MirrorRepos(val local: Repo.Local, val mirrors: List[Repo.Mirror], val backend: Repo.Mirror) extends Repos {
  override def find(path: String): Repo.Remote = {
    val matches = mirrors.filter(x => x.matches(path))
    val exists = matches.find(x => x.exists(path))
    exists.getOrElse(backend)
  }
}
