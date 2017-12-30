package org.beangle.maven.artifact

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RepoTest extends FunSpec with Matchers {

  describe("Local repo") {
    it("to string") {
      val local = Repo.local(null);
      val artifact = Artifact("org.beangle.commons:beangle-commons-web_2.12:5.0.0.M6")
      println(local.lastestBefore(artifact))
      println(local.lastest(artifact))
    }
  }
}