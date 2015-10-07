package org.beangle.maven.plugin.hibernate

import java.io.{ BufferedReader, File, InputStreamReader }

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Component, Mojo, LifecyclePhase, ResolutionScope }
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings

@Mojo(name = "hbmlint", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class LintDojo extends AbstractMojo {

  @Component
  private var project: MavenProject = _

  @Component
  private var settings: Settings = _

  override def execute() {
    if (project.getPackaging == "pom") {
      getLog.info("Hbm Lint supports jar/war projects,Skip pom projects.")
      return
    }
    val folder = new File(project.getBuild.getOutputDirectory + "/../generated-resources/")
    folder.mkdirs()
    val classPath = Hibernates.classpath(project, settings.getLocalRepository)
    try {
      getLog.info("Hibernate Hbm lint result in " + folder.getCanonicalPath)
      val pb = new ProcessBuilder("java", "-cp", classPath.toString, "org.beangle.data.jpa.hibernate.tool.HbmLint",
        folder.getCanonicalPath)
      getLog.debug(pb.command().toString)
      pb.redirectErrorStream(true)
      val pro = pb.start()
      pro.waitFor()
      val reader = new BufferedReader(new InputStreamReader(pro.getInputStream))
      var line: String = reader.readLine()
      while (line != null) {
        getLog.info(line)
        line = reader.readLine()
      }
      reader.close()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}
