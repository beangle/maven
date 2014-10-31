

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
                <goal>container-dependency</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
