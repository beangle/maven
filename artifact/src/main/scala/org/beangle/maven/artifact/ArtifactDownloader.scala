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
package org.beangle.maven.artifact

import java.io.IOException
import java.util.Map
import java.util.concurrent.{ ConcurrentHashMap, ExecutorService, Executors }

import scala.collection.JavaConversions.mapAsScalaMap

import org.beangle.maven.artifact.downloader.{ Downloader, RangeDownloader }

class ArtifactDownloader(private val remote: RemoteRepository, private val local: LocalRepository) {

  private var statuses: Map[String, Downloader] = new ConcurrentHashMap[String, Downloader]()

  private var executor: ExecutorService = Executors.newFixedThreadPool(5)

  def download(artifacts: Iterable[Artifact]) {
    if (artifacts.size <= 0) return
    var idx = 1
    for (artifact <- artifacts) {
      val id = idx
      executor.execute(new Runnable() {
        def run() {
          val downloader = new RangeDownloader(id + "/" + artifacts.size, remote.url(artifact), local.path(artifact))
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
    sleep(500)
    var i = 0
    while (!statuses.isEmpty && !executor.isTerminated) {
      sleep(500)
      val splash = Array('\\', '|', '/', '-')
      print("\r")
      val sb = new StringBuilder()
      sb.append(splash(i % 4)).append("  ")
      for ((key, value) <- statuses) {
        val downloader = value
        sb.append((downloader.downloaded / 1024 + "KB/" + (downloader.contentLength / 1024) + "KB    "))
      }
      sb.append(" " * (100 - sb.length))
      i += 1
      print(sb.toString)
    }
    executor.shutdown()
    print("\n")
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
