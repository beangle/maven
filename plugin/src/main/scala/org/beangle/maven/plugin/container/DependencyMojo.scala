package org.beangle.maven.plugin.container

import java.io.{ File, FileWriter, IOException }

import org.apache.maven.artifact.Artifact
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Component, Mojo, Parameter, LifecyclePhase, ResolutionScope }
import org.apache.maven.project.MavenProject

@Mojo(name = "gen-dependency", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DependencyMojo extends AbstractMojo {

  @Component
  private var project: MavenProject = _

  @Parameter(property = "dependenciesIncludes")
  private var dependencyIncludes: String = "*:*"

  @Parameter(property = "dependencyExcludes")
  private var dependencyExcludes: String = _

  private val fileName = "container.dependencies"

  def execute(): Unit = {
    if (project.getPackaging != "war") {
      getLog.info("Container Dependency Generation supports only war project!")
      return
    }
    val folder = project.getBuild.getOutputDirectory + "/META-INF/beangle"
    new File(folder).mkdirs()
    val file = new File(folder + "/" + fileName)
    file.delete()
    try {
      file.createNewFile()
      val provideds = new collection.mutable.ArrayBuffer[String]
      val excludes = convert(dependencyExcludes)
      val includes = convert(dependencyIncludes)
      import scala.collection.JavaConversions._
      project.getArtifacts foreach { artifact =>
        val groupId = artifact.getGroupId
        val str = artifact.toString
        val scope = artifact.getScope
        val curr = new Dependency(groupId, artifact.getArtifactId)
        var scopeMatched = (scope == "provided")
        if (!scopeMatched && !artifact.getVersion.endsWith("SNAPSHOT")) {
          if (scope == "runtime" || scope == "compile") scopeMatched = true
        }
        var included = false
        if (scopeMatched) {
          included = includes.exists(d => d.matches(curr))
          if (groupId.startsWith("javax.servlet")) included = false
          if (included) {
            included &= (!excludes.exists(d => d.matches(curr)))
          }
        }
        if (scopeMatched && included) provideds += (str.replace(":jar", "").replace(":" + scope, ""))
      }
      val sb = new StringBuilder()
      provideds.sorted foreach { one =>
        sb.append(one).append('\n')
      }
      val fw = new FileWriter(file)
      fw.write(sb.toString)
      fw.close()
      getLog.info("Generated DEPENDENCIES:" + file.getAbsolutePath)
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  private def convert(dependencies: String): Iterable[Dependency] = {
    if (dependencies == null) List.empty else {
      val results = new collection.mutable.ArrayBuffer[Dependency]
      getLog.info(dependencies)
      val array = dependencies.replace("\n", "").replace("\r", "").replace(";", ",").split(",")
      for (a <- array if a.length >= 0) {
        val commaIdx = a.indexOf(":")
        if (-1 == commaIdx) {
          getLog.warn("Invalid dependency:" + a)
        } else {
          results += new Dependency(a.substring(0, commaIdx), a.substring(commaIdx + 1))
        }
      }
      results
    }
  }
}
