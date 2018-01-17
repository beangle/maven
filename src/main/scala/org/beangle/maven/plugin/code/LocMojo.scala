package org.beangle.maven.plugin.code

import java.io.{ File, FileInputStream, BufferedReader, InputStreamReader, Reader }
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.project.MavenProject
import org.apache.maven.plugins.annotations.{ Mojo, Parameter, LifecyclePhase, ResolutionScope }
import org.beangle.commons.activation.MimeTypes
import org.beangle.commons.io.IOs
import org.beangle.commons.io.Files./
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings

@Mojo(name = "loc", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class LocMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  def execute(): Unit = {
    val stats = Collections.newMap[String, Int]

    countDir(project.getBasedir, stats)

    var sum = 0
    val rs = stats.toList.sortBy(_._2).reverse
    var maxLength = 0
    rs foreach {
      case (e, c) => {
        if (e.length > maxLength) maxLength = e.length
        sum += c
      }
    }

    println(s"Project has $sum lines codes.")
    rs foreach { t =>
      println(Strings.leftPad(t._1, maxLength, ' ') + "  " + t._2)
    }
  }

  private def countDir(path: File, stats: collection.mutable.Map[String, Int]): Unit = {
    if (path.exists()) {
      println(s"counting ${path.getAbsolutePath} ...")
      count(path, stats)
    }
  }

  private def count(dir: File, stats: collection.mutable.Map[String, Int]): Unit = {
    if (!dir.exists()) return ;

    if (dir.isFile) {
      val fileExt = Strings.substringAfterLast(dir.getName, ".")
      if (Strings.isNotBlank(fileExt) && isText(fileExt)) {
        val reader = toBufferedReader(new InputStreamReader(new FileInputStream(dir)))
        var line = reader.readLine()
        var loc = 0;
        while (line != null) {
          if (Strings.isNotBlank(line)) loc += 1
          line = reader.readLine()
        }
        IOs.close(reader)
        stats.get(fileExt) match {
          case Some(c) => stats.put(fileExt, c + loc)
          case None    => stats.put(fileExt, loc)
        }
      }
    } else {
      dir.list() foreach { childName =>
        count(new File(dir.getAbsolutePath + / + childName), stats)
      }
    }
  }

  private def toBufferedReader(reader: Reader): BufferedReader = {
    if (reader.isInstanceOf[BufferedReader]) reader.asInstanceOf[BufferedReader] else new BufferedReader(reader)
  }

  private def isText(fileExt: String): Boolean = {
    MimeTypes.getMimeType(fileExt) match {
      case Some(m) => (m.getPrimaryType == "text" || fileExt == "xml" || fileExt == "js")
      case None    => false
    }
  }
}