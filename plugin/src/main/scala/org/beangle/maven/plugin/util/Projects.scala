package org.beangle.maven.plugin.util

import org.apache.maven.artifact.Artifact
//remove if not needed
import scala.collection.JavaConversions._

object Projects {

  def getPath(artifact: Artifact, localRepository: String): String = {
    val path = new StringBuilder()
    path.append(localRepository).append("/").append(artifact.getGroupId.replaceAll("\\.", "/"))
      .append("/")
      .append(artifact.getArtifactId)
      .append("/")
      .append(artifact.getVersion)
      .append("/")
      .append(artifact.getArtifactId)
      .append("-")
      .append(artifact.getVersion)
      .append(".jar")
    path.toString
  }
}
