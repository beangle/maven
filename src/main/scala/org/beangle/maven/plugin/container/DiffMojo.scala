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
package org.beangle.maven.plugin.container

import java.io.File

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Mojo, Parameter, LifecyclePhase, ResolutionScope }
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings
import org.beangle.commons.io.Files
import org.beangle.commons.file.diff.Bsdiff
import org.beangle.commons.lang.{ Consoles, Strings }
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.repo.artifact.{ Artifact, Diff, Layout }
import org.beangle.maven.plugin.util.Projects
import org.beangle.repo.artifact.Repo

@Mojo(name = "diff", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DiffMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  @Parameter(defaultValue = "${settings}", readonly = true)
  private var settings: Settings = _

  def execute(): Unit = {
    val packaging = project.getPackaging
    if (packaging != "war" && packaging != "jar") {
      getLog.info("Diff Generation supports only war/jar projects!")
      return
    }

    val localRepo = Repo.local(settings.getLocalRepository)
    val thisArtifact = Artifact(project.getGroupId, project.getArtifactId, project.getVersion, None, packaging)
    var format = System.getProperty("VersionRange")
    var start, end = ""
    if (null == format) {
      localRepo.lastestBefore(thisArtifact) match {
        case Some(old) => start = old.version
        case None      => start = Consoles.prompt("Input version range starts with:", null, (_ != project.getVersion))
      }
      end = project.getVersion
    } else {
      val rs = Strings.split(format, "_")
      if (rs.size != 2) {
        println("Version Range should be start_end");
        System.exit(1)
      } else {
        start = rs(0)
        end = rs(1)
      }
    }

    val file1 =
      Projects.getFile(project.getGroupId, project.getArtifactId, start, packaging, settings.getLocalRepository)

    val file2 =
      Projects.getFile(project.getGroupId, project.getArtifactId, end, packaging, settings.getLocalRepository)

    val diff = Diff(Artifact(project.getGroupId, project.getArtifactId, start, None, project.getPackaging), end)

    if (!file1.exists) {
      println(s"Cannot find ${file1.getPath}")
      System.exit(1)
    }
    if (!file2.exists) {
      println(s"Cannot find ${file2.getPath}")
      System.exit(1)
    }

    val diffFile = new File(settings.getLocalRepository + "/" + Layout.Maven2.path(diff))
    println(s"Generating diff file ${diffFile.getPath}")
    val watch = new Stopwatch(true)
    Bsdiff.diff(file1, file2, diffFile)
    Files.copy(diffFile, new File(project.getBuild.getDirectory + "/" + diffFile.getName))
    println(s"Generated ${diffFile.getName}(${diffFile.length / 1000.0}KB) using $watch")
  }

}