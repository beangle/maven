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
package org.beangle.maven.artifact.downloader

import java.io.{ File, FileOutputStream, InputStream, OutputStream }
import java.net.URL
import org.beangle.commons.io.IOs

class DefaultDownloader(id: String, url: String, location: String) extends AbstractDownloader(id, url, location) {
  protected override def downloading(resource: URL) {
    println("Downloading " + resource)
    super.defaultDownloading(resource.openConnection())
  }
}
