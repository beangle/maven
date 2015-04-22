package org.beangle.maven.hibernate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.project.MavenProject;
import org.beangle.maven.util.Projects;

public class Hibernates {

  private static Map<String, Artifact> dependencies = new HashMap<String, Artifact>();

  public static final String HibernateVersion = "4.3.1.Final";
  static {
    add("org.scala", "scala-library", "2.11.6");
    add("org.beangle.commons", "beangle-commons-core", "4.2.4");
    add("org.beangle.data", "beangle-data-model", "4.2.2-SNAPSHOT");
    add("org.beangle.data", "beangle-data-jpa", "4.2.2-SNAPSHOT");

    add("org.hibernate", "hibernate-core", HibernateVersion);
    add("org.jboss.logging", "jboss-logging", "3.1.3.GA");
    add("org.jboss.logging", "jboss-logging-annotations", "1.2.0.Beta1");
    add("javax.transaction", "jta", "1.1");
    add("org.jboss.spec.javax.transaction", "jboss-transaction-api_1.2_spec", "1.0.0.Final");
    add("dom4j", "dom4j", "1.6.1");
    add("xml-apis", "xml-apis", "1.0.b2");
    add("org.hibernate.common", "hibernate-commons-annotations", "4.0.4.Final");
    add("org.hibernate.javax.persistence", "hibernate-jpa-2.1-api", "1.0.0.Final");
    add("org.javassist", "javassist", "3.18.1-GA");
    add("antlr", "antlr", "2.7.7");
    add("org.jboss", "jandex", "1.1.0.Final");
    add("org.hibernate", "hibernate-validator", "5.0.2.Final");
    add("com.fasterxml", "classmate", "1.0.0");
  }

  public static final String classpath(MavenProject project, String localRepository) {
    StringBuilder classPath = new StringBuilder(project.getBuild().getOutputDirectory());

    Map<String, Artifact> addon = new HashMap<String, Artifact>(dependencies);
    for (Artifact artifact : project.getArtifacts()) {
      addon.remove(artifact.getArtifactId());
      addToClassPath(classPath, localRepository, artifact);
    }

    for (Artifact artifact : addon.values()) {
      addToClassPath(classPath, localRepository, artifact);
    }

    return classPath.toString();
  }

  private static void addToClassPath(StringBuilder classPath, String localRepository, Artifact artifact) {
    classPath.append(File.pathSeparator);
    classPath.append(Projects.getPath(artifact, localRepository));
  }

  private static void add(String groupId, String artifactId, String version) {
    Artifact artifact = new DefaultArtifact(groupId, artifactId, version, "runtime", "jar", "", null);
    dependencies.put(artifactId, artifact);
  }
}
