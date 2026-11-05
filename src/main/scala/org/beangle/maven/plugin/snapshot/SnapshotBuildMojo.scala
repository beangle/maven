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
package org.beangle.maven.plugin.snapshot

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{LifecyclePhase, Mojo, Parameter}
import org.apache.maven.project.MavenProject

import java.io.File

/**
 * Build a timestamped SNAPSHOT jar/war and its Maven-style `.sha1`.
 */
@Mojo(name = "snapshot-build", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
class SnapshotBuildMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private var project: MavenProject = _

  @Parameter(property = "snapshotDirectory", defaultValue = "${project.build.directory}/snapshot")
  private var snapshotDirectory: File = _

  override def execute(): Unit = {
    SnapshotSupport.build(project, snapshotDirectory) match {
      case Left(message) => getLog.warn(message)
      case Right(build) =>
        getLog.info(s"Build ${build.file.getAbsolutePath}")
        getLog.info(s"Generated ${build.sha1.getName}")
    }
  }
}
