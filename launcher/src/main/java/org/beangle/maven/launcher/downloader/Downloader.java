package org.beangle.maven.launcher.downloader;

import java.io.IOException;

public interface Downloader {

  String getUrl();

  void start() throws IOException;

  long getDownloaded();

  long getContentLength();

  String getId();

}
