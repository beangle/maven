package org.beangle.maven.patch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.settings.Settings;
import org.beangle.maven.hibernate.Hibernates;
import org.beangle.maven.util.Projects;

@Mojo(name = "patch-hibernate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PatchMojo extends AbstractMojo {

  @Component
  private Settings settings;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    DefaultArtifact hibernate = new DefaultArtifact("org.hibernate", "hibernate-core",
        Hibernates.HibernateVersion, "runtime", "jar", "", null);
    File hibernateFile = new File(Projects.getPath(hibernate, settings.getLocalRepository()));
    if (!hibernateFile.exists()) {
      getLog().info("Cannot find hibernate-core in locale repository ,patching aborted!");
      return;
    }
    getLog().info("Find " + hibernateFile);
    try {
      JarFile hibernateJar = new JarFile(hibernateFile);
      if (null != hibernateJar.getJarEntry(Patches.PatchFlag)) {
        getLog().info("Beangle Hibenate patches had bean applied.");
        hibernateJar.close();
        return;
      }
      hibernateJar.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    mergePatches(hibernateFile);
  }

  private void mergePatches(File hibernateFile) {
    try {
      File tmpZip = new File(hibernateFile.getAbsolutePath() + ".tmp");
      hibernateFile.renameTo(tmpZip);
      hibernateFile.createNewFile();

      byte[] buffer = new byte[4096];
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(hibernateFile));
      List<String> files = new ArrayList<String>();
      files.addAll(Arrays.asList(Patches.files));
      files.add(Patches.PatchFlag);

      for (String patch : files) {
        getLog().info("Patching " + patch);
        String patchFile = "/patches" + patch;
        if (patchFile.endsWith("class")) patchFile += "file";
        InputStream in = getClass().getResourceAsStream(patchFile);
        out.putNextEntry(new ZipEntry(patch));
        for (int read = in.read(buffer); read > -1; read = in.read(buffer)) {
          out.write(buffer, 0, read);
        }
        out.closeEntry();
        in.close();
      }

      ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpZip));
      for (ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry()) {
        if (!zipEntryMatch(ze.getName(), Patches.files)) {
          out.putNextEntry(ze);
          for (int read = zin.read(buffer); read > -1; read = zin.read(buffer)) {
            out.write(buffer, 0, read);
          }
          out.closeEntry();
        }
      }
      zin.close();
      out.close();
      tmpZip.delete();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean zipEntryMatch(String zeName, String[] files) {
    for (int i = 0; i < files.length; i++) {
      if ((files[i]).equals(zeName)) { return true; }
    }
    return false;
  }

  // public void mergePatchesJDK7(File hibernateFile) {
  // FileSystem fs = null;
  // try {
  // Path zipFilePath = Paths.get(hibernateFile.getAbsolutePath());
  // fs = FileSystems.newFileSystem(zipFilePath, null);
  // List<String> files = new ArrayList<String>();
  // files.addAll(Arrays.asList(Patches.files));
  // files.add(Patches.PatchFlag);
  // for (String patch : files) {
  // String patchFile = "/patches" + patch;
  // if (patchFile.endsWith("class")) patchFile += "file";
  // Path inside = fs.getPath(patchFile);
  // InputStream is = getClass().getResourceAsStream(patchFile);
  // getLog().info("Patching " + patch);
  // Files.copy(is, inside, StandardCopyOption.REPLACE_EXISTING);
  // }
  // } catch (Exception e) {
  // e.printStackTrace();
  // } finally {
  // if (null != fs) try {
  // fs.close();
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  // }
  // }

  public Settings getSettings() {
    return settings;
  }

  public void setSettings(Settings settings) {
    this.settings = settings;
  }

}
