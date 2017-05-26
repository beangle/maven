/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.maven.artifact

trait Product

object Artifact {
  def apply(gav: String): Artifact = {
    val infos = gav.split(":")
    val classifierIdx = infos(2).indexOf('-')
    var classifier: Option[String] = None
    var version: String = ""
    if (-1 == classifierIdx) {
      classifier = None
      version = infos(2)
    } else {
      classifier = Some(infos(2).substring(classifierIdx + 1))
      version = infos(2).substring(0, classifierIdx)
    }
    val packaging = if (infos.length > 3) infos(3) else "jar"
    new Artifact(infos(0), infos(1), version, classifier, packaging)
  }
}
case class Artifact(val groupId: String, val artifactId: String,
                    val version: String, val classifier: Option[String] = None, val packaging: String = "jar")
    extends Product {

  def md5: Artifact = {
    Artifact(groupId, artifactId, version, classifier, packaging + ".md5");
  }
  def sha1: Artifact = {
    Artifact(groupId, artifactId, version, classifier, packaging + ".sha1");
  }

  override def toString: String = {
    groupId + ":" + artifactId + ":" + version + (if (classifier.isEmpty) "" else (":" + classifier.get)) + ":" +
      packaging
  }

  def forVersion(newVersion: String): Artifact = {
    return new Artifact(groupId, artifactId, newVersion, classifier, packaging);
  }

  override def hashCode: Int = {
    toString.hashCode
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case o: Artifact =>
        this.groupId == o.groupId && this.artifactId == o.artifactId &&
          this.version == o.version && this.classifier == o.classifier &&
          this.packaging == o.packaging
      case _ => false
    }
  }

}

object Diff {
  def apply(old: Artifact, newVersion: String): Diff = {
    Diff(old.groupId, old.artifactId, old.version, newVersion, old.classifier, old.packaging + ".diff")
  }
}

case class Diff(val groupId: String, val artifactId: String,
                oldVersion: String, newVersion: String, val classifier: Option[String], packaging: String = "jar")
    extends Product {

  def older: Artifact = {
    new Artifact(groupId, artifactId, oldVersion, classifier, packaging.replace(".diff", ""))
  }

  def newer: Artifact = {
    new Artifact(groupId, artifactId, newVersion, classifier, packaging.replace(".diff", ""))
  }
  override def toString: String = {
    groupId + ":" + artifactId + ":" + oldVersion + "_" + newVersion + (if (classifier.isEmpty) "" else (":" + classifier.get)) + ":" +
      packaging
  }
}
