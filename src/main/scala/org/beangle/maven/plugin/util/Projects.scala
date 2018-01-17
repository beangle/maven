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
package org.beangle.maven.plugin.util

import org.apache.maven.artifact.Artifact
import java.io.File

object Projects {

  def getPath(groupId: String, artifactId: String, version: String, packaging: String, localRepository: String): String = {
    val path = new StringBuilder()
    path.append(localRepository).append("/").append(groupId.replaceAll("\\.", "/"))
      .append("/")
      .append(artifactId)
      .append("/")
      .append(version)
      .append("/")
      .append(artifactId)
      .append("-")
      .append(version)
      .append(".").append(packaging)
    path.toString
  }

  def getFile(groupId: String, artifactId: String, version: String, packaging: String, localRepository: String): File = {
    new File(getPath(groupId, artifactId, version, packaging, localRepository))
  }

  def getPath(artifact: Artifact, localRepository: String): String = {
    getPath(artifact.getGroupId, artifact.getArtifactId, artifact.getVersion, artifact.getType, localRepository)
  }
}
