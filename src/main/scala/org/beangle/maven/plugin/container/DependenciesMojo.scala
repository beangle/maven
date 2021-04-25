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

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{LifecyclePhase, Mojo, Parameter, ResolutionScope}
import org.apache.maven.project.MavenProject

import java.io.{File, FileWriter, IOException}
import scala.jdk.javaapi.CollectionConverters.asScala

@Mojo(name = "dependencies", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DependenciesMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  private val fileName = "dependencies"

  private val inlcuded=Set("provided","compile","runtime")

  def execute(): Unit = {
    val folder = project.getBuild.getOutputDirectory + "/META-INF/beangle/"
    new File(folder).mkdirs()
    val file = new File(folder + fileName)
    file.delete()
    try {
      file.createNewFile()
      val provideds = new collection.mutable.ArrayBuffer[String]

      asScala(project.getArtifacts) foreach { artifact =>
        val str = artifact.toString
        val scope = artifact.getScope
        if (inlcuded.contains(scope)) {
          provideds += str.replace(":jar", "").replace(":" + scope, "")
        }
      }
      val sb = new StringBuilder()
      provideds.sorted foreach { one =>
        sb.append(one).append('\n')
      }
      val fw = new FileWriter(file)
      fw.write(sb.toString)
      fw.close()
      getLog.info(s"Generated dependencies:(${provideds.size})" + file.getAbsolutePath)
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  private def convert(dependencies: String): Iterable[Dependency] = {
    if (dependencies == null) List.empty else {
      val results = new collection.mutable.ArrayBuffer[Dependency]
      getLog.info(dependencies)
      val array = dependencies.replace("\n", "").replace("\r", "").replace(";", ",").split(",")
      for (a <- array if a.length >= 0) {
        val commaIdx = a.indexOf(":")
        if (-1 == commaIdx) {
          getLog.warn("Invalid dependency:" + a)
        } else {
          results += new Dependency(a.substring(0, commaIdx), a.substring(commaIdx + 1))
        }
      }
      results
    }
  }
}
