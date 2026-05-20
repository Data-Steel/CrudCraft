/*
 * Copyright (c) 2026 CrudCraft contributors
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

import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.fileheader.LogicEditableHeader;
import nl.datasteel.crudcraft.codegen.fileheader.LogicStrictHeader;


/** Helper for deriving common package, class and header information for stub generators. */
public final class StubGeneratorUtil {

    private StubGeneratorUtil() {}

    /**
     * Returns the license header content used in generated files.
     *
     * @return an empty string because generated application code must not carry CrudCraft's
     *     project license header
     */
    public static String licenseHeader() {
        return loadLicenseHeader();
    }

    private static String loadLicenseHeader() {
        return "";
    }

    /**
     * Derives package name, class name and header text for a stub generator.
     *
     * @param model model metadata
     * @param subPackage sub package (e.g. "service")
     * @param suffix class name suffix (e.g. "Service")
     * @param layer layer type for headers (e.g. "Service")
     * @param generatorClass generator implementation class
     * @return metadata for the stub
     */
    public static StubMeta stubMeta(
            ModelDescriptor model,
            String subPackage,
            String suffix,
            String layer,
            Class<?> generatorClass) {
        String pkg = model.getBasePackage() + "." + subPackage;
        String name = model.getName() + suffix;
        String header =
                model.isEditable()
                        ? LogicEditableHeader.header(
                                layer, model.getName(), pkg, generatorClass.getSimpleName())
                        : LogicStrictHeader.header(
                                model.getName(), pkg, generatorClass.getSimpleName());
        return new StubMeta(pkg, name, header);
    }

    /**
     * Simple record holding stub metadata.
     *
     * @param pkg package name for the stub
     * @param name simple class name
     * @param header javadoc header text
     */
    public record StubMeta(String pkg, String name, String header) {}
}
