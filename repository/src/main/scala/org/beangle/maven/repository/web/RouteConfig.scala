package org.beangle.maven.repository.web

import org.beangle.webmvc.dispatch.{ Route, RouteProvider }
import org.beangle.commons.http.HttpMethods._
import org.beangle.maven.repository.web.action.GetHandler
import org.beangle.maven.repository.web.action.HeadHandler

/**
 * @author chaostone
 */
class RouteConfig extends RouteProvider {

  def routes: Iterable[Route] = {
    List(new Route(GET, "/maven2/{path*}", new GetHandler),
      new Route(HEAD, "/maven2/{path*}", new HeadHandler))
  }
}