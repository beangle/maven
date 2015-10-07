package org.beangle.maven.repository.web.action

import java.io.File
import org.beangle.commons.lang.{ Strings, SystemInfo }
import org.beangle.maven.artifact.downloader.RangeDownloader
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.execution.Handler
import org.beangle.commons.io.IOs
import java.io.FileInputStream
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
/**
 * @author chaostone
 */
class Maven2Handler extends ActionSupport with Handler {

  def handle(request: HttpServletRequest, response: HttpServletResponse): Any = {
    val path = Params.get("path").get
    val url = request.getRequestURI
    val filePath =
      if (url.contains(".")) {
        path + ("." + Strings.substringAfterLast(url, "."))
      } else path

    var localPath = SystemInfo.user.home + "/.m2/repository/" + filePath
    var remotePath = "http://central.maven.org/maven2/" + filePath
    val file = new File(localPath)
    if (file.exists()) {
      val is = new FileInputStream(file)
      IOs.copy(is, response.getOutputStream)
      //FIXME rebase on beangle 4.4.1
      is.close()
    } else {
      val downloader = new RangeDownloader("download", remotePath, localPath)
      downloader.start()
      if (file.exists()) {
        val is = new FileInputStream(file)
        IOs.copy(is, response.getOutputStream)
        is.close()
      } else {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      }
    }
  }

}