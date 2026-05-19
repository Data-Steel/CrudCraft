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

package nl.datasteel.crudcraft.codegen.golden;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.tools.JavaFileObject;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.CrudCraftProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GoldenEndpointPolicyMatrixCoverageTest {

    private static final String PACKAGE_NAME = "demo.golden.strictmatrix";
    private static final String INSOMNIA_OUTPUT_DIR =
            "-Acrudcraft.insomnia.outputDir=target/strict-golden-matrix-insomnia";
    private static final List<CrudTemplate> EXPECTED_TEMPLATES =
            List.of(
                    CrudTemplate.FULL,
                    CrudTemplate.READ_ONLY,
                    CrudTemplate.IMMUTABLE_WRITE,
                    CrudTemplate.PATCH_ONLY,
                    CrudTemplate.NO_DELETE,
                    CrudTemplate.NO_BATCH,
                    CrudTemplate.CREATE_ONLY,
                    CrudTemplate.SEARCH_ONLY,
                    CrudTemplate.META_ONLY,
                    CrudTemplate.LIGHT_PUBLIC,
                    CrudTemplate.SECURE_INTERNAL,
                    CrudTemplate.VALIDATION_ONLY);

    @Test
    void goldenEndpointMatrixEnumeratesEveryTemplateSecurityAndPolicyCombination()
            throws IOException {
        List<MatrixCase> matrix = matrixCases();
        Compilation compilation =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(new CrudCraftProcessor())
                        .compile(matrix.stream().map(MatrixCase::source).toList());

        assertEquals(
                Compilation.Status.SUCCESS,
                compilation.status(),
                () -> compilation.diagnostics().toString());
        assertTrue(compilation.warnings().isEmpty(), () -> compilation.warnings().toString());
        assertEquals(EXPECTED_TEMPLATES, Arrays.asList(CrudTemplate.values()));
        assertEquals(48, matrix.size());
        Set<String> matrixIds = new HashSet<>();
        matrix.forEach(matrixCase -> matrixIds.add(matrixCase.id()));
        assertEquals(48, matrixIds.size());

        for (MatrixCase matrixCase : matrix) {
            String controllerName =
                    PACKAGE_NAME + ".controller." + matrixCase.className() + "Controller";
            JavaFileObject controller =
                    compilation.generatedSourceFile(controllerName).orElseThrow();
            String source = controller.getCharContent(false).toString();
            if (matrixCase.secure()) {
                assertTrue(source.contains("@PreAuthorize"), matrixCase.id());
            } else {
                assertFalse(source.contains("@PreAuthorize"), matrixCase.id());
            }
            if (matrixCase.customPolicy()) {
                assertTrue(source.contains("@GetMapping(\"/count\")"), matrixCase.id());
            }
        }
    }

    private static List<MatrixCase> matrixCases() {
        List<MatrixCase> cases = new ArrayList<>();
        for (CrudTemplate template : EXPECTED_TEMPLATES) {
            for (boolean secure : List.of(false, true)) {
                for (boolean customPolicy : List.of(false, true)) {
                    cases.add(new MatrixCase(template, secure, customPolicy));
                }
            }
        }
        return cases;
    }

    private record MatrixCase(CrudTemplate template, boolean secure, boolean customPolicy) {
        String id() {
            return template.name() + " secure=" + secure + " customPolicy=" + customPolicy;
        }

        String className() {
            return camel(template.name())
                    + (secure ? "Secure" : "Open")
                    + (customPolicy ? "CustomPolicy" : "TemplatePolicy");
        }

        JavaFileObject source() {
            return JavaFileObjects.forSourceLines(
                    PACKAGE_NAME + "." + className(),
                    "package " + PACKAGE_NAME + ";",
                    "import jakarta.persistence.Entity;",
                    "import jakarta.persistence.Id;",
                    "import java.util.UUID;",
                    "import nl.datasteel.crudcraft.annotations.CrudTemplate;",
                    "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                    "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                    "import nl.datasteel.crudcraft.annotations.fields.Request;",
                    "import nl.datasteel.crudcraft.annotations.fields.Searchable;",
                    "import nl.datasteel.crudcraft.codegen.golden.fixture.MatrixEndpointPolicy;",
                    "import nl.datasteel.crudcraft.codegen.golden.fixture.MatrixSecurityPolicy;",
                    "@Entity",
                    annotation(),
                    "public class " + className() + " {",
                    "  @Id @Dto(ref = true) private UUID id;",
                    "  @Dto @Request @Searchable private String name;",
                    "}");
        }

        private String annotation() {
            List<String> attributes = new ArrayList<>();
            attributes.add("template = CrudTemplate." + template.name());
            if (secure) {
                attributes.add("secure = true");
                attributes.add("securityPolicy = MatrixSecurityPolicy.class");
            }
            if (customPolicy) {
                attributes.add("endpointPolicy = MatrixEndpointPolicy.class");
            }
            return "@CrudCrafted(" + String.join(", ", attributes) + ")";
        }
    }

    private static String camel(String value) {
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("_"))
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .reduce("", String::concat);
    }
}
