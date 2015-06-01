package org.beangle.maven.plugin.patch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

@Mojo(name = "patch-hibernate-war", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PatchWarMojo extends AbstractMojo {

  @Component
  private Settings settings;

  @Component
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!project.getPackaging().equals("war")) {
      getLog().info("Hibernate war patching supports war project.Skip jar/pom projects.");
      return;
    }
    boolean findBeangleJpa = false;
    boolean findHibernate = false;
    for (Artifact artifact : project.getArtifacts()) {
      if (artifact.getArtifactId().equals("beangle-data-jpa")) findBeangleJpa = true;
      if (artifact.getArtifactId().equals("hibernate-core")) findHibernate = true;
    }
    if (!(findHibernate && findBeangleJpa)) return;
    try {
      for (String patch : Patches.files) {
        String target = project.getBuild().getOutputDirectory() + patch;
        InputStream is = getClass().getResourceAsStream("/patches" + patch + "file");
        getLog().info("Patching " + patch);
        new File(target.substring(0, target.lastIndexOf("/"))).mkdirs();
        new File(target).createNewFile();
        copy(is, new FileOutputStream(target));
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
    }
  }

  static long copy(InputStream source, OutputStream sink) throws IOException {
    long nread = 0L;
    byte[] buf = new byte[8192];
    int n;
    while ((n = source.read(buf)) > 0) {
      sink.write(buf, 0, n);
      nread += n;
    }
    source.close();
    sink.close();
    return nread;
  }

  public Settings getSettings() {
    return settings;
  }

  public void setSettings(Settings settings) {
    this.settings = settings;
  }

}
