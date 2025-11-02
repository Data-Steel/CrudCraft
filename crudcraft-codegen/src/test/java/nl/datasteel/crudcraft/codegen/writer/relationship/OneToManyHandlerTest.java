/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen.writer.relationship;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for OneToManyHandler fix and clear logic. */
class OneToManyHandlerTest {

    private final OneToManyHandler handler = new OneToManyHandler();

    private FieldDescriptor mockField(String mappedBy, RelationshipType relType) {
        FieldDescriptor fd = mock(FieldDescriptor.class);
        when(fd.getName()).thenReturn("authors");
        when(fd.getTargetType()).thenReturn("com.example.Author");
        when(fd.getMappedBy()).thenReturn(mappedBy);
        when(fd.getRelType()).thenReturn(relType);
        return fd;
    }

    @Test
    void addFixSetsEntityOnInverseSingleReference() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        FieldDescriptor fd = mockField(null, RelationshipType.ONE_TO_MANY);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("fix");
        handler.addFix(md, fd, builder, ClassName.get("com.example", "Book"));
        String code = builder.build().toString();
        assertTrue(code.contains(".set(authorsChild, entity)"));
    }

    @Test
    void addClearSetsNullOnInverseSingleReference() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        FieldDescriptor fd = mockField("book", RelationshipType.ONE_TO_MANY);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("clear");
        handler.addClear(md, fd, builder, ClassName.get("com.example", "Book"));
        String code = builder.build().toString();
        assertTrue(code.contains(".set(authorsChild, null)"));
    }

    @Test
    void addFixAddsEntityToInverseCollectionForManyToMany() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        FieldDescriptor fd = mockField(null, RelationshipType.MANY_TO_MANY);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("fix");
        handler.addFix(md, fd, builder, ClassName.get("com.example", "Book"));
        String code = builder.build().toString();
        assertTrue(code.contains("authorsInv.add(entity)"));
    }

    @Test
    void addClearRemovesEntityFromInverseCollectionForManyToMany() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        FieldDescriptor fd = mockField("book", RelationshipType.MANY_TO_MANY);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("clear");
        handler.addClear(md, fd, builder, ClassName.get("com.example", "Book"));
        String code = builder.build().toString();
        assertTrue(code.contains("authorsInv.remove(entity)"));
    }
}
