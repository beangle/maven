package org.beangle.maven.launcher.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DefaultDownloader extends AbstractDownloader {

  public DefaultDownloader(String id, String url, String location) {
    super(id, url, location);
  }

  @Override
  protected void downloading() throws IOException {
    InputStream input = null;
    OutputStream output = null;
    long startAt= System.currentTimeMillis();
    try {
      File file = new File(location + ".part");
      file.delete();

      byte[] buffer = new byte[1024 * 4];
      URL resourceURL = new URL(url);
      URLConnection conn = resourceURL.openConnection();
      this.status = new DownloadStatus(conn.getContentLengthLong());
      input = resourceURL.openConnection().getInputStream();
      output = new FileOutputStream(file);

      int n = input.read(buffer);
      while (-1 != n) {
        output.write(buffer, 0, n);
        status.count.addAndGet(n);
        n = input.read(buffer);
      }
      file.renameTo(new File(location));
    } finally {
      close(input, output);
    }
    finish(System.currentTimeMillis()-startAt);
  }

}
