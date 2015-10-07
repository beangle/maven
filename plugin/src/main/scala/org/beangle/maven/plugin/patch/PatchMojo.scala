package org.beangle.maven.plugin.patch

import java.io.{ File, FileInputStream, FileOutputStream }
import java.util.jar.JarFile
import java.util.zip.{ ZipEntry, ZipInputStream, ZipOutputStream }

import scala.collection.JavaConversions.bufferAsJavaList

import org.apache.maven.artifact.DefaultArtifact
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{ Component, Mojo, LifecyclePhase, ResolutionScope }
import org.apache.maven.settings.Settings
import org.beangle.commons.collection.Collections
import org.beangle.maven.plugin.hibernate.Hibernates
import org.beangle.maven.plugin.util.Projects

@Mojo(name = "patch-hibernate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class PatchMojo extends AbstractMojo {

  @Component
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
      for (patch <- files) {
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
