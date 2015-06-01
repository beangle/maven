package org.beangle.maven.launcher;

import java.io.File;

public class LocalRepository {
  public final String base;
  public final String layout;

  public LocalRepository() {
    this("maven2", null);
  }

  public LocalRepository(String layout, String base) {
    this.layout = layout;
    if (null == base) {
      if (layout.equals("maven2")) {
        this.base = System.getProperty("user.home") + "/.m2/repository";
      } else if (layout.equals("ivy2")) {
        this.base = System.getProperty("user.home") + "/.ivy2/cache";
      } else {
        throw new RuntimeException("Do not support layout $layout,Using maven2 or ivy2");
      }
    } else {
      if (base.endsWith("/")) this.base = base.substring(0, base.length() - 1);
      else this.base = base;
    }
    new File(this.base).mkdirs();
  }

  public String path(Artifact artifact) {
    if (layout.equals("maven2")) {
      return base + "/" + artifact.groupId.replace('.', '/') + "/" + artifact.artifactId + "/"
          + artifact.version + "/" + artifact.artifactId + "-" + artifact.version + ".jar";
    } else if (layout.equals("ivy2")) {
      return base + "/" + artifact.groupId + "/" + artifact.artifactId + "/jars/" + artifact.artifactId + "-"
          + artifact.version + ".jar";
    } else {
      throw new RuntimeException("Do not support layout $layout,Using maven2 or ivy2");
    }
  }
}
