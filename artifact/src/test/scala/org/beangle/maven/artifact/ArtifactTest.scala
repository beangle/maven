package org.beangle.maven.artifact

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ArtifactTest extends FunSpec with Matchers {
  val older = Artifact("org.beangle.commons", "beangle-commons-core_2.12", "4.6.1", Some("sources"), "jar")
  val artifact = Artifact("org.beangle.commons", "beangle-commons-core_2.12", "4.6.2", Some("sources"),
    "jar")
  val local = new Repo.Local("/home/chaostone/.m2/repository");

  describe("artifact ") {
    it("to string") {
      artifact.toString should be equals ("org.beangle.commons:beangle-commons-core_2.12:4.6.2:sources:jar")
    }

    it("to path") {
      val loc = local.url(artifact)
      loc should be equals ("/home/chaostone/.m2/repository/org/beangle/commons/beangle-commons-core_2.12"
        + "/4.6.2/beangle-commons-core_2.12-4.6.2-sources.jar")
    }
    it("diff path") {
      val diff = Diff(older, "4.6.2");
      val rs = local.url(diff).contains("org/beangle/commons/beangle-commons-core_2.12/4.6.2/beangle-commons-core_2.12-4.6.1_4.6.2-sources.jar.diff")
      rs should be equals (true)
    }
  }
}
