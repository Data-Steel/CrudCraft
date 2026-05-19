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

package nl.datasteel.crudcraft.codegen.writer;

import com.palantir.javapoet.AnnotationSpec;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/** Tests for TemplateUtil helper methods. */
class TemplateUtilTest {

    @Test
    void privateConstructorIsCoveredForStrictMutationLineCoverage() throws Exception {
        var constructor = TemplateUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    @Test
    void schemaCreatesAnnotationWithDescription() {
        AnnotationSpec spec = TemplateUtil.schema("desc");
        assertEquals("io.swagger.v3.oas.annotations.media.Schema", spec.type().toString());
        assertTrue(spec.members().get("description").toString().contains("desc"));
    }

    @Test
    void schemaAllowableJoinsValues() {
        AnnotationSpec spec = TemplateUtil.schemaAllowable(List.of("A", "B"));
        assertTrue(spec.members().get("allowableValues").toString().contains("\"A\""));
        assertTrue(spec.members().get("allowableValues").toString().contains("\"B\""));
    }

    @Test
    void schemaAllowableHandlesEmptyList() {
        AnnotationSpec spec = TemplateUtil.schemaAllowable(List.of());
        assertEquals("{}", spec.members().get("allowableValues").get(0).toString());
    }

    @Test
    void schemaForFieldCleansDescriptionAndSetsNullable() {
        String javadoc = "  Name line  \n@param ignored value\n";
        AnnotationSpec spec = TemplateUtil.schemaForField(javadoc, true);

        assertTrue(spec.members().get("description").toString().contains("Name line"));
        assertTrue(spec.members().get("nullable").toString().contains("true"));
    }

    @Test
    void schemaForFieldOmitsEmptyDescriptionAndNullableFalse() {
        AnnotationSpec spec = TemplateUtil.schemaForField("   ", false);
        assertTrue(spec.members().isEmpty());
    }

    @Test
    void schemaForFieldOmitsNullDescription() {
        AnnotationSpec spec = TemplateUtil.schemaForField(null, false);
        assertTrue(spec.members().isEmpty());
    }

    @Test
    void schemaForEnumEscapesValuesAndIncludesNullable() {
        AnnotationSpec spec = TemplateUtil.schemaForEnum("Status", List.of("A", "Q\"uote"), true);
        String allowable = spec.members().get("allowableValues").get(0).toString();

        assertTrue(allowable.contains("\"A\""));
        assertTrue(allowable.contains("\\\""));
        assertTrue(spec.members().get("description").toString().contains("Status"));
        assertTrue(spec.members().get("nullable").toString().contains("true"));
    }

    @Test
    void schemaFromMetadataReturnsNullWhenNoMetadataAndNoJavadoc() {
        AnnotationSpec spec = TemplateUtil.schemaFromMetadata("   ", SchemaMetadata.empty(), false);
        assertNull(spec);
    }

    @Test
    void schemaFromMetadataReturnsNullWhenNoMetadataAndNullJavadoc() {
        AnnotationSpec spec = TemplateUtil.schemaFromMetadata(null, SchemaMetadata.empty(), false);
        assertNull(spec);
    }

    @Test
    void schemaFromMetadataUsesMetadataDescriptionAndProperties() {
        Map<String, Object> additional = new LinkedHashMap<>();
        additional.put("requiredMode", "REQUIRED");
        additional.put("deprecated", true);
        additional.put("maxLength", 32);
        additional.put("allowableValues", List.of("OPEN", "CLOSED"));
        SchemaMetadata metadata = new SchemaMetadata("Schema description", "42", additional);

        AnnotationSpec spec = TemplateUtil.schemaFromMetadata("fallback", metadata, true);

        assertTrue(spec.members().get("description").toString().contains("Schema description"));
        assertTrue(spec.members().get("example").toString().contains("42"));
        assertTrue(spec.members().get("requiredMode").toString().contains("REQUIRED"));
        assertTrue(spec.members().get("deprecated").toString().contains("true"));
        assertTrue(spec.members().get("maxLength").toString().contains("32"));
        assertTrue(spec.members().get("allowableValues").toString().contains("\"OPEN\""));
        assertTrue(spec.members().get("nullable").toString().contains("true"));
    }

    @Test
    void schemaFromMetadataFallsBackToJavadocWhenDescriptionMissing() {
        SchemaMetadata metadata = new SchemaMetadata(" ", null, Map.of());

        AnnotationSpec spec =
                TemplateUtil.schemaFromMetadata("Line 1\n@return ignored", metadata, false);

        assertTrue(spec.members().get("description").toString().contains("Line 1"));
    }

    @Test
    void schemaFromMetadataSkipsUnsupportedAndEmptyListProperties() {
        Map<String, Object> additional = new LinkedHashMap<>();
        additional.put("allowableValues", List.of());
        additional.put("vendorExtension", Map.of("x", "y"));
        SchemaMetadata metadata = new SchemaMetadata("desc", null, additional);

        AnnotationSpec spec = TemplateUtil.schemaFromMetadata("fallback", metadata, false);

        assertTrue(spec.members().get("description").toString().contains("desc"));
        assertNull(spec.members().get("allowableValues"));
        assertNull(spec.members().get("vendorExtension"));
    }

    @Test
    void schemaForEnumHandlesBlankDescriptionAndNonNullable() {
        AnnotationSpec spec = TemplateUtil.schemaForEnum("   ", List.of("A"), false);
        assertNull(spec.members().get("description"));
        assertNull(spec.members().get("nullable"));
        assertTrue(spec.members().get("allowableValues").toString().contains("\"A\""));
    }

    @Test
    void schemaForEnumHandlesNullDescription() {
        AnnotationSpec spec = TemplateUtil.schemaForEnum(null, List.of("A"), false);
        assertNull(spec.members().get("description"));
        assertTrue(spec.members().get("allowableValues").toString().contains("\"A\""));
    }

    @Test
    void schemaFromMetadataCanContainOnlyExample() {
        SchemaMetadata metadata = new SchemaMetadata(null, "example", Map.of());

        AnnotationSpec spec = TemplateUtil.schemaFromMetadata(null, metadata, false);

        assertNull(spec.members().get("description"));
        assertTrue(spec.members().get("example").toString().contains("example"));
    }

    @Test
    void schemaFromMetadataEscapesListValues() {
        Map<String, Object> additional = new LinkedHashMap<>();
        additional.put("allowableValues", List.of("Q\"uote"));
        SchemaMetadata metadata = new SchemaMetadata("desc", null, additional);

        AnnotationSpec spec = TemplateUtil.schemaFromMetadata(null, metadata, false);

        assertTrue(spec.members().get("allowableValues").toString().contains("\\\""));
    }

    @Test
    void cleanJavadocHandlesNullDirectly() throws Exception {
        Method method = TemplateUtil.class.getDeclaredMethod("cleanJavadoc", String.class);
        method.setAccessible(true);

        assertNull(method.invoke(null, new Object[] {null}));
    }
}
