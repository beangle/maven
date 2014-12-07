package org.beangle.maven.container;

public class Dependency {

  String groupId;

  String artifactId;

  public boolean matches(Dependency other) {
    return ((groupId.equals("*") || groupId.equals(other.groupId)) && (artifactId.equals("*") || artifactId
        .equals(other.artifactId)));
  }

  public Dependency(String groupId, String artifactId) {
    this.groupId = groupId;
    this.artifactId = artifactId;
  }
}
