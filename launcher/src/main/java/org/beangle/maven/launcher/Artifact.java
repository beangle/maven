package org.beangle.maven.launcher;

public class Artifact {

	public final String groupId;
	public final String artifactId;
	public final String version;

	public Artifact(String groupId, String artifactId, String version) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

}
