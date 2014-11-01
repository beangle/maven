package org.beangle.maven.util;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

public class Projects {

  public static final String classpath(MavenProject project, String localRepository) {
    StringBuilder classPath = new StringBuilder(project.getBuild().getOutputDirectory());
    for (Artifact artifact : project.getArtifacts()) {
      classPath.append(File.pathSeparator);
      classPath.append(localRepository).append("/").append(artifact.getGroupId().replaceAll("\\.", "/"))
          .append("/").append(artifact.getArtifactId()).append("/").append(artifact.getVersion()).append("/")
          .append(artifact.getArtifactId()).append("-").append(artifact.getVersion()).append(".jar");
    }
    return classPath.toString();
  }
}
