package org.beangle.maven.artifact

trait Layout {
  def path(artifact: Artifact): String
  def path(diff: Diff): String
}

object Maven2 extends Layout {
  def path(a: Artifact): String = {
    "/" + a.groupId.replace('.', '/') + "/" + a.artifactId + "/" + a.version + "/" +
      a.artifactId + "-" + a.version + (if (a.classifier.isEmpty) "" else "-" + a.classifier.get) +
      "." + a.packaging
  }

  def path(d: Diff): String = {
    "/" + d.groupId.replace('.', '/') + "/" + d.artifactId + "/" + d.newVersion +
      "/" + d.artifactId + "-" + d.oldVersion + "_" + d.newVersion +
      (if (null == d.classifier) "" else ("-" + d.classifier)) + "." + d.packaging;
  }
}

object Ivy2 extends Layout {
  def path(a: Artifact): String = {
    "/" + a.groupId + "/" + a.artifactId +
      "/jars/" + a.artifactId + "-" + a.version + "." + a.packaging
  }
  def path(d: Diff): String = {
    "/" + d.groupId + "/" + d.artifactId + "/diffs/" +
      d.artifactId + "-" + d.oldVersion + "_" + d.newVersion +
      "." + d.packaging;
  }
}
