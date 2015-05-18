package org.beangle.maven.launcher;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CustomDependencyResolver implements DependencyResolver {
  public static final String DependenciesFile = "META-INF/beangle/container.dependencies";

  @Override
  public Artifact[] resolve(URL resource) {
    List<Artifact> artifacts = new ArrayList<Artifact>();
    if (null == resource) return new Artifact[0];
    try {
      InputStreamReader reader = new InputStreamReader(resource.openStream());
      LineNumberReader lr = new LineNumberReader(reader);
      String line = null;
      do {
        line = lr.readLine();
        if (line != null && !line.isEmpty()) {
          String[] infos = line.split(":");
          artifacts.add(new Artifact(infos[0], infos[1], infos[2]));
        }
      } while (line != null);

      lr.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return artifacts.toArray(new Artifact[0]);
  }
}
