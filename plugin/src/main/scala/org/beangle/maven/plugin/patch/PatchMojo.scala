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
package org.beangle.maven.plugin.patch

import java.io.{ File, FileInputStream, FileOutputStream }
import java.util.jar.JarFile
import java.util.zip.{ ZipEntry, ZipInputStream, ZipOutputStream }

import scala.collection.JavaConversions.bufferAsJavaList

import org.apache.maven.artifact.DefaultArtifact
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Component, Mojo, LifecyclePhase, ResolutionScope, Parameter }
import org.apache.maven.settings.Settings
import org.beangle.commons.collection.Collections
import org.beangle.maven.plugin.hibernate.Hibernates
import org.beangle.maven.plugin.util.Projects

@Mojo(name = "patch-hibernate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class PatchMojo extends AbstractMojo {

  @Parameter(defaultValue = "${settings}", readonly = true)
  var settings: Settings = _

  override def execute() {
    val hibernate = new DefaultArtifact("org.hibernate", "hibernate-core", Hibernates.HibernateVersion,
      "runtime", "jar", "", null)
    val hibernateFile = new File(Projects.getPath(hibernate, settings.getLocalRepository))
    if (!hibernateFile.exists()) {
      getLog.info("Cannot find hibernate-core in locale repository ,patching aborted!")
      return
    }
    getLog.info("Find " + hibernateFile)
    try {
      val hibernateJar = new JarFile(hibernateFile)
      if (null != hibernateJar.getJarEntry(Patches.PatchFlag)) {
        getLog.info("Beangle Hibenate patches had bean applied.")
        hibernateJar.close()
        return
      }
      hibernateJar.close()
    } catch {
      case e: Exception => e.printStackTrace()
    }
    mergePatches(hibernateFile)
  }

  private def mergePatches(hibernateFile: File) {
    try {
      val tmpZip = new File(hibernateFile.getAbsolutePath + ".tmp")
      hibernateFile.renameTo(tmpZip)
      hibernateFile.createNewFile()
      val buffer = Array.ofDim[Byte](4096)
      val out = new ZipOutputStream(new FileOutputStream(hibernateFile))
      val files = Collections.newBuffer[String]
      files ++= Patches.files
      files.add(Patches.PatchFlag)
      val fileIter = files.iterator
      while (fileIter.hasNext) {
        val patch = fileIter.next()
        getLog.info("Patching " + patch)
        var patchFile = "/patches" + patch
        if (patchFile.endsWith("class")) patchFile += "file"
        val in = getClass.getResourceAsStream(patchFile)
        out.putNextEntry(new ZipEntry(patch))
        var read = in.read(buffer)
        while (read > -1) {
          out.write(buffer, 0, read)
          read = in.read(buffer)
        }
        out.closeEntry()
        in.close()
      }
      val zin = new ZipInputStream(new FileInputStream(tmpZip))
      var ze = zin.getNextEntry
      while (ze != null) {
        if (!zipEntryMatch(ze.getName, Patches.files)) {
          out.putNextEntry(ze)
          var read = zin.read(buffer)
          while (read > -1) {
            out.write(buffer, 0, read)
            read = zin.read(buffer)
          }
          out.closeEntry()
        }
        ze = zin.getNextEntry
      }
      zin.close()
      out.close()
      tmpZip.delete()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  private def zipEntryMatch(zeName: String, files: Iterable[String]): Boolean = {
    files.exists(p => p == zeName)
  }
}
