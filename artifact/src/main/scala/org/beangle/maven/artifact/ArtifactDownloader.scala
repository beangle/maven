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
package org.beangle.maven.artifact

import java.io.IOException
import java.util.concurrent.{ ConcurrentHashMap, Executors }

import scala.collection.JavaConverters.mapAsScalaMap

import org.beangle.maven.artifact.downloader.{ Downloader, RangeDownloader }
import org.beangle.maven.artifact.util.Delta

/**
 * ArtifactDownloader
 * <p>
 * Support Features
 * <li>1. Display download processes
 * <li>2. Multiple thread downloading
 * <li>3. Detect resource status before downloading
 * </p>
 */
class ArtifactDownloader(private val remote: Repo.Remote, private val local: Repo.Local) {

  private val statuses = new ConcurrentHashMap[String, Downloader]()

  private val executor = Executors.newFixedThreadPool(5)

  def download(artifacts: Iterable[Artifact]): Unit = {
    val sha1s = new collection.mutable.ArrayBuffer[Artifact]
    val diffs = new collection.mutable.ArrayBuffer[Diff]

    for (artifact <- artifacts) {
      if (!local.file(artifact).exists) {
        val sha1 = artifact.sha1
        if (!local.file(sha1).exists()) {
          sha1s += sha1
        }
        local.lastest(artifact) foreach { lastest =>
          diffs += Diff(lastest, artifact.version)
        }
      }
    }
    doDownload(sha1s);

    // download diffs and patch them.
    doDownload(diffs);
    val newers = new collection.mutable.ArrayBuffer[Artifact]
    for (diff <- diffs) {
      val diffFile = local.file(diff)
      if (diffFile.exists) {
        Delta.patch(local.url(diff.older), local.url(diff.newer), local.url(diff))
        newers += diff.newer
      }
    }
    // check it,last time.
    for (artifact <- artifacts) {
      if (!local.file(artifact).exists) {
        newers += artifact
      }
    }
    doDownload(newers);
    // verify sha1 against newer artifacts.
    for (artifact <- newers) {
      local.verifySha1(artifact)
    }
    executor.shutdown()
  }

  private def doDownload(products: Iterable[Product]): Unit = {
    if (products.size <= 0) return
    var idx = 1
    for (artifact <- products) {
      if (!local.file(artifact).exists()) {
        val id = idx
        executor.execute(new Runnable() {
          def run() {
            val downloader = new RangeDownloader(id + "/" + products.size, remote.url(artifact), local.url(artifact))
            statuses.put(downloader.url, downloader)
            try {
              downloader.start()
            } catch {
              case e: IOException => e.printStackTrace()
            } finally {
              statuses.remove(downloader.url)
            }
          }
        })
        idx += 1
      }
    }
    sleep(500)
    var i = 0
    val splash = Array('\\', '|', '/', '-')
    val count = statuses.size
    while (!statuses.isEmpty && !executor.isTerminated) {
      sleep(500)
      print("\r")
      val sb = new StringBuilder()
      sb.append(splash(i % 4)).append("  ")
      for ((key, value) <- mapAsScalaMap(statuses)) {
        val downloader = value
        sb.append((downloader.downloaded / 1024 + "KB/" + (downloader.contentLength / 1024) + "KB    "))
      }
      sb.append(" " * (100 - sb.length))
      i += 1
      print(sb.toString)
    }
    if (count > 0) print("\n")
  }

  private def sleep(millsecond: Int) {
    try {
      Thread.sleep(500)
    } catch {
      case e: InterruptedException => {
        e.printStackTrace()
        throw new RuntimeException(e)
      }
    }
  }
}
