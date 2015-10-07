package org.beangle.maven.plugin.container

class Dependency(val groupId: String, val artifactId: String) {

  def matches(other: Dependency): Boolean = {
    ((groupId == "*" || groupId == other.groupId) && (artifactId == "*" || artifactId == other.artifactId))
  }
}
