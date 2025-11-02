/*
 * Copyright (c) 2025 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.datasteel.crudcraft.codegen.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.stream.Collectors;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.fileheader.LogicEditableHeader;
import nl.datasteel.crudcraft.codegen.fileheader.LogicStrictHeader;

/**
 * Helper for deriving common package, class and header information for stub generators.
 */
public final class StubGeneratorUtil {

    private StubGeneratorUtil() {
    }

    private static final String LICENSE_HEADER = loadLicenseHeader();

    public static String licenseHeader() {
        return LICENSE_HEADER;
    }

    private static String loadLicenseHeader() {
        try {
            String baseDir = System.getProperty("maven.multiModuleProjectDirectory", System.getProperty("user.dir"));
            Path header = Path.of(baseDir, ".license-header");
            String content = Files.readString(header, StandardCharsets.UTF_8)
                    .replace("YEAR", String.valueOf(Year.now().getValue()))
                    .replace("OWNER", "CrudCraft contributors");
            return content.lines()
                    .filter(line -> !line.startsWith("/*") && !line.startsWith("*/"))
                    .map(line -> line.startsWith(" *") ? line.substring(2) : line)
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Derives package name, class name and header text for a stub generator.
     *
     * @param model          model metadata
     * @param subPackage     sub package (e.g. "service")
     * @param suffix         class name suffix (e.g. "Service")
     * @param layer          layer type for headers (e.g. "Service")
     * @param generatorClass generator implementation class
     * @return metadata for the stub
     */
    public static StubMeta stubMeta(
            ModelDescriptor model,
            String subPackage,
            String suffix,
            String layer,
            Class<?> generatorClass
    ) {
        String pkg = model.getBasePackage() + "." + subPackage;
        String name = model.getName() + suffix;
        String header = model.isEditable()
                ? LogicEditableHeader.header(layer, model.getName(), pkg, generatorClass.getSimpleName())
                : LogicStrictHeader.header(model.getName(), pkg, generatorClass.getSimpleName());
        return new StubMeta(pkg, name, header);
    }

    /**
     * Simple record holding stub metadata.
     *
     * @param pkg package name for the stub
     * @param name simple class name
     * @param header javadoc header text
     */
    public record StubMeta(String pkg, String name, String header) {
    }
}
