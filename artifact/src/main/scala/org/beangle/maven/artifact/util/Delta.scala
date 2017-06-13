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
package org.beangle.maven.artifact.util

import java.io.BufferedReader
import java.io.InputStreamReader

object Delta {

  def diff(oldFile: String, newFile: String, diffFile: String): Unit = {
    exec("bsdiff", oldFile, newFile, diffFile)
  }

  def patch(oldFile: String, newFile: String, diffFile: String): Unit = {
    exec("bspatch", oldFile, newFile, diffFile)
  }

  def sha1(fileLoc: String): String = {
    exec("sha1sum", fileLoc)
  }

  private def exec(command: String, args: String*): String = {
    try {
      val arguments = new collection.mutable.ArrayBuffer[String]
      arguments += command
      arguments ++= args

      val pb = new ProcessBuilder(arguments: _*)
      pb.redirectErrorStream(true)
      val pro = pb.start()
      pro.waitFor()
      val reader = new BufferedReader(new InputStreamReader(pro.getInputStream()))
      val sb = new StringBuilder()
      var line = reader.readLine()
      while (line != null) {
        sb.append(line).append('\n')
        line = reader.readLine()
      }
      reader.close()
      sb.toString
    } catch {
      case e: Throwable => throw new RuntimeException(e)
    }
  }
}
