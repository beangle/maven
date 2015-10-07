package org.beangle.maven.artifact.downloader

import java.io.{ File, FileOutputStream, InputStream, OutputStream }
import java.net.URL
import org.beangle.commons.io.IOs

class DefaultDownloader(id: String, url: String, location: String) extends AbstractDownloader(id, url, location) {

  protected override def downloading() {
    var input: InputStream = null
    var output: OutputStream = null
    val startAt = System.currentTimeMillis()
    try {
      val file = new File(location + ".part")
      file.delete()
      val buffer = Array.ofDim[Byte](1024 * 4)
      val resourceURL = new URL(url)
      val conn = resourceURL.openConnection()
      this.status = new Downloader.Status(conn.getContentLengthLong)
      input = resourceURL.openConnection().getInputStream
      output = new FileOutputStream(file)
      var n = input.read(buffer)
      while (-1 != n) {
        output.write(buffer, 0, n)
        status.count.addAndGet(n)
        n = input.read(buffer)
      }
      file.renameTo(new File(location))
    } finally {
      IOs.close(input, output)
    }
    finish(System.currentTimeMillis() - startAt)
  }
}
