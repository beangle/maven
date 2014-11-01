Beangle Maven 插件是配合Beangle框架提供的辅助工具。

生成数据库ddl语句

    mvn beangle:gen-ddl

生成Hibernate 配置文件

    mvn beangle:gen-hibernate-cfg

在war工程中，生成容器依赖项：

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
