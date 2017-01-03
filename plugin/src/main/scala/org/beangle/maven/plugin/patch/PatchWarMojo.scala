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
package org.beangle.maven.plugin.patch

import java.io.{ File, FileOutputStream }
import scala.collection.JavaConverters.asScalaSet
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Component, Mojo, LifecyclePhase, ResolutionScope, Parameter }
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings
import org.beangle.commons.io.IOs

@Mojo(name = "patch-hibernate-war", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class PatchWarMojo extends AbstractMojo {

  @Parameter(defaultValue = "${settings}", readonly = true)
  var settings: Settings = _

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  override def execute() {
    if (project.getPackaging != "war") {
      getLog.info("Hibernate war patching supports war project.Skip jar/pom projects.")
      return
    }
    var findBeangleHibernate = false
    var findHibernate = false
    val artifactIter = project.getArtifacts.iterator()
    while (artifactIter.hasNext()) {
      val artifact = artifactIter.next
      if (artifact.getArtifactId.startsWith("beangle-data-hibernate")) findBeangleHibernate = true
      if (artifact.getArtifactId == "hibernate-core") findHibernate = true
    }
    if (!(findHibernate && findBeangleHibernate)) return
    try {
      val iter = Patches.files.iterator
      while (iter.hasNext) {
        val patch = iter.next
        val target = project.getBuild.getOutputDirectory + patch
        val is = getClass.getResourceAsStream("/patches" + patch + "file")
        getLog.info("Patching " + patch)
        new File(target.substring(0, target.lastIndexOf("/"))).mkdirs()
        new File(target).createNewFile()
        IOs.copy(is, new FileOutputStream(target))
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
    }
  }
}
