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

import com.sun.net.httpserver.HttpServer
import org.junit.Assert._
import org.junit.Test

import java.io.File
import java.net.{InetSocketAddress, URI}
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicReference

class SnapshotSupportTest {

  @Test
  def computesLowercaseSha1Hex(): Unit = {
    val file = Files.createTempFile("snapshot", ".bin").toFile
    try {
      Files.write(file.toPath, "hello".getBytes(StandardCharsets.UTF_8))
      assertEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d", SnapshotSupport.sha1Hex(file))
    } finally file.delete()
  }

  @Test
  def computesEmptyFileSha1(): Unit = {
    val file = Files.createTempFile("snapshot", ".empty").toFile
    try assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", SnapshotSupport.sha1Hex(file))
    finally file.delete()
  }

  @Test
  def laysOutMavenRepositoryPath(): Unit = {
    assertEquals("org/beangle/commons/beangle-commons/5.0.0-SNAPSHOT",
      SnapshotSupport.artifactPath("org.beangle.commons", "beangle-commons", "5.0.0-SNAPSHOT"))
  }

  @Test
  def joinsRepositoryUrlWithSingleSlash(): Unit = {
    assertEquals("http://127.0.0.1:8080/maven/org/beangle/commons/x.jar",
      SnapshotSupport.snapshotUrl("http://127.0.0.1:8080/maven/", "org/beangle/commons/x.jar"))
    assertEquals("http://127.0.0.1:8080/maven/org/beangle/commons/x.jar",
      SnapshotSupport.snapshotUrl("http://127.0.0.1:8080/maven", "org/beangle/commons/x.jar"))
  }

  @Test
  def readsBearerTokenFromCredentials(): Unit = {
    val file = credentialsFile("token=s3cret\n")
    try {
      assertEquals(Some("s3cret"), SnapshotSupport.token(file))
      assertEquals(Some("Bearer s3cret"), SnapshotSupport.bearerAuthorization(file))
    } finally file.delete()
  }

  @Test
  def ignoresMissingOrTokenlessCredentials(): Unit = {
    assertEquals(None, SnapshotSupport.token(null))
    assertEquals(None, SnapshotSupport.token(new File("does-not-exist")))
    val file = credentialsFile("user=deploy\npassword=s3cret\n")
    try assertEquals(None, SnapshotSupport.bearerAuthorization(file))
    finally file.delete()
  }

  @Test
  def buildsTimestampedArtifactAndChecksum(): Unit = {
    val dir = Files.createTempDirectory("snapshot-build").toFile
    val source = Files.createTempFile("source", ".jar").toFile
    try {
      Files.write(source.toPath, "hello".getBytes(StandardCharsets.UTF_8))
      val build = SnapshotSupport.buildSnapshot(source, dir, "demo", "1.0-SNAPSHOT", "jar", "20260101.000000-1")
      assertEquals("demo-1.0-20260101.000000-1.jar", build.file.getName)
      assertEquals("hello", new String(Files.readAllBytes(build.file.toPath), StandardCharsets.UTF_8))
      assertEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d",
        new String(Files.readAllBytes(build.sha1.toPath), StandardCharsets.UTF_8))
    } finally {
      source.delete()
      Option(dir.listFiles()).getOrElse(Array.empty).foreach(_.delete())
      dir.delete()
    }
  }

  @Test
  def uploadsFileWithBearerAuthorization(): Unit = {
    val request = new AtomicReference[(String, String, String)]
    val received = new AtomicReference[String]
    val server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/maven", exchange => {
      request.set((exchange.getRequestMethod, exchange.getRequestHeaders.getFirst("Authorization"),
        exchange.getRequestURI.getPath))
      received.set(new String(exchange.getRequestBody.readAllBytes(), StandardCharsets.UTF_8))
      exchange.sendResponseHeaders(201, -1)
      exchange.close()
    })
    server.start()
    val file = Files.createTempFile("snapshot", ".jar").toFile
    try {
      Files.write(file.toPath, "jar-bytes".getBytes(StandardCharsets.UTF_8))
      val url = URI.create(s"http://127.0.0.1:${server.getAddress.getPort}/maven/org/example/x.jar").toURL
      val result = SnapshotSupport.upload(url, file, "Bearer s3cret")
      assertEquals(201, result._1)
      assertEquals(("PUT", "Bearer s3cret", "/maven/org/example/x.jar"), request.get())
      assertEquals("jar-bytes", received.get())
    } finally {
      file.delete()
      server.stop(0)
    }
  }

  @Test
  def returnsErrorBodyForRejectedUpload(): Unit = {
    val server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/maven", exchange => {
      val bytes = "publish requires a token".getBytes(StandardCharsets.UTF_8)
      exchange.sendResponseHeaders(401, bytes.length)
      exchange.getResponseBody.write(bytes)
      exchange.close()
    })
    server.start()
    val file = Files.createTempFile("snapshot", ".jar").toFile
    try {
      val url = URI.create(s"http://127.0.0.1:${server.getAddress.getPort}/maven/org/example/x.jar").toURL
      val result = SnapshotSupport.upload(url, file, "Bearer nope")
      assertEquals(401, result._1)
      assertEquals("publish requires a token", result._2)
    } finally {
      file.delete()
      server.stop(0)
    }
  }

  private def credentialsFile(content: String): File = {
    val file = Files.createTempFile("snapshot_credentials", ".properties").toFile
    Files.write(file.toPath, content.getBytes(StandardCharsets.UTF_8))
    file
  }
}
