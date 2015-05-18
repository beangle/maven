package org.beangle.maven.launcher;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ArtifactDownloaderTest {
  ArtifactDownloader downloader = new ArtifactDownloader(new RemoteRepository(), new LocalRepository());

  @Test
  public void testDownload() throws Exception {
    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add(new Artifact("org.beangle.commons","beangle-commons-core","4.2.4"));
    artifacts.add(new Artifact("org.beangle.commons","beangle-commons-web","4.2.4"));
    downloader.download(artifacts.toArray(new Artifact[0]));
  }
}
