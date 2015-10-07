package org.beangle.maven.artifact

import java.io.File
import LocalRepository._

object LocalRepository {

  val Maven2 = "maven2"

  val Ivy2 = "ivy2"

  def findBase(layout: String, base: String): String = {
    if (null == base) {
      if (layout == Maven2) {
        System.getProperty("user.home") + "/.m2/repository"
      } else if (layout == "ivy2") {
        System.getProperty("user.home") + "/.ivy2/cache"
      } else {
        throw new RuntimeException("Do not support layout $layout,Using maven2 or ivy2")
      }
    } else {
      if (base.endsWith("/")) base.substring(0, base.length - 1) else base
    }
  }
}

class LocalRepository(val layout: String, ibase: String) {

  val base: String = findBase(layout, ibase)

  new File(this.base).mkdirs()

  def this() {
    this(Maven2, null)
  }

  def path(artifact: Artifact): String = {
    if (layout == Maven2) {
      base + "/" + artifact.groupId.replace('.', '/') + "/" +
        artifact.artifactId + "/" +
        artifact.version + "/" +
        artifact.artifactId + "-" +
        artifact.version + ".jar"
    } else if (layout == Ivy2) {
      base + "/" + artifact.groupId + "/" + artifact.artifactId +
        "/jars/" + artifact.artifactId +
        "-" + artifact.version + ".jar"
    } else {
      throw new RuntimeException("Do not support layout $layout,Using maven2 or ivy2")
    }
  }
}
