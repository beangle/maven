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
package org.beangle.maven.plugin.container

import java.io.{File, FileOutputStream}

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{LifecyclePhase, Mojo, Parameter, ResolutionScope}
import org.apache.maven.project.MavenProject
import org.beangle.commons.io.{Dirs, IOs}
import org.beangle.commons.lang.ClassLoaders

@Mojo(name = "webxml", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class WebxmlMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  def execute(): Unit = {
    if (project.getPackaging == "war") {
      val webappXml = s"${project.getBasedir}/src/main/webapp/WEB-INF/web.xml"
      if (new File(webappXml).exists()) {
        getLog.info("Ignore web.xml generation.")
      } else {
        getLog.info("Generate web.xml")
        val buildWebInf = s"${project.getBasedir}/target/${project.getArtifactId}-${project.getVersion}/WEB-INF"
        Dirs.on(buildWebInf).mkdirs()
        val webxml = new File(buildWebInf + "/web.xml")
        val os = new FileOutputStream(webxml)
        IOs.copy(ClassLoaders.getResourceAsStream("sas/web.xml").get, os)
        IOs.close(os)
      }
    }
  }
}
