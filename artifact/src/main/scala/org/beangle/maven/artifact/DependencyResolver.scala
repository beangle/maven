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
