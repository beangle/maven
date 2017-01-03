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

class RemoteRepository(baseUrl: String) {

  val httpBase = if (!(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) "http://" + baseUrl else baseUrl

  val base = if (httpBase.endsWith("/")) httpBase.substring(0, httpBase.length - 1) else httpBase

  def this() {
    this("http://central.maven.org/maven2")
  }

  def url(artifact: Artifact): String = {
    base + "/" + artifact.groupId.replace('.', '/') + "/" +
      artifact.artifactId + "/" +
      artifact.version + "/" +
      artifact.artifactId + "-" +
      artifact.version + ".jar"
  }
}
