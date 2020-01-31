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
import java.{util => ju}

import org.apache.maven.artifact.{Artifact, DefaultArtifact}
import org.apache.maven.project.MavenProject
import org.beangle.commons.lang.Strings
import org.beangle.maven.plugin.util.Projects

import scala.jdk.javaapi.CollectionConverters.asScala

object Orms {

  private val dependencies: ju.Map[String, Artifact] = new ju.HashMap[String, Artifact]
  add("ch.qos.logback", "logback-core", "1.3.0-alpha5")
  add("ch.qos.logback", "logback-classic", "1.3.0-alpha5")
  add("org.slf4j", "slf4j-api", "2.0.0-alpha1")

  def classpath(project: MavenProject, localRepository: String): String = {
    val classPath = new StringBuilder(project.getBuild.getOutputDirectory)
    val addon = new ju.HashMap[String, Artifact](dependencies)
    for (artifact <- asScala(project.getArtifacts)) {
      addon.remove(artifact.getArtifactId)
      addToClassPath(classPath, localRepository, artifact)
    }
    for (artifact <- asScala(addon.values)) {
      addToClassPath(classPath, localRepository, artifact)
    }
    val logurl = Orms.getClass.getResource("/logback.xml")
    if (null != logurl) {
      classPath.append(File.pathSeparator)
      classPath.append(Strings.substringBetween(logurl.toString, "jar:file:", "!"))
    }
    classPath.toString
  }

  def simplify(classpath: String): String = {
    val classpaths = Strings.split(classpath, File.pathSeparator)
    val shorted = classpaths map (c => if (c.endsWith(".jar")) Strings.substringAfterLast(c, File.separator) else c)
    Strings.join(shorted, " ")
  }

  private def addToClassPath(classPath: StringBuilder, localRepository: String, artifact: Artifact): Unit = {
    classPath.append(File.pathSeparator)
    classPath.append(Projects.getPath(artifact, localRepository))
  }

  private def add(groupId: String, artifactId: String, version: String): Unit = {
    val artifact = new DefaultArtifact(groupId, artifactId, version, "runtime", "jar", "", null)
    dependencies.put(artifactId, artifact)
  }
}
