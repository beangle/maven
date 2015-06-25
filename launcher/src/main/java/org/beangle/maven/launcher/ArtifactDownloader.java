package org.beangle.maven.launcher;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.beangle.maven.launcher.downloader.Downloader;
import org.beangle.maven.launcher.downloader.RangeDownloader;

/**
 * ArtifactDownloader
 * <p>
 * Support Features
 * <li>Display download processes
 * <li>Multiple thread downloading
 * <li>Detect resource status before downloading
 * 
 * @author chaostone
 */
public class ArtifactDownloader {

  private final RemoteRepository remote;
  private final LocalRepository local;

  private Map<String, Downloader> statuses = new ConcurrentHashMap<String, Downloader>();

  private ExecutorService executor;

  public ArtifactDownloader(RemoteRepository remote, LocalRepository local) {
    super();
    this.remote = remote;
    this.local = local;
    this.executor = Executors.newFixedThreadPool(5);
  }

  public void download(final Artifact[] artifacts) {
    if (artifacts.length <= 0) return;
    int idx = 1;
    for (final Artifact artifact : artifacts) {
      final int id = idx;
      executor.execute(new Runnable() {
        public void run() {
          Downloader downloader = new RangeDownloader(id + "/" + artifacts.length, remote.url(artifact),
              local.path(artifact));
          statuses.put(downloader.getUrl(), downloader);
          try {
            downloader.start();
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            statuses.remove(downloader.getUrl());
          }
        }
      });
      idx += 1;
    }

    sleep(500);
    int i = 0;
    while (!statuses.isEmpty() && !executor.isTerminated()) {
      sleep(500);
      char[] splash = new char[] { '\\', '|', '/', '-' };
      // print(multiple("\b", 100));
      print("\r");
      StringBuilder sb = new StringBuilder();
      sb.append(splash[i % 4]).append("  ");
      for (Map.Entry<String, Downloader> thread : statuses.entrySet()) {
        Downloader downloader = thread.getValue();
        sb.append((downloader.getDownloaded() / 1024 + "KB/" + (downloader.getContentLength() / 1024) + "KB    "));
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

  private void sleep(int millsecond) {
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private static void print(String msg) {
    System.out.print(msg);
  }
}
