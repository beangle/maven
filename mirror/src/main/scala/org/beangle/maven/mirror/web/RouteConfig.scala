/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.maven.mirror.web

import org.beangle.webmvc.dispatch.{ Route, RouteProvider }
import org.beangle.commons.http.HttpMethods._
import org.beangle.maven.mirror.web.handler.GetHandler
import org.beangle.maven.mirror.web.handler.HeadHandler
import org.beangle.maven.mirror.web.handler.AboutHandler

/**
 * @author chaostone
 */
class RouteConfig extends RouteProvider {

  def routes: Iterable[Route] = {
    List(new Route(GET, "/{path*}", new GetHandler),
      new Route(HEAD, "/{path*}", new HeadHandler),
      new Route(GET, "/about", new AboutHandler))
  }
}