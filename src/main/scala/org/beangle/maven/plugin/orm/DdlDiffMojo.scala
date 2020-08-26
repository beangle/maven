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
import org.beangle.commons.lang.Strings

/**
 * Generate ddl from orm mappings
 */
@Mojo(name = "ddl-diff", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DdlDiffMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  @Parameter(defaultValue = "${settings}", readonly = true)
  private var settings: Settings = _

  @Parameter(property = "dialect", defaultValue = "postgresql")
  private var dialect: String = _

  @Parameter(property = "oldVersion")
  private var oldVersion: String = _

  @Parameter(property = "newVersion")
  private var newVersion: String = _

  override def execute(): Unit = {
    if (project.getPackaging == "pom") {
      getLog.info("Ddl generation supports jar/war projects,Skip pom projects.")
      return
    }
    if (Strings.isBlank(oldVersion)) {
      getLog.warn("Specify -DoldVersion=???")
      return
    }
    if (Strings.isBlank(newVersion)) {
      getLog.warn("Specify -DnewVersion=???")
      return
    }
    this.dialect = dialect.toLowerCase
    val classPath = Orms.classpath(project, settings.getLocalRepository)
    getLog.debug("Using classpath:" + Orms.simplify(classPath))
    gen(classPath)
  }

  def gen(classpath: String): Unit = {
    val folder = new File(project.getBuild.getOutputDirectory + "/../db/" + dialect + "/migrate")
    folder.mkdirs()
    try {
      val oldDbFile = new File(s"${project.getBasedir.getAbsolutePath}/src/main/resources/db/${dialect}/db-${oldVersion}.xml")
      if (!oldDbFile.exists()) {
        getLog.warn(s"Cannot find ${oldDbFile.getAbsolutePath}")
        return
      }
      val newDbFile = new File(s"${project.getBasedir.getAbsolutePath}/src/main/resources/db/${dialect}/db-${newVersion}.xml")
      if (!newDbFile.exists()) {
        getLog.warn(s"Cannot find ${newDbFile.getAbsolutePath}")
        return
      }
      val target = folder.getCanonicalPath + s"/${oldVersion}-${newVersion}.sql"
      val pb = new ProcessBuilder("java", "-cp", classpath.toString, "org.beangle.data.jdbc.meta.Diff",
        oldDbFile.getAbsolutePath, newDbFile.getAbsolutePath, target)
      getLog.debug(pb.command().toString)
      pb.inheritIO()
      val pro = pb.start()
      pro.waitFor()
      getLog.info("DDl diff generated in " + target)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

}
