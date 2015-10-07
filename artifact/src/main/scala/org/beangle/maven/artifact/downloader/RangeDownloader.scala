package org.beangle.maven.artifact.downloader

import java.io.{ File, FileOutputStream }
import java.net.{ HttpURLConnection, URL }
import java.util.concurrent.{ Callable, ExecutorService, Executors }
import org.beangle.commons.io.IOs

class RangeDownloader(name: String, url: String, location: String) extends AbstractDownloader(name, url, location) {

  var threads: Int = 20

  var step: Int = 20 * 1024

  var executor: ExecutorService = Executors.newFixedThreadPool(threads)

  protected override def downloading() {
    val resourceURL = new URL(url)
    val conn = resourceURL.openConnection()
    val startAt = System.currentTimeMillis()
    this.status = new Downloader.Status(conn.getContentLengthLong)
    if (this.status.total > java.lang.Integer.MAX_VALUE) throw new RuntimeException("Too large file.Using DefaultURLDownloader")
    val total = this.status.total.toInt
    val totalbuffer = Array.ofDim[Byte](total)
    var begin = 0
    val tasks = new java.util.ArrayList[Callable[Integer]]
    while (begin < this.status.total) {
      val start = begin
      val end = if (((start + step - 1) > total)) total else (start + step - 1)
      tasks.add(new Callable[Integer]() {
        def call(): java.lang.Integer = {
          val connection = resourceURL.openConnection().asInstanceOf[HttpURLConnection]
          connection.setRequestProperty("RANGE", "bytes=" + start + "-" + end)
          val input = connection.getInputStream
          val buffer = Array.ofDim[Byte](1024)
          var n = input.read(buffer)
          var next = start
          while (-1 != n) {
            System.arraycopy(buffer, 0, totalbuffer, next, n)
            status.count.addAndGet(n)
            next += n
            n = input.read(buffer)
          }
          IOs.close(input)
          return end
        }
      })
      begin += step
    }
    try {
      executor.invokeAll(tasks)
      executor.shutdown()
    } catch {
      case e: InterruptedException => e.printStackTrace()
    }
    if (status.count.get == status.total) {
      val output = new FileOutputStream(new File(location))
      output.write(totalbuffer, 0, total)
      IOs.close(output)
    } else {
      throw new RuntimeException("Download error")
    }
    finish(System.currentTimeMillis() - startAt)
  }

}
