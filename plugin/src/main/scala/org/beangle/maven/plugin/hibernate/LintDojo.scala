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
package org.beangle.maven.plugin.hibernate

import java.io.{ BufferedReader, File, InputStreamReader }

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Component, Mojo, LifecyclePhase, ResolutionScope, Parameter }
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings

@Mojo(name = "hbmlint", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class LintDojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  @Parameter(defaultValue = "${settings}", readonly = true)
  private var settings: Settings = _

  override def execute() {
    if (project.getPackaging == "pom") {
      getLog.info("Hbm Lint supports jar/war projects,Skip pom projects.")
      return
    }
    val folder = new File(project.getBuild.getOutputDirectory + "/../generated-resources/")
    folder.mkdirs()
    val classPath = Hibernates.classpath(project, settings.getLocalRepository)
    try {
      getLog.info("Hibernate Hbm lint result in " + folder.getCanonicalPath)
      val pb = new ProcessBuilder("java", "-cp", classPath.toString, "org.beangle.data.hibernate.tool.HbmLint",
        folder.getCanonicalPath)
      getLog.debug(pb.command().toString)
      pb.redirectErrorStream(true)
      val pro = pb.start()
      pro.waitFor()
      val reader = new BufferedReader(new InputStreamReader(pro.getInputStream))
      var line: String = reader.readLine()
      while (line != null) {
        getLog.info(line)
        line = reader.readLine()
      }
      reader.close()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}