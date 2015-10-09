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

import java.io.File
import java.util.HashMap
import java.util.Map
import org.apache.maven.artifact.Artifact
import org.apache.maven.artifact.DefaultArtifact
import org.apache.maven.project.MavenProject
import org.beangle.maven.plugin.util.Projects
import scala.collection.JavaConversions._

object Hibernates {

  private var dependencies: Map[String, Artifact] = new HashMap[String, Artifact]()

  val HibernateVersion = "4.3.1.Final"

  add("org.scala", "scala-library", "2.11.6")

  add("org.beangle.commons", "beangle-commons-core", "4.4.2")

  add("org.beangle.data", "beangle-data-model_2.11", "4.3.0")

  add("org.beangle.data", "beangle-data-hibernate_2.11", "4.3.0")

  add("org.hibernate", "hibernate-core", HibernateVersion)

  add("org.jboss.logging", "jboss-logging", "3.1.3.GA")

  add("org.jboss.logging", "jboss-logging-annotations", "1.2.0.Beta1")

  add("javax.transaction", "jta", "1.1")

  add("org.jboss.spec.javax.transaction", "jboss-transaction-api_1.2_spec", "1.0.0.Final")

  add("dom4j", "dom4j", "1.6.1")

  add("xml-apis", "xml-apis", "1.0.b2")

  add("org.hibernate.common", "hibernate-commons-annotations", "4.0.4.Final")

  add("org.hibernate.javax.persistence", "hibernate-jpa-2.1-api", "1.0.0.Final")

  add("org.javassist", "javassist", "3.18.1-GA")

  add("antlr", "antlr", "2.7.7")

  add("org.jboss", "jandex", "1.1.0.Final")

  add("org.hibernate", "hibernate-validator", "5.0.2.Final")

  add("com.fasterxml", "classmate", "1.0.0")

  def classpath(project: MavenProject, localRepository: String): String = {
    val classPath = new StringBuilder(project.getBuild.getOutputDirectory)
    val addon = new HashMap[String, Artifact](dependencies)
    for (artifact <- project.getArtifacts) {
      addon.remove(artifact.getArtifactId)
      addToClassPath(classPath, localRepository, artifact)
    }
    for (artifact <- addon.values) {
      addToClassPath(classPath, localRepository, artifact)
    }
    classPath.toString
  }

  private def addToClassPath(classPath: StringBuilder, localRepository: String, artifact: Artifact) {
    classPath.append(File.pathSeparator)
    classPath.append(Projects.getPath(artifact, localRepository))
  }

  private def add(groupId: String, artifactId: String, version: String) {
    val artifact = new DefaultArtifact(groupId, artifactId, version, "runtime", "jar", "", null)
    dependencies.put(artifactId, artifact)
  }
}