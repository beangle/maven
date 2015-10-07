package org.beangle.maven.artifact

import java.net.URL
import java.io.InputStreamReader
import java.io.LineNumberReader
import org.beangle.commons.collection.Collections

trait DependencyResolver {

  def resolve(resource: URL): Iterable[Artifact]
}

object BeangleDependencyResolver {

  val DependenciesFile = "META-INF/beangle/container.dependencies"
}

class BeangleDependencyResolver extends DependencyResolver {

  override def resolve(resource: URL): Iterable[Artifact] = {
    val artifacts = Collections.newBuffer[Artifact]
    if (null == resource) return Array.ofDim[Artifact](0)
    try {
      val reader = new InputStreamReader(resource.openStream())
      val lr = new LineNumberReader(reader)
      var line: String = null
      do {
        line = lr.readLine()
        if (line != null && !line.isEmpty) {
          val infos = line.split(":")
          artifacts += new Artifact(infos(0), infos(1), infos(2))
        }
      } while (line != null);
      lr.close()
    } catch {
      case e: Exception => e.printStackTrace()
    }
    artifacts
  }
}
