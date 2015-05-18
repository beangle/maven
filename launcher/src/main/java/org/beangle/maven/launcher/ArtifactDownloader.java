package org.beangle.maven.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author chaostone
 */
public class ArtifactDownloader {

  private final RemoteRepository remote;
  private final LocalRepository local;

  private Map<String, DownloadStatus> statuses = new ConcurrentHashMap<String, DownloadStatus>();

  private ExecutorService executor;

  public ArtifactDownloader(RemoteRepository remote, LocalRepository local) {
    super();
    this.remote = remote;
    this.local = local;
    this.executor = Executors.newFixedThreadPool(5);
  }

  void dowload(String url, String location) throws IOException {
    DownloadStatus status = statuses.get(url);
    if (new File(location).exists()) { return; }

    System.out.println("download:" + url);
    InputStream input = null;
    OutputStream output = null;
    try {
      File file = new File(location + ".part");
      file.delete();
      file.getParentFile().mkdirs();

      byte[] buffer = new byte[1024 * 4];
      URL resourceURL = new URL(url);

      URLConnection conn = resourceURL.openConnection();
      status.total = conn.getContentLength();
      input = resourceURL.openConnection().getInputStream();
      output = new FileOutputStream(file);

      int n = input.read(buffer);
      while (-1 != n) {
        output.write(buffer, 0, n);
        status.count += n;
        n = input.read(buffer);
      }
      file.renameTo(new File(location));
    } finally {
      if (input != null) input.close();
      if (output != null) output.close();
    }
  }

  public void download(Artifact[] artifacts) throws Exception {
    if (artifacts.length <= 0) return;
    for (final Artifact artifact : artifacts) {
      executor.execute(new Runnable() {
        public void run() {
          String url = remote.url(artifact);
          DownloadStatus status = new DownloadStatus(0);
          statuses.put(url, status);
          try {
            dowload(url, local.path(artifact));
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            statuses.remove(url);
          }
        }
      });
    }

    Thread.sleep(500);
    int i = 0;
    while (!statuses.isEmpty()) {
      Thread.sleep(500);
      char[] splash = new char[] { '\\', '|', '/', '-' };
      print(multiple("\b", 100));
      StringBuilder sb = new StringBuilder();
      sb.append(splash[i % 4]).append("  ");
      for (Map.Entry<String, DownloadStatus> thread : statuses.entrySet()) {
        sb.append((thread.getValue().count / 1024 + "KB/" + (thread.getValue().total / 1024) + "KB    "));
      }
      sb.append((multiple(" ", (100 - sb.length()))));
      i += 1;
      print(sb.toString());
    }
    executor.shutdown();
    print("\n");
  }

  public static String multiple(String msg, int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(msg);
    }
    return sb.toString();
  }

  private static void print(String msg) {
    System.out.print(msg);
  }
}

class DownloadStatus {
  public int total;
  public int count;

  public DownloadStatus(int total) {
    super();
    this.total = total;
  }

}
