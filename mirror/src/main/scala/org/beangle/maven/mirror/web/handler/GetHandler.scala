/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.maven.mirror.web.handler

import java.io.FileInputStream
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.spi
import org.beangle.maven.mirror.service.Mirror
import org.beangle.webmvc.execution.Handler
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import java.io.IOException
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.web.io.RangedWagon
/**
 * @author chaostone
 */
class GetHandler extends Handler {
  val wagon = new RangedWagon
  def handle(request: HttpServletRequest, response: HttpServletResponse): Any = {
    val filePath = PathHelper.getFilePath(request)
    if (Mirror.exists(filePath)) {
      val file = Mirror.get(filePath)
      wagon.copy(new FileInputStream(file), request, response)
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }
  }

}