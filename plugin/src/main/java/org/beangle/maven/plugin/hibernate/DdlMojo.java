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
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

/**
 * Generate Hibernate ddl
 * 
 * @author chii
 */
@Mojo(name = "hbm2ddl", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DdlMojo extends AbstractMojo {

  @Component
  private MavenProject project;

  @Component
  private Settings settings;

  @Parameter(property = "dialect", defaultValue = "PostgreSQL9")
  private String dialect;

  @Parameter(property = "locale", defaultValue = "zh_CN")
  private String locale;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (project.getPackaging().equals("pom")) {
      getLog().info("Ddl generation supports jar/war projects,Skip pom projects.");
      return;
    }
    String dialectStr = getDialect();
    String classPath = Hibernates.classpath(project, settings.getLocalRepository());
    File folder = new File(project.getBuild().getOutputDirectory() + "/../generated-resources/ddl/"
        + dialectStr.toLowerCase() + "/");
    folder.mkdirs();
    try {
      getLog().info("Using classpath:" + classPath.toString());
      getLog().info("Hibernate DDl generating in " + folder.getCanonicalPath());
      ProcessBuilder pb = new ProcessBuilder("java", "-cp", classPath.toString(),
          "org.beangle.data.jpa.hibernate.tool.DdlGenerator", "org.hibernate.dialect." + dialectStr
              + "Dialect", folder.getCanonicalPath(), locale);
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

  private String getDialect() {
    String d = System.getProperty("beangle.dialect");
    if (d != null && d.length() > 0) return d;
    else return dialect;
  }
}
