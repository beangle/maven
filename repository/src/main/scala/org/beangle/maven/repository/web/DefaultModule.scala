package org.beangle.maven.repository.web

import org.beangle.commons.inject.bind.AbstractBindModule

/**
 * @author chaostone
 */
object DefaultModule extends AbstractBindModule {

  protected override def binding(): Unit = {
    bind(classOf[RouteConfig])
  }
}