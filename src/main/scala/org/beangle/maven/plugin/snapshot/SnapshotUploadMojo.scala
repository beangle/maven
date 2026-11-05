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

import org.apache.maven.plugin.{AbstractMojo, MojoFailureException}
import org.apache.maven.plugins.annotations.{LifecyclePhase, Mojo, Parameter}
import org.apache.maven.project.MavenProject

import java.io.File
import java.net.URI
import scala.collection.mutable.ArrayBuffer

/**
 * Build and upload a timestamped SNAPSHOT jar/war to micdn's `/maven` endpoint.
 */
@Mojo(name = "snapshot-upload", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
class SnapshotUploadMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private var project: MavenProject = _

  @Parameter(property = "snapshotDirectory", defaultValue = "${project.build.directory}/snapshot")
  private var snapshotDirectory: File = _

  @Parameter(property = "snapshotRepoUrl", defaultValue = "unknown-url")
  private var snapshotRepoUrl: String = _

  @Parameter(property = "snapshotCredentials", defaultValue = "${user.home}/.sbt/snapshot_credentials")
  private var snapshotCredentials: File = _

  override def execute(): Unit = {
    SnapshotSupport.build(project, snapshotDirectory) match {
      case Left(message) => getLog.warn(message)
      case Right(build) =>
        if (snapshotRepoUrl == null || snapshotRepoUrl.trim.isEmpty || snapshotRepoUrl == "unknown-url") {
          throw new MojoFailureException("set snapshotRepoUrl, e.g. http://server/maven.")
        }
        val authorization = SnapshotSupport.bearerAuthorization(snapshotCredentials).getOrElse {
          val hint =
            if (null == snapshotCredentials) "snapshotCredentials is not set"
            else if (!snapshotCredentials.exists()) s"credentials file ${snapshotCredentials} does not exist"
            else s"cannot find token in credentials file ${snapshotCredentials}"
          throw new MojoFailureException(s"Snapshot upload is aborted: $hint")
        }
        getLog.info(s"Build ${build.file.getAbsolutePath}")
        getLog.info(s"Generated ${build.sha1.getName}")
        val dir = SnapshotSupport.artifactPath(project.getGroupId, project.getArtifactId, project.getVersion)
        val failures = new ArrayBuffer[String]
        Seq(build.file, build.sha1).foreach { uploadFile =>
          val uploadUrl = SnapshotSupport.snapshotUrl(snapshotRepoUrl, dir + "/" + uploadFile.getName)
          getLog.info(s"Uploading to $uploadUrl")
          val (status, reason) = SnapshotSupport.upload(URI.create(uploadUrl).toURL, uploadFile, authorization)
          if (status >= 200 && status < 300) {
            getLog.info(s"Upload ${uploadFile.getName} success")
          } else {
            val message = s"Upload ${uploadFile.getName} failed for status is $status and reason is $reason"
            getLog.error(message)
            failures += message
          }
        }
        if (failures.nonEmpty) throw new MojoFailureException(failures.mkString("; "))
    }
  }
}
