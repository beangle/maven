package org.beangle.maven.plugin.code

import java.io.File

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Mojo, Parameter, LifecyclePhase, ResolutionScope }
import org.apache.maven.project.MavenProject
import org.beangle.commons.file.text.{ Formater, FormaterBuilder }
import org.beangle.commons.io.Files./

@Mojo(name = "format-ws", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class FormatSpaceMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  def execute(): Unit = {
    val builder = new FormaterBuilder()
    builder.enableTab2space(4).enableTrimTrailingWhiteSpace().insertFinalNewline()
    val formater = builder.build()

    var dir = System.getProperty("dir")
    var ext = Option(System.getProperty("ext"))

    if (null == dir) {
      import collection.JavaConverters.asScalaBuffer
      asScalaBuffer(project.getCompileSourceRoots) foreach { resource =>
        format(resource, formater, ext)
      }

      asScalaBuffer(project.getTestCompileSourceRoots) foreach { resource =>
        format(resource, formater, ext)
      }

      asScalaBuffer(project.getResources) foreach { resource =>
        format(resource.getDirectory, formater, ext)
      }

      asScalaBuffer(project.getTestResources) foreach { resource =>
        format(resource.getDirectory, formater, ext)
      }
    } else {
      if (new File(dir).exists()) {
        format(dir, formater, ext)
      } else {
        format(project.getBasedir.getAbsolutePath + / + dir, formater, ext)
      }
    }
  }

  private def format(path: String, formater: Formater, ext: Option[String]): Unit = {
    if (new File(path).exists()) {
      println(s"formating ${path} ...")
      Formater.format(formater, new File(path), ext)
    }
  }
}