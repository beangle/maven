Beangle Maven 插件是配合Beangle框架提供的辅助工具。

生成数据库ddl语句

    mvn beangle:ddl

在war工程中，生成Bas容器依赖项：

    mvn beangle:bas

或者在pom.xml中增加如下片段，直接在mvn clean install中会自动生成META-INF/container.dependencies

    <build>
      <plugins>
        <plugin>
          <groupId>org.beangle.maven</groupId>
          <artifactId>beangle-maven-plugin</artifactId>
          <version>0.3.2</version>
          <executions>
            <execution>
              <id>generate</id>
              <phase>compile</phase>
              <goals>
                <goal>bas</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

 需要容器支持war包中的META-INF/container.dependencies，自动下载并管理其中的不同版本的包。
 目前[beangle bas](http://github.com/beangle/bas)有特定的loader支持。

生成并上传开发SNAPSHOT：

    mvn package beangle:snapshot-upload -DsnapshotRepoUrl=http://127.0.0.1:8080/maven

该目标只处理版本号包含 `SNAPSHOT` 的 jar/war 工程：先在 `${project.build.directory}/snapshot`
生成带 UTC 时间戳的构件和 `.sha1`，再按 Maven 仓库布局以 HTTP PUT 上传到 micdn 的 `/maven`。
上传凭据默认读取 `~/.sbt/snapshot_credentials` 中的 `token` 属性，以 `Authorization: Bearer` 发送：

    token=6bf65bdce91a9f8ba68aca25175076fa

如果只需要生成快照构件而不上传，可使用 `mvn package beangle:snapshot-build`。
