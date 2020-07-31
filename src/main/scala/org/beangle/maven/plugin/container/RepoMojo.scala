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

import java.io.File

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{LifecyclePhase, Mojo, Parameter, ResolutionScope}
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings
import org.beangle.commons.io.Files
import org.beangle.commons.io.Files./
import org.beangle.commons.lang.Strings
import org.beangle.maven.plugin.util.ZipUtils
import org.beangle.repo.artifact.{Artifact, ArtifactDownloader, Repo}

import scala.jdk.javaapi.CollectionConverters.asScala

@Mojo(name = "repo", defaultPhase = LifecyclePhase.NONE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class RepoMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  @Parameter(defaultValue = "${settings}", readonly = true)
  private var settings: Settings = _

  @Parameter(property = "target")
  private var target:String=_

  def execute(): Unit = {
    if (project.getPackaging != "war" && project.getPackaging != "jar") {
      return
    }
    val projectRepoDir =
      if (null == target) {
        if (null == project.getParent) {
          project.getBuild.getDirectory + / + "repository"
        } else {
          project.getParent.getBuild.getDirectory + / + "repository"
        }
      } else {
        target + / + "repository"
      }
    new File(projectRepoDir).mkdirs()
    val artifacts = new collection.mutable.ArrayBuffer[Artifact]
    asScala(project.getArtifacts) foreach { artifact =>
      var str = artifact.toString
      val scope = artifact.getScope
      var scopeMatched = scope == "provided"
      if (!scopeMatched && !artifact.getVersion.endsWith("SNAPSHOT")) {
        if (scope == "runtime" || scope == "compile") scopeMatched = true
      }
      if (scopeMatched) {
        str = str.replace(":" + scope, "")
        artifacts += Artifact(str)
      }
    }
    val localRepo = Repo.local(settings.getLocalRepository)
    val projectRepo = Repo.local(projectRepoDir)
    copy(artifacts, localRepo, projectRepo)

    val poms = artifacts.map(_.packaging("pom"))
    copy(poms, localRepo, projectRepo)

    val sha1s = artifacts.map(_.sha1)
    copy(sha1s, localRepo, projectRepo)

    ZipUtils.zip(new File(projectRepoDir), new File(projectRepoDir + ".zip"))
    getLog.info(s"Project reposistory is generated in $projectRepoDir")
  }

  def copy(artifacts: collection.Seq[Artifact], localRepo: Repo.Local, projectRepo: Repo.Local): Unit = {
    val missings = artifacts.filter(x => !localRepo.file(x).exists)
    if (missings.nonEmpty) {
      val downloader = new ArtifactDownloader(new Repo.Remote, localRepo)
      downloader.download(missings)
    }
    artifacts foreach { artifact =>
      val src = localRepo.file(artifact)
      if (src.exists()) {
        val target = projectRepo.file(artifact)
        if (!target.exists()) {
          Files.copy(src, target)
          getLog.info(s"Copy $artifact")
        }
      } else {
        getLog.warn("Cannot find " + src.getAbsolutePath)
      }
    }
  }


}
