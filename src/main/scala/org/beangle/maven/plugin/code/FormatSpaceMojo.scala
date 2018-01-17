package org.beangle.maven.plugin.code

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Mojo, Parameter, LifecyclePhase, ResolutionScope }
import org.apache.maven.project.MavenProject

@Mojo(name = "format-space", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class FormatSpaceMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private var project: MavenProject = _

  def execute(): Unit = {
    collection.JavaConverters.asScalaBuffer(project.getResources) foreach { resource =>
      println(resource)
    }
  }
}