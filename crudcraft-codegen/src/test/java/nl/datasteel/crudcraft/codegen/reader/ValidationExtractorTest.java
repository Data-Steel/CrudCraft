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
package nl.datasteel.crudcraft.codegen.reader;

import com.google.testing.compile.JavaFileObjects;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.reader.field.ValidationExtractor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ValidationExtractorTest {

    private static VariableElement firstField(TypeElement type) {
        return (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .findFirst().orElseThrow();
    }

    @Test
    void collectsCustomConstraintAnnotations() {
        String ann = "package t; import jakarta.validation.Constraint; import jakarta.validation.Payload; import java.lang.annotation.*; @Target({ElementType.FIELD}) @Retention(RetentionPolicy.RUNTIME) @Constraint(validatedBy={}) public @interface MyConstraint { String message() default \"\"; Class<?>[] groups() default {}; Class<? extends Payload>[] payload() default {}; }";
        String src = "package t; class C { @MyConstraint String f; }";
        Elements elements = CompilationTestUtils.elements(
                JavaFileObjects.forSourceString("t.MyConstraint", ann),
                JavaFileObjects.forSourceString("t.C", src));
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement f = firstField(type);
        var v = ValidationExtractor.INSTANCE.extract(f, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(1, v.getValidations().size());
        assertTrue(v.getValidations().get(0).toString().contains("MyConstraint"));
    }

    @Test
    void collectsBeanValidationAnnotations() {
        String src = "package t; import jakarta.validation.constraints.*; class C { @NotNull String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement f = firstField(type);
        var v = ValidationExtractor.INSTANCE.extract(f, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(1, v.getValidations().size());
        assertTrue(v.getValidations().get(0).toString().contains("NotNull"));
    }

    @Test
    void returnsEmptyWhenNoAnnotationsPresent() {
        String src = "package t; class C { String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement f = firstField(type);
        var v = ValidationExtractor.INSTANCE.extract(f, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(v.getValidations().isEmpty());
    }
}
