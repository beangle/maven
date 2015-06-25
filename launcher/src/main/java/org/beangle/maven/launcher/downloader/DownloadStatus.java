package org.beangle.maven.launcher.downloader;

import java.util.concurrent.atomic.AtomicLong;

public class DownloadStatus {
  public final long total;
  public final AtomicLong count = new AtomicLong(0);

  public DownloadStatus(long total) {
    super();
    this.total = total;
  }

}
