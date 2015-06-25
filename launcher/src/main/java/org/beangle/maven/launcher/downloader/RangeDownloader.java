package org.beangle.maven.launcher.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RangeDownloader extends AbstractDownloader {

  private int threads = 20;
  private int step = 20 * 1024;

  private ExecutorService executor = Executors.newFixedThreadPool(threads);

  public RangeDownloader(String id, String url, String location) {
    super(id, url, location);
  }

  @Override
  protected void downloading() throws IOException {
    final URL resourceURL = new URL(url);
    URLConnection conn = resourceURL.openConnection();
    long startAt = System.currentTimeMillis();
    this.status = new DownloadStatus(conn.getContentLengthLong());
    if (this.status.total > Integer.MAX_VALUE) throw new RuntimeException(
        "Too large file.Using DefaultURLDownloader");
    int total = (int) this.status.total;

    final byte[] totalbuffer = new byte[total];
    int begin = 0;
    List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
    while (begin < this.status.total) {
      final int start = begin;
      final int end = ((start + step - 1) > total) ? total : (start + step - 1);
      tasks.add(new Callable<Integer>() {
        public Integer call() throws Exception {
          HttpURLConnection connection = (HttpURLConnection) resourceURL.openConnection();
          connection.setRequestProperty("RANGE", "bytes=" + start + "-" + end);
          InputStream input = connection.getInputStream();
          byte[] buffer = new byte[1024];
          int n = input.read(buffer);
          int next = start;

          while (-1 != n) {
            System.arraycopy(buffer, 0, totalbuffer, next, n);
            status.count.addAndGet(n);
            next += n;
            n = input.read(buffer);
          }
          close(input);
          return end;
        }
      });
      begin += step;
    }
    try {
      executor.invokeAll(tasks);
      executor.shutdown();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (status.count.get() == status.total) {
      OutputStream output = new FileOutputStream(new File(location));
      output.write(totalbuffer, 0, total);
      close(output);
    } else {
      throw new RuntimeException("Download error");
    }
    finish(System.currentTimeMillis() - startAt);
  }

}
