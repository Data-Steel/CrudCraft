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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * Tests for DtoGenerator#idFieldName focusing on irregular plurals.
 */
class DtoGeneratorIdFieldNameTest {

    private FieldDescriptor field(String name, RelationshipType relType) {
        Identity identity = new Identity(name, null, null, SchemaMetadata.empty());
        DtoOptions dto = new DtoOptions(true, true, true, new String[0]);
        EnumOptions en = new EnumOptions(false, null);
        Relationship rel = new Relationship(relType, "", "", false, false);
        Validation val = new Validation(null);
        SearchOptions search = new SearchOptions(false, null, 0);
        Security sec = new Security(false, null, null);
        return new FieldDescriptor(identity, dto, en, rel, val, search, sec);
    }

    private String idFieldNameCollection(String fieldName) throws Exception {
        FieldDescriptor fd = field(fieldName, RelationshipType.ONE_TO_MANY);
        TypeName listType = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class));
        Method m = DtoGenerator.class.getDeclaredMethod("idFieldName", FieldDescriptor.class, TypeName.class);
        m.setAccessible(true);
        return (String) m.invoke(new DtoGenerator(), fd, listType);
    }

    private String idFieldNameSingle(String fieldName) throws Exception {
        FieldDescriptor fd = field(fieldName, RelationshipType.MANY_TO_ONE);
        Method m = DtoGenerator.class.getDeclaredMethod("idFieldName", FieldDescriptor.class, TypeName.class);
        m.setAccessible(true);
        return (String) m.invoke(new DtoGenerator(), fd, ClassName.get(UUID.class));
    }

    @Test
    void singularizesCities() throws Exception {
        assertEquals("cityIds", idFieldNameCollection("cities"));
    }

    @Test
    void singularizesIndices() throws Exception {
        assertEquals("indexIds", idFieldNameCollection("indices"));
    }

    @Test
    void singleRelationUsesIdSuffix() throws Exception {
        assertEquals("authorId", idFieldNameSingle("author"));
    }
}
