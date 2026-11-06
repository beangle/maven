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

import org.apache.maven.project.MavenProject
import org.beangle.commons.net.http.Https

import java.io.{ByteArrayOutputStream, File, FileInputStream}
import java.net.{HttpURLConnection, URL}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, StandardCopyOption}
import java.security.MessageDigest
import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.Properties
import scala.util.Using

object SnapshotSupport {

  case class SnapshotBuild(file: File, sha1: File)

  private val TimestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss")

  def build(project: MavenProject, outputDir: File): Either[String, SnapshotBuild] = {
    val packaging = project.getPackaging
    val version = project.getVersion
    if (version == null || !version.contains("SNAPSHOT") || (packaging != "jar" && packaging != "war")) {
      Left("Only supports war/jar with SNAPSHOT version.")
    } else {
      val source = artifactFile(project, packaging)
      if (!source.isFile) {
        Left(s"Cannot find ${source.getName}, build snapshot is aborted.")
      } else {
        Right(buildSnapshot(source, outputDir, project.getArtifactId, version, packaging))
      }
    }
  }

  def artifactFile(project: MavenProject, packaging: String): File = {
    val artifact = project.getArtifact
    val file = if (artifact == null) null else artifact.getFile
    if (file != null && file.isFile) file
    else new File(project.getBuild.getDirectory, project.getBuild.getFinalName + "." + packaging)
  }

  def buildSnapshot(source: File, outputDir: File, artifactId: String, version: String,
    packaging: String, buildNumber: String = timestampBuildNumber()): SnapshotBuild = {
    Files.createDirectories(outputDir.toPath)
    val baseVersion = version.replace("-SNAPSHOT", "")
    val build = new File(outputDir, artifactId + "-" + baseVersion + "-" + buildNumber + "." + packaging)
    Files.copy(source.toPath, build.toPath, StandardCopyOption.REPLACE_EXISTING)
    val sha1 = new File(build.getAbsolutePath + ".sha1")
    Files.write(sha1.toPath, sha1Hex(build).getBytes(StandardCharsets.UTF_8))
    SnapshotBuild(build, sha1)
  }

  /** Repository-relative path: groupId dots become slashes, then Maven layout. */
  def artifactPath(groupId: String, artifactId: String, version: String): String =
    groupId.split('.').mkString("/") + "/" + artifactId + "/" + version

  def snapshotUrl(base: String, path: String): String =
    base.stripSuffix("/") + "/" + path

  /** The only credential is `token` in snapshot_credentials. */
  def token(file: File): Option[String] = {
    if (file == null || !file.isFile) None
    else {
      val properties = new Properties
      Using.resource(new FileInputStream(file))(properties.load)
      Option(properties.getProperty("token")).map(_.trim).filter(_.nonEmpty)
    }
  }

  def bearerAuthorization(file: File): Option[String] =
    token(file).map(t => "Bearer " + t)

  def sha1Hex(file: File): String = {
    val md = MessageDigest.getInstance("SHA-1")
    val in = new FileInputStream(file)
    try {
      val buffer = new Array[Byte](8192)
      var n = in.read(buffer)
      while (n != -1) {
        md.update(buffer, 0, n)
        n = in.read(buffer)
      }
    } finally {
      in.close()
    }
    val hex = new StringBuilder
    md.digest().foreach(b => hex.append("%02x".format(b & 0xff)))
    hex.toString
  }

  /** Snapshot build number: UTC timestamp plus build sequence, e.g. `20260913.101500-1`. */
  def timestampBuildNumber(): String =
    TimestampFormatter.format(LocalDateTime.now(ZoneOffset.UTC)) + "-1"

  def upload(url: URL, file: File, authorization: String, method: String = "PUT"): (Int, String) = {
    var conn: HttpURLConnection = null
    try {
      conn = url.openConnection.asInstanceOf[HttpURLConnection]
      Https.noverify(conn)
      conn.setUseCaches(false)
      conn.setRequestProperty("Connection", "close")
      conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
      conn.setDoOutput(true)
      conn.setDoInput(true)
      conn.setConnectTimeout(10 * 1000)
      conn.setReadTimeout(10 * 1000)
      conn.setRequestMethod(method)
      conn.setRequestProperty("Content-Type", "application/octet-stream")
      if (null != authorization) conn.addRequestProperty("Authorization", authorization)

      val in = new FileInputStream(file)
      try {
        val out = conn.getOutputStream
        try in.transferTo(out) finally out.close()
      } finally {
        in.close()
      }

      val code = conn.getResponseCode
      val stream = if (code >= 400) conn.getErrorStream else conn.getInputStream
      val body =
        if (stream == null) ""
        else {
          val out = new ByteArrayOutputStream
          try stream.transferTo(out) finally stream.close()
          new String(out.toByteArray, StandardCharsets.UTF_8)
        }
      (code, body)
    } catch {
      case e: Exception =>
        (-1, s"${e.getClass.getName}: ${e.getMessage} (url=$url)")
    } finally {
      if (null != conn) conn.disconnect()
    }
  }
}
