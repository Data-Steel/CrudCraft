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

import java.lang.reflect.Method;
import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class StubGeneratorUtilTest {

    private ModelDescriptor model(boolean editable) {
        ModelIdentity id = new ModelIdentity("User", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(editable, true, false, false);
        EndpointOptions options = new EndpointOptions(null, null, null, null);
        ModelSecurity security = new ModelSecurity(false, null, List.of());
        return new ModelDescriptor(id, flags, options, security);
    }

    @Test
    void stubMetaUsesEditableHeaderWhenModelEditable() {
        StubGeneratorUtil.StubMeta meta =
                StubGeneratorUtil.stubMeta(
                        model(true), "service", "Service", "Service", StubGeneratorUtilTest.class);
        assertEquals("com.example.service", meta.pkg());
        assertEquals("UserService", meta.name());
        assertTrue(meta.header().contains("Override protected hooks"));
    }

    @Test
    void stubMetaUsesStrictHeaderWhenModelNotEditable() {
        StubGeneratorUtil.StubMeta meta =
                StubGeneratorUtil.stubMeta(
                        model(false), "service", "Service", "Service", StubGeneratorUtilTest.class);
        assertTrue(meta.header().contains("default implementation"));
    }

    @Test
    void licenseHeaderLoadsAndNormalizesConfiguredTemplate() throws Exception {
        String previous = System.getProperty("maven.multiModuleProjectDirectory");
        java.nio.file.Path base = java.nio.file.Files.createTempDirectory("license-template");
        java.nio.file.Files.writeString(
                base.resolve(".license-header"),
                "/*\n * Copyright (c) YEAR OWNER\n */\nBody",
                java.nio.charset.StandardCharsets.UTF_8);
        String loaded;
        try {
            loaded = invokeLoadLicenseHeader(base.toString());
            assertTrue(StubGeneratorUtil.licenseHeader() != null);
            assertTrue(!StubGeneratorUtil.licenseHeader().isEmpty());
        } finally {
            restoreMultiModuleProjectDirectory(previous);
        }

        assertTrue(loaded.contains("CrudCraft contributors"));
        assertTrue(loaded.contains(String.valueOf(java.time.Year.now().getValue())));
        assertTrue(!loaded.contains("/*"));
        assertTrue(!loaded.contains("*/"));
        assertTrue(!loaded.contains(" * Copyright"));
    }

    @Test
    void licenseHeaderFallsBackToEmptyStringWhenTemplateIsMissing() throws Exception {
        String previous = System.getProperty("maven.multiModuleProjectDirectory");
        java.nio.file.Path missingBase = java.nio.file.Files.createTempDirectory("missing-license");
        try {
            System.setProperty("maven.multiModuleProjectDirectory", missingBase.toString());
            assertEquals("", invokeLoadLicenseHeader(missingBase.toString()));
        } finally {
            restoreMultiModuleProjectDirectory(previous);
        }
    }

    private static void restoreMultiModuleProjectDirectory(String previous) {
        if (previous == null) {
            System.clearProperty("maven.multiModuleProjectDirectory");
        } else {
            System.setProperty("maven.multiModuleProjectDirectory", previous);
        }
    }

    private static String invokeLoadLicenseHeader(String baseDir) throws Exception {
        if (baseDir != null) {
            System.setProperty("maven.multiModuleProjectDirectory", baseDir);
        }
        Method method = StubGeneratorUtil.class.getDeclaredMethod("loadLicenseHeader");
        method.setAccessible(true);
        return (String) method.invoke(null);
    }
}
