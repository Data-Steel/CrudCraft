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
package nl.datasteel.crudcraft.codegen.writer.relationship;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for OneToOneHandler fix and clear logic. */
class OneToOneHandlerTest {

    private final OneToOneHandler handler = new OneToOneHandler();

    @Test
    void addFixSetsInverseField() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        FieldDescriptor fd = mock(FieldDescriptor.class);
        when(fd.getName()).thenReturn("author");
        when(fd.getTargetType()).thenReturn("com.example.Author");
        when(fd.getMappedBy()).thenReturn(null);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("fix");
        handler.addFix(md, fd, builder, ClassName.get("com.example", "Book"));
        String code = builder.build().toString();
        assertTrue(code.contains("author_booksField.set(child, entity)"));
    }

    @Test
    void addClearNullifiesInverseField() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        FieldDescriptor fd = mock(FieldDescriptor.class);
        when(fd.getName()).thenReturn("author");
        when(fd.getTargetType()).thenReturn("com.example.Author");
        when(fd.getMappedBy()).thenReturn("book");

        MethodSpec.Builder builder = MethodSpec.methodBuilder("clear");
        handler.addClear(md, fd, builder, ClassName.get("com.example", "Book"));
        String code = builder.build().toString();
        assertTrue(code.contains("author_bookField.set(child, null)"));
    }
}
