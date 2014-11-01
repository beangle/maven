Beangle Maven 插件是配合Beangle框架提供的辅助工具。

生成数据库ddl语句

    mvn beangle:gen-ddl

生成Hibernate 配置文件

    mvn beangle:gen-hibernate-cfg

在war工程中，生成容器依赖项：

    mvn beangle:gen-dependency

或者在pom.xml中增加如下片段，直接在mvn clean install中会自动生成META-INF/container.dependencies

    <build>
      <plugins>
        <plugin>
          <groupId>org.beangle.maven</groupId>  
          <artifactId>beangle-maven-plugin</artifactId>  
          <version>0.1.0-SNAPSHOT</version>
          <executions>
            <execution>
              <id>generate</id>
              <phase>compile</phase>
              <goals>
                <goal>gen-dependency</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
 
 需要容器支持war包中的META-INF/container.dependencies，自动下载并管理其中的不同版本的包。
 目前[beangle tomcat](http://github.com/beangle/tomcat)有特定的loader支持。
