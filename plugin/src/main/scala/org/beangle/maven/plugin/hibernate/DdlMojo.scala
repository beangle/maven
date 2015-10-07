package org.beangle.maven.plugin.hibernate

import java.io.{ BufferedReader, File, InputStreamReader }

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Component, Mojo, Parameter, LifecyclePhase, ResolutionScope }
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings

/**
 *
 */
@Mojo(name = "hbm2ddl", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DdlMojo extends AbstractMojo {

  @Component
  private var project: MavenProject = _

  @Component
  private var settings: Settings = _

  @Parameter(property = "dialect", defaultValue = "PostgreSQL9")
  private var dialect: String = _

  @Parameter(property = "locale", defaultValue = "zh_CN")
  private var locale: String = _

  override def execute() {
    if (project.getPackaging == "pom") {
      getLog.info("Ddl generation supports jar/war projects,Skip pom projects.")
      return
    }
    val dialectStr = dialect
    val classPath = Hibernates.classpath(project, settings.getLocalRepository)
    val folder = new File(project.getBuild.getOutputDirectory + "/../generated-resources/ddl/" +
      dialectStr.toLowerCase() +
      "/")
    folder.mkdirs()
    try {
      getLog.info("Using classpath:" + classPath.toString)
      getLog.info("Hibernate DDl generating in " + folder.getCanonicalPath)
      val pb = new ProcessBuilder("java", "-cp", classPath.toString, "org.beangle.data.hibernate.tool.DdlGenerator",
        "org.hibernate.dialect." + dialectStr + "Dialect", folder.getCanonicalPath, locale)
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

  //  private def getDialect(): String = {
  //    val d = System.getProperty("beangle.dialect")
  //    if (d != null && d.length > 0) d else dialect
  //  }
}
