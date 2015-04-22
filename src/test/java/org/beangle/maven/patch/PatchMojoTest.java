package org.beangle.maven.patch;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Settings;

public class PatchMojoTest {


  public static void main(String[] args) throws MojoExecutionException, MojoFailureException {
    PatchMojo patch = new PatchMojo();
    Settings settings = new Settings();
    settings.setLocalRepository(System.getProperty("user.home") + "/.m2/repository");
    patch.setSettings(settings);
    patch.execute();
  }
}
