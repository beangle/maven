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
