# CrudCraft Tools

Utility helpers used during the build process of CrudCraft applications.

## EditableFileTool
The primary utility is `EditableFileTool`, which scans generated sources for files marked with `@CrudCraft:editable`. New files are copied into your source tree so you can modify them, and the generated versions are removed to prevent accidental overwrites.

### Usage
The tool can be invoked from the command line or via Maven. Example Maven configuration:
```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>3.1.0</version>
  <executions>
    <execution>
      <id>copy-editable-sources</id>
      <phase>compile</phase>
      <goals><goal>java</goal></goals>
      <configuration>
        <mainClass>nl.datasteel.crudcraft.tools.EditableFileTool</mainClass>
        <arguments>
          <argument>${project.build.directory}/generated-sources/annotations</argument>
          <argument>${project.basedir}/src/main/java</argument>
        </arguments>
      </configuration>
    </execution>
  </executions>
</plugin>
```
By default the tool uses `target/generated-sources/annotations` and `src/main/java` when run without arguments.

## More Information
- [Editable Stubs Guide](../guides/editable-stubs.md)
- [crudcraft.dev](https://crudcraft.dev)
