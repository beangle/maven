package org.beangle.maven.artifact

trait Layout {
  def path(artifact: Artifact): String
}

object Maven2 extends Layout {
  def path(a: Artifact): String = {
    "/" + a.groupId.replace('.', '/') + "/" +
      a.artifactId + "/" + a.version + "/" +
      a.artifactId + "-" + a.version +
      (if (a.classifier.isEmpty) "" else "-" + a.classifier.get) +
      "." + a.packaging
  }
}

object Ivy2 extends Layout {
  def path(a: Artifact): String = {
    "/" + a.groupId + "/" + a.artifactId +
      "/jars/" + a.artifactId + "-" + a.version + "." + a.packaging
  }
}
