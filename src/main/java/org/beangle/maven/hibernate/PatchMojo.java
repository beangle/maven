package org.beangle.maven.hibernate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.settings.Settings;
import org.beangle.maven.util.Projects;

@Mojo(name = "patch-hibernate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PatchMojo extends AbstractMojo {

  @Component
  private Settings settings;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    DefaultArtifact hibernate = new DefaultArtifact("org.hibernate", "hibernate-core",
        Projects.HibernateVersion, "runtime", "jar", "", null);
    String hibernateFile = Projects.getPath(hibernate, settings.getLocalRepository());
    if (!new File(hibernateFile).exists()) {
      getLog().info("Cannot find hibernate-core in locale repository ,patching aborted!");
      return;
    }
    FileSystem fs = null;
    try {
      Path zipFilePath = Paths.get(hibernateFile);
      getLog().info("Find " + hibernateFile);
      fs = FileSystems.newFileSystem(zipFilePath, null);
      for (String patch : Patches.files) {
        Path inside = fs.getPath(patch);
        InputStream is = getClass().getResourceAsStream(patch + "file");
        getLog().info("Patching " + patch);
        Files.copy(is, inside, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (null != fs) try {
        fs.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public Settings getSettings() {
    return settings;
  }

  public void setSettings(Settings settings) {
    this.settings = settings;
  }

}
