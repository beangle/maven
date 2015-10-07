package org.beangle.maven.plugin.patch

import java.io.{ File, FileOutputStream }

import scala.collection.JavaConversions.asScalaSet

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Component, Mojo, LifecyclePhase, ResolutionScope }
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings
import org.beangle.commons.io.IOs

@Mojo(name = "patch-hibernate-war", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class PatchWarMojo extends AbstractMojo {

  @Component
  var settings: Settings = _

  @Component
  private var project: MavenProject = _

  override def execute() {
    if (project.getPackaging != "war") {
      getLog.info("Hibernate war patching supports war project.Skip jar/pom projects.")
      return
    }
    var findBeangleJpa = false
    var findHibernate = false
    for (artifact <- project.getArtifacts) {
      if (artifact.getArtifactId == "beangle-data-jpa") findBeangleJpa = true
      if (artifact.getArtifactId == "hibernate-core") findHibernate = true
    }
    if (!(findHibernate && findBeangleJpa)) return
    try {
      for (patch <- Patches.files) {
        val target = project.getBuild.getOutputDirectory + patch
        val is = getClass.getResourceAsStream("/patches" + patch + "file")
        getLog.info("Patching " + patch)
        new File(target.substring(0, target.lastIndexOf("/")))
          .mkdirs()
        new File(target).createNewFile()
        IOs.copy(is, new FileOutputStream(target))
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
    }
  }
}
