/*
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

import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for RelationshipHandler default methods. */
class RelationshipHandlerTest {

    private RelationshipHandler handler;
    private FieldDescriptor fieldDescriptor;
    private ModelDescriptor modelDescriptor;

    @BeforeEach
    void setUp() {
        handler = new RelationshipHandler() {
            @Override
            public void addFix(ModelDescriptor md, FieldDescriptor fd, com.squareup.javapoet.MethodSpec.Builder fix, com.squareup.javapoet.ClassName entityType) {
            }

            @Override
            public void addClear(ModelDescriptor md, FieldDescriptor fd, com.squareup.javapoet.MethodSpec.Builder clear, com.squareup.javapoet.ClassName entityType) {
            }
        };
        fieldDescriptor = mock(FieldDescriptor.class);
        modelDescriptor = mock(ModelDescriptor.class);
    }

    @Test
    void relationshipFieldNameAppendsSuffix() {
        when(fieldDescriptor.getName()).thenReturn("author");
        assertEquals("author" + RelationshipHandler.RELATIONSHIP_FIELD_SUFFIX,
                handler.getRelationshipFieldName(fieldDescriptor));
    }

    @Test
    void inverseNameUsesMappedByWhenPresent() {
        when(fieldDescriptor.getName()).thenReturn("author");
        when(fieldDescriptor.getMappedBy()).thenReturn("book");
        assertEquals("author_book" + RelationshipHandler.RELATIONSHIP_FIELD_SUFFIX,
                handler.getInverseRelationshipFieldName(fieldDescriptor, modelDescriptor));
    }

    @Test
    void inverseNameDerivesWhenMappedByMissing() {
        when(fieldDescriptor.getName()).thenReturn("author");
        when(fieldDescriptor.getMappedBy()).thenReturn(null);
        when(modelDescriptor.getName()).thenReturn("Book");
        String expected = "author_books" + RelationshipHandler.RELATIONSHIP_FIELD_SUFFIX;
        assertEquals(expected,
                handler.getInverseRelationshipFieldName(fieldDescriptor, modelDescriptor));
    }
}
