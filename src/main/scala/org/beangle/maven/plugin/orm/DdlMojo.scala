/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.maven.plugin.orm
import java.io.{ BufferedReader, File, InputStreamReader }

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Component, Mojo, Parameter, LifecyclePhase, ResolutionScope }
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings
import org.beangle.commons.lang.Strings

/**
 * Generate ddl from orm mappings
 */
@Mojo(name = "ddl", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DdlMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  @Parameter(defaultValue = "${settings}", readonly = true)
  private var settings: Settings = _

  @Parameter(property = "dialect", defaultValue = "PostgreSQL")
  private var dialect: String = _

  @Parameter(property = "locale", defaultValue = "zh_CN")
  private var locale: String = _

  override def execute() {
    if (project.getPackaging == "pom") {
      getLog.info("Ddl generation supports jar/war projects,Skip pom projects.")
      return
    }
    var dialectStr = dialect
    dialectStr = Strings.capitalize(dialectStr)
    dialectStr = dialectStr.replace("sql", "SQL")
    val classPath = Orms.classpath(project, settings.getLocalRepository)
    val folder = new File(project.getBuild.getOutputDirectory + "/../ddl/" + dialectStr.toLowerCase + "/")
    folder.mkdirs()
    try {
      getLog.info("Using classpath:" + Orms.simplify(classPath))
      getLog.info("DDl generating in " + folder.getCanonicalPath)
      val pb = new ProcessBuilder("java", "-cp", classPath.toString, "org.beangle.data.orm.tool.DdlGenerator",
        dialectStr, folder.getCanonicalPath, locale)
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
