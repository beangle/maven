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
package org.beangle.maven.plugin.container;

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
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * @author chaostone
 */
@Mojo(name = "gen-dependency", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DependencyMojo extends AbstractMojo {

  @Component
  private MavenProject project;

  @Parameter(property = "dependenciesIncludes")
  private String dependencyIncludes = "*:*";

  @Parameter(property = "dependencyExcludes")
  private String dependencyExcludes;

  private final String fileName = "container.dependencies";

  public final void execute() throws MojoExecutionException, MojoFailureException {
    if (!project.getPackaging().equals("war")) {
      getLog().info("Container Dependency Generation supports only war project!");
      return;
    }
    String folder = project.getBuild().getOutputDirectory() + "/META-INF/beangle";
    new File(folder).mkdirs();
    File file = new File(folder + "/" + fileName);
    file.delete();
    try {
      file.createNewFile();
      List<String> provideds = new ArrayList<String>();
      List<Dependency> excludes = convert(dependencyExcludes);
      List<Dependency> includes = convert(dependencyIncludes);

      for (Artifact artifact : project.getArtifacts()) {
        String groupId = artifact.getGroupId();
        String str = artifact.toString();
        String scope = artifact.getScope();
        Dependency curr = new Dependency(groupId, artifact.getArtifactId());

        boolean scopeMatched = scope.equals("provided");
        if (!scopeMatched && !artifact.getVersion().endsWith("SNAPSHOT")) {
          if (scope.equals("runtime") || scope.equals("compile")) scopeMatched = true;
        }

        boolean included = false;
        if (scopeMatched) {
          for (Dependency d : includes) {
            if (d.matches(curr)) {
              included = true;
              break;
            }
          }

          if (groupId.startsWith("javax.servlet")) included = false;

          if (included) {
            for (Dependency d : excludes) {
              if (d.matches(curr)) {
                included = false;
                break;
              }
            }
          }
        }

        if (scopeMatched && included) provideds.add(str.replace(":jar", "").replace(":" + scope, ""));
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

  private List<Dependency> convert(String dependencies) {
    if (dependencies == null) return Collections.emptyList();
    else {
      List<Dependency> results = new ArrayList<Dependency>();
      getLog().info(dependencies);
      String[] array = dependencies.replace("\n", "").replace("\r", "").replace(";", ",").split(",");
      for (String a : array) {
        if (a.length() >= 0) {
          int commaIdx = a.indexOf(":");
          if (-1 == commaIdx) {
            getLog().warn("Invalid dependency:" + a);
          } else {
            results.add(new Dependency(a.substring(0, commaIdx), a.substring(commaIdx + 1)));
          }
        }
      }
      return results;
    }
  }

}
