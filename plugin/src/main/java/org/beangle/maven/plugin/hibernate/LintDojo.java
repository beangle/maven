/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.maven.plugin.hibernate;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

@Mojo(name = "hbmlint", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class LintDojo extends AbstractMojo {
  @Component
  private MavenProject project;

  @Component
  private Settings settings;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (project.getPackaging().equals("pom")) {
      getLog().info("Hbm Lint supports jar/war projects,Skip pom projects.");
      return;
    }
    File folder = new File(project.getBuild().getOutputDirectory() + "/../generated-resources/");
    folder.mkdirs();
    String classPath = Hibernates.classpath(project, settings.getLocalRepository());
    try {
      getLog().info("Hibernate Hbm lint result in " + folder.getCanonicalPath());
      ProcessBuilder pb = new ProcessBuilder("java", "-cp", classPath.toString(),
          "org.beangle.data.jpa.hibernate.tool.HbmLint", folder.getCanonicalPath());
      getLog().debug(pb.command().toString());
      pb.redirectErrorStream(true);
      Process pro = pb.start();
      pro.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(pro.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        getLog().info(line);
      }
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
