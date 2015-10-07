package org.beangle.maven.repository.web

import org.beangle.webmvc.dispatch.{ Route, RouteProvider }
import org.beangle.maven.repository.web.action.Maven2Handler

/**
 * @author chaostone
 */
class RouteConfig extends RouteProvider {

  def routes: Iterable[Route] = {
    List(new Route("/maven2/{path*}", new Maven2Handler))
  }
}