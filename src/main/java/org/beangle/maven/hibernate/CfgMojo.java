package org.beangle.maven.hibernate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "gen-hibernate-cfg", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CfgMojo extends AbstractMojo {
  @Component
  private MavenProject project;

  private final String fileName = "hibernate.cfg.xml";

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    List<String> hbms = new ArrayList<String>();
    try {
      searchHbm(project.getBuild().getOutputDirectory(), hbms);
      File folder = new File(project.getBuild().getOutputDirectory() + "/../generated-resources");
      folder.mkdirs();
      File cfg = new File(folder.getCanonicalPath() + File.separator + fileName);
      cfg.createNewFile();
      String template = read(this.getClass().getResource("/hibernate.cfg.xml.ftl"));
      StringBuilder mappings = new StringBuilder(100 * hbms.size());
      Collections.sort(hbms);
      for (String hbmfile : hbms) {
        String relative = hbmfile.substring(project.getBuild().getOutputDirectory().length() + 1);
        relative.replace('\\', '/');
        mappings.append("    <mapping resource=\"").append(relative).append("\"/>\n");
      }
      // delete last \n
      if (mappings.length() > 0) mappings.deleteCharAt(mappings.length() - 1);

      Writer writer = new FileWriter(cfg);
      writer.append(template.replace("${mappings}", mappings.toString()));
      writer.close();
      getLog().info("Generated " + hbms.size() + " hbms in " + cfg.getCanonicalPath());
    } catch (IOException e) {

    }
  }

  private void searchHbm(String folder, List<String> results) throws IOException {
    File parent = new File(folder);
    for (String name : parent.list()) {
      File child = new File(folder + File.separatorChar + name);
      if (child.isFile() && child.exists() && name.endsWith("hbm.xml")) {
        results.add(folder + File.separatorChar + name);
      } else {
        if (child.isDirectory() && !Files.isSymbolicLink(child.toPath())) {
          searchHbm(child.getCanonicalPath(), results);
        }
      }
    }
  }

  private String read(URL url) throws IOException {
    StringWriter sw = new StringWriter(16);
    InputStream is = url.openStream();
    copy(new InputStreamReader(is), sw);
    is.close();
    return sw.toString();
  }

  private int copy(Reader input, Writer output) throws IOException {
    char[] buffer = new char[1024];
    int count = 0;
    int n = input.read(buffer);
    while (-1 != n) {
      output.write(buffer, 0, n);
      count += n;
      n = input.read(buffer);
    }
    return count;
  }
}
