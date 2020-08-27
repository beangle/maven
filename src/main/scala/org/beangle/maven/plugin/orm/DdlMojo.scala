/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.maven.plugin.orm

import java.io.File

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{LifecyclePhase, Mojo, Parameter, ResolutionScope}
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings

/**
 * Generate ddl from orm mappings
 */
@Mojo(name = "ddl", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DdlMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  @Parameter(defaultValue = "${settings}", readonly = true)
  private var settings: Settings = _

  @Parameter(property = "dialect", defaultValue = "PostgreSQL,Mysql,H2,Oracle,Db2,Sqlserver")
  private var dialect: String = _

  @Parameter(property = "locale", defaultValue = "zh_CN")
  private var locale: String = _

  override def execute(): Unit = {
    if (project.getPackaging == "pom") {
      getLog.info("Ddl generation supports jar/war projects,Skip pom projects.")
      return
    }
    val classPath = Orms.classpath(project, settings.getLocalRepository)
    getLog.debug("Using classpath:" + Orms.simplify(classPath))
    gen(classPath, dialect)
  }

  def gen(classpath: String, dialectStr: String): Unit = {
    val folder = new File(project.getBuild.getOutputDirectory + "/../db/")
    folder.mkdirs()
    try {
      val pb = new ProcessBuilder("java", "-cp", classpath.toString, "org.beangle.data.orm.tool.DdlGenerator",
        dialectStr, folder.getCanonicalPath, locale)
      getLog.debug(pb.command().toString)
      pb.inheritIO()
      val pro = pb.start()
      pro.waitFor()
      val warningFile = new File(folder.getCanonicalPath + "/warnings.txt")
      val hasWarning = warningFile.exists()
      getLog.info("DDl generated in " + folder.getCanonicalPath)
      if (hasWarning) {
        getLog.warn("Found some warnings in " + warningFile.getCanonicalPath)
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

}
