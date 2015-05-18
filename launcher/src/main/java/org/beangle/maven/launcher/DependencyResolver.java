package org.beangle.maven.launcher;

import java.net.URL;

public interface DependencyResolver {

  Artifact[] resolve(URL resource);
}