package org.beangle.maven.plugin.container

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Mojo, Parameter, LifecyclePhase, ResolutionScope }
import org.apache.maven.project.MavenProject
import org.beangle.commons.lang.Consoles
import org.beangle.commons.lang.Strings
import org.apache.maven.settings.Settings
import org.beangle.maven.plugin.util.Projects
import java.io.File
import org.beangle.maven.artifact.util.Bsdiff
import org.beangle.maven.artifact.Diff
import org.beangle.maven.artifact.Artifact
import org.beangle.maven.artifact.Layout

@Mojo(name = "diff", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DiffMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  @Parameter(defaultValue = "${settings}", readonly = true)
  private var settings: Settings = _

  def execute(): Unit = {
    if (project.getPackaging != "war") {
      getLog.info("Diff Generation supports only war project!")
      return
    }
    var format = System.getProperty("VersionRange")
    var start, end = ""
    if (null == format) {
      start = Consoles.prompt("Input version range starts with:\n", null,
        (_ != project.getVersion))
      end = project.getVersion
    } else {
      val rs = Strings.split(format, "_")
      if (rs.size != 2) {
        println("Version Range should be start_end");
        System.exit(1)
      } else {
        start = rs(0)
        end = rs(1)
      }
    }

    val file1 =
      Projects.getFile(project.getGroupId, project.getArtifactId, start, "war", settings.getLocalRepository)

    val file2 =
      Projects.getFile(project.getGroupId, project.getArtifactId, end, "war", settings.getLocalRepository)

    val diff = Diff(Artifact(project.getGroupId, project.getArtifactId, start, None, "war"), end)

    if (!file1.exists) {
      println(s"Cannot find ${file1.getPath}")
      System.exit(1)
    }
    if (!file2.exists) {
      println(s"Cannot find ${file2.getPath}")
      System.exit(1)
    }

    val diffFile = new File(settings.getLocalRepository + "/" + Layout.Maven2.path(diff))
    println(s"Generating diff file ${diffFile.getPath}")
    Bsdiff.diff(file1, file2, diffFile)

  }

}