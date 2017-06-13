/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.maven.repo.web

import java.util.EnumSet
import org.beangle.webmvc.dispatch.Dispatcher
import javax.servlet.{ DispatcherType, ServletContext }
import org.beangle.cdi.spring.web.ContextListener

class Initializer extends org.beangle.commons.web.init.Initializer {

  override def onStartup(sc: ServletContext) {
    sc.setInitParameter("templatePath", "class://")

    val ctxListener = new ContextListener
    ctxListener.childContextConfigLocation = "WebApplicationContext:Action@classpath:spring-web-context.xml"
    val container = ctxListener.loadContainer()
    addListener(ctxListener)

    sc.addServlet("Action", new Dispatcher(container)).addMapping("/*")
  }
}
