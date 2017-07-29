Beangle Maven 插件是配合Beangle框架提供的辅助工具。

生成数据库ddl语句

    mvn beangle:ddl

在war工程中，生成Sas容器依赖项：

    mvn beangle:sas

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
                <goal>sas</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

 需要容器支持war包中的META-INF/container.dependencies，自动下载并管理其中的不同版本的包。
 目前[beangle sas](http://github.com/beangle/sas)有特定的loader支持。
