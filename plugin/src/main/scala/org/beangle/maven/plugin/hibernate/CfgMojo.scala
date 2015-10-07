package org.beangle.maven.plugin.hibernate

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.net.URL
import org.apache.maven.model.Resource
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import scala.collection.JavaConversions._
import org.beangle.commons.collection.Collections

@Mojo(name = "hbm2cfg", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class CfgMojo extends AbstractMojo {

  @Component
  private var project: MavenProject = _

  private val fileName = "hibernate.cfg.xml"

  @Parameter(property = "dir")
  private var dir: String = _

  override def execute() {
    if (project.getPackaging == "pom") {
      getLog.info("Hibernate config generation supports jar/war projects,Skip pom projects.")
      return
    }
    if (null == dir) {
      dir = project.getBuild.getOutputDirectory + "/META-INF"
      val cfgResourceExists = project.getBuild.getResources.exists(r => new File(r.getDirectory + "/META-INF/" + fileName).exists())
      if (cfgResourceExists) {
        getLog.info("Hibernate.cfg.xml exists,so generation will use generated-resources.")
        dir = project.getBuild.getOutputDirectory + "/../generated-resources"
      }
    }
    val hbms = Collections.newBuffer[String]
    searchHbm(project.getBuild.getOutputDirectory, hbms)
    if (hbms.isEmpty) {
      getLog.info("No hbm.xml files founded,Hibernate.cfg.xml generation was skipped.")
    } else {
      val folder = new File(dir)
      folder.mkdirs()
      val cfg = new File(folder.getCanonicalPath + File.separator + fileName)
      cfg.createNewFile()
      val template = read(this.getClass.getResource("/hibernate.cfg.xml.ftl"))
      val mappings = new StringBuilder(100 * hbms.size)
      hbms.sorted
      var last: String = null
      hbms foreach { hbmfile =>
        val relative = hbmfile.substring(project.getBuild.getOutputDirectory.length + 1)
        relative.replace('\\', '/')
        if (null != last &&
          !last.startsWith(relative.substring(0, relative.lastIndexOf("/")))) {
          mappings.append('\n')
        }
        mappings.append("    <mapping resource=\"").append(relative).append("\"/>\n")
        last = relative
      }
      if (mappings.length > 0) mappings.deleteCharAt(mappings.length - 1)
      val writer = new FileWriter(cfg)
      writer.append(template.replace("${mappings}", mappings.toString))
      writer.close()
      getLog.info("Generated " + hbms.size + " mappings in " + cfg.getCanonicalPath)
    }
  }

  private def searchHbm(folder: String, results: Iterable[String]) {
    val parent = new File(folder)
    if (!parent.exists()) return
    for (name <- parent.list()) {
      val child = new File(folder + File.separatorChar + name)
      if (child.isFile && child.exists() && name.endsWith("hbm.xml")) {
        results.add(folder + File.separatorChar + name)
      } else {
        if (child.isDirectory && !isSymbolicLink(child)) searchHbm(child.getCanonicalPath, results)
      }
    }
  }

  private def isSymbolicLink(file: File): Boolean = {
    var canon: File = null
    if (file.getParent == null) {
      canon = file
    } else {
      val canonDir = file.getParentFile.getCanonicalFile
      canon = new File(canonDir, file.getName)
    }
    canon.getCanonicalFile != canon.getAbsoluteFile
  }

  private def read(url: URL): String = {
    val sw = new StringWriter(16)
    val is = url.openStream()
    copy(new InputStreamReader(is), sw)
    is.close()
    sw.toString
  }

  private def copy(input: Reader, output: Writer): Int = {
    val buffer = Array.ofDim[Char](1024)
    var count = 0
    var n = input.read(buffer)
    while (-1 != n) {
      output.write(buffer, 0, n)
      count += n
      n = input.read(buffer)
    }
    count
  }
}
