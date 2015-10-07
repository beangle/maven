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
