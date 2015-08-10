package org.beangle.maven.launcher.downloader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class AbstractDownloader implements Downloader {

  protected final String id;
  protected final String url;
  protected final String location;
  protected DownloadStatus status = null;

  public AbstractDownloader(String id, String url, String location) {
    super();
    this.id = id;
    this.url = url;
    this.location = location;
  }

  public long getContentLength() {
    return (null == status) ? 0 : status.total;
  }

  public long getDownloaded() {
    return (null == status) ? 0 : status.count.get();
  }

  public String getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public String getLocation() {
    return location;
  }

  public void start() throws IOException {
    File file = new File(location);
    if (file.exists()) { return; }

    URL resourceURL = new URL(url);
    String urlStatus = touchUrl(resourceURL);
    if (null != urlStatus) {
      System.out.print("\r" + urlStatus + " " + url);
      return;
    }
    file.getParentFile().mkdirs();
    downloading();
  }

  protected abstract void downloading() throws IOException;

  /**
   * 访问资源状态
   * 
   * @param url
   * @return null表示可以访问，其他的表示不能访问的原因
   */
  protected String touchUrl(URL url) {
    HttpURLConnection headConnection;
    try {
      headConnection = (HttpURLConnection) url.openConnection();
      headConnection.setRequestMethod("HEAD");
      headConnection.setDoOutput(true);
      int statusCode = headConnection.getResponseCode();
      switch (statusCode) {
      case HttpURLConnection.HTTP_OK:
        return null;
      case HttpURLConnection.HTTP_FORBIDDEN:
        return "Access denied!";
      case HttpURLConnection.HTTP_NOT_FOUND:
        return "Not Found";
      case HttpURLConnection.HTTP_UNAUTHORIZED:
        return "Access denied";
      default:
        return String.valueOf(statusCode);
      }
    } catch (IOException e) {
      return "Error transferring file: " + e.getMessage();
    }
  }

  protected void close(Closeable... objs) {
    for (Closeable obj : objs) {
      try {
        if (obj != null) obj.close();
      } catch (Exception e) {
      }
    }
  }

  protected void finish(long elaps) {
    if (elaps == 0) System.out.print("\r" + getId() + " " + this.getUrl() + " " + (status.total / 1024)
        + "KB");
    else System.out.print("\r" + getId() + " " + this.getUrl() + " " + (status.total / 1024) + "KB("
        + ((int) (status.total / 1024.0 / elaps * 100000.0) / 100.0) + "KB/s)");

  }
}
