package org.beangle.maven.container;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * @author chaostone
 */
@Mojo(name = "gen-dependency", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DependencyMojo extends AbstractMojo {

  @Component
  private MavenProject project;

  private final String fileName = "container.dependencies";

  public final void execute() throws MojoExecutionException, MojoFailureException {
    if (!project.getPackaging().equals("war")) {
      getLog().info("Container Dependency Generation supports only war project!");
      return;
    }
    String folder = project.getBuild().getOutputDirectory() + "/META-INF";
    new File(folder).mkdirs();
    File file = new File(folder + "/" + fileName);
    file.delete();
    try {
      file.createNewFile();
      List<String> provideds = new ArrayList<String>();

      for (Artifact artifact : project.getArtifacts()) {
        String groupId = artifact.getGroupId();
        String str = artifact.toString();
        if (artifact.getScope().equals("provided") && !groupId.startsWith("org.scala-lang")
            && !groupId.startsWith("javax.servlet")) {
          provideds.add(str.replace(":jar", "").replace(":provided", ""));
        }
      }
      StringBuilder sb = new StringBuilder();
      Collections.sort(provideds);
      for (String one : provideds) {
        sb.append(one).append('\n');
      }
      FileWriter fw = new FileWriter(file);
      fw.write(sb.toString());
      fw.close();
      getLog().info("Generated DEPENDENCIES:" + file.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
