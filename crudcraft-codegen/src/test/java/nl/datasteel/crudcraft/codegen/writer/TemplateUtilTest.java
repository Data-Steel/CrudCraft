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
package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.AnnotationSpec;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/** Tests for TemplateUtil helper methods. */
class TemplateUtilTest {

    @Test
    void schemaCreatesAnnotationWithDescription() {
        AnnotationSpec spec = TemplateUtil.schema("desc");
        assertEquals("io.swagger.v3.oas.annotations.media.Schema", spec.type.toString());
        assertTrue(spec.members.get("description").toString().contains("desc"));
    }

    @Test
    void schemaAllowableJoinsValues() {
        AnnotationSpec spec = TemplateUtil.schemaAllowable(List.of("A", "B"));
        assertTrue(spec.members.get("allowableValues").toString().contains("\"A\""));
        assertTrue(spec.members.get("allowableValues").toString().contains("\"B\""));
    }

    @Test
    void schemaAllowableHandlesEmptyList() {
        AnnotationSpec spec = TemplateUtil.schemaAllowable(List.of());
        assertEquals("{}", spec.members.get("allowableValues").get(0).toString());
    }
}
