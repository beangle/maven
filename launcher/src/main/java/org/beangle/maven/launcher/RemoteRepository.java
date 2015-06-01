package org.beangle.maven.launcher;

public class RemoteRepository {

  public final String base;

  public RemoteRepository() {
    this("http://central.maven.org/maven2");
  }

  public RemoteRepository(String baseUrl) {
    super();
    String httpBase = baseUrl;
    if (!(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) httpBase = "http://" + baseUrl;

    if (httpBase.endsWith("/")) base = httpBase.substring(0, baseUrl.length() - 1);
    else base = httpBase;
  }

  public String url(Artifact artifact) {
    return base + "/" + artifact.groupId.replace('.', '/') + "/" + artifact.artifactId + "/"
        + artifact.version + "/" + artifact.artifactId + "-" + artifact.version + ".jar";
  }
}
