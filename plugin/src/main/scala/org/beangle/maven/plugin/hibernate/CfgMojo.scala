/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.net.URL
import org.apache.maven.model.Resource
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import scala.collection.JavaConversions._
import org.beangle.commons.collection.Collections
import org.beangle.commons.io.IOs

@Mojo(name = "hbm2cfg", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class CfgMojo extends AbstractMojo {

  @Parameter(property = "project", defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  @Parameter(property = "dir")
  private var dir: String = _

  private val fileName = "hibernate.cfg.xml"

  override def execute() {
    if (project.getPackaging == "pom") {
      getLog.info("Hibernate config generation supports jar/war projects,Skip pom projects.")
      return
    }
    if (null == dir) {
      dir = project.getBuild.getOutputDirectory + "/META-INF"

      var cfgResourceExists = false
      val resources = project.getBuild.getResources
      var i = 0
      while (i < resources.length) {
        val r = resources(i)
        if (new File(r.getDirectory + "/META-INF/" + fileName).exists) {
          cfgResourceExists = true
          i = resources.length
        }
        i += 1
      }
      if (cfgResourceExists) {
        getLog.info("Hibernate.cfg.xml exists,so generation will use generated-resources.")
        dir = project.getBuild.getOutputDirectory + "/../generated-resources"
      }
    }
    val hbms = Collections.newBuffer[String]
    searchHbm(project.getBuild.getOutputDirectory, hbms)
    if (hbms.isEmpty) {
      getLog.info("No hbm.xml files founded,Hibernate.cfg.xml generation was skipped.")
    } else {
      val folder = new File(dir)
      folder.mkdirs()
      val cfg = new File(folder.getCanonicalPath + File.separator + fileName)
      cfg.createNewFile()
      val template = IOs.readString(this.getClass.getResourceAsStream("/hibernate.cfg.xml.ftl"))
      val mappings = new StringBuilder(100 * hbms.size)
      hbms.sorted
      var last: String = null
      val iter = hbms.iterator
      while (iter.hasNext) {
        val hbmfile = iter.next
        val relative = hbmfile.substring(project.getBuild.getOutputDirectory.length + 1)
        relative.replace('\\', '/')
        if (null != last &&
          !last.startsWith(relative.substring(0, relative.lastIndexOf("/")))) {
          mappings.append('\n')
        }
        mappings.append("    <mapping resource=\"").append(relative).append("\"/>\n")
        last = relative
      }
      if (mappings.length > 0) mappings.deleteCharAt(mappings.length - 1)
      val writer = new FileWriter(cfg)
      writer.append(template.replace("${mappings}", mappings.toString))
      writer.close()
      getLog.info("Generated " + hbms.size + " mappings in " + cfg.getCanonicalPath)
    }
  }

  private def searchHbm(folder: String, results: Iterable[String]) {
    val parent = new File(folder)
    if (!parent.exists()) return
    val files = parent.list
    var i = 0
    while (i < files.length) {
      val name = files(i)
      val child = new File(folder + File.separatorChar + name)
      if (child.isFile && child.exists() && name.endsWith("hbm.xml")) {
        results.add(folder + File.separatorChar + name)
      } else {
        if (child.isDirectory && !isSymbolicLink(child)) searchHbm(child.getCanonicalPath, results)
      }
      i += 1
    }
  }

  private def isSymbolicLink(file: File): Boolean = {
    var canon: File = null
    if (file.getParent == null) {
      canon = file
    } else {
      canon = new File(file.getParentFile.getCanonicalFile, file.getName)
    }
    canon.getCanonicalFile != canon.getAbsoluteFile
  }

}
