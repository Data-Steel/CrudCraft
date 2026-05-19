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

package nl.datasteel.crudcraft.runtime.export;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadataRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;


class EntitySerializerTest {

    @Test
    void serializeReturnsNullForNullEntity() {
        EntitySerializer serializer = new EntitySerializer(new EntityMetadataRegistry());

        assertNull(serializer.serialize(null, new ExportRequest()));
    }

    @Test
    void serializeSupportsIncludeExcludeAndDescendantFiltering() {
        EntitySerializer serializer = new EntitySerializer(new EntityMetadataRegistry());
        RootEntity entity = sampleEntity();

        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("name", "child.name", "tags", "address.street", "scores"));
        request.setExcludeFields(Set.of("child.secret"));
        request.setMaxDepth(2);

        Map<String, Object> result = serializer.serialize(entity, request);

        assertEquals("root", result.get("name"));
        assertTrue(result.containsKey("tags"));
        assertTrue(result.containsKey("scores"));
        assertTrue(result.containsKey("child"));
        assertTrue(result.containsKey("address"));
        assertFalse(result.containsKey("notIncluded"));

        @SuppressWarnings("unchecked")
        Map<String, Object> child = (Map<String, Object>) result.get("child");
        assertEquals("child", child.get("name"));
        assertFalse(child.containsKey("secret"));

        @SuppressWarnings("unchecked")
        List<Object> tags = (List<Object>) result.get("tags");
        assertFalse(tags.isEmpty());
        assertInstanceOf(Map.class, tags.getFirst());

        @SuppressWarnings("unchecked")
        Map<String, Object> address = (Map<String, Object>) result.get("address");
        assertEquals("Main", address.get("street"));
        assertFalse(address.containsKey("zip"));
    }

    @Test
    void serializeRespectsMaxDepthForNestedRelationships() {
        EntitySerializer serializer = new EntitySerializer(new EntityMetadataRegistry());
        RootEntity entity = sampleEntity();

        ExportRequest request = new ExportRequest();
        request.setIncludeFields(
                Set.of("child", "child.name", "child.grandChild", "child.grandChild.name"));
        request.setMaxDepth(1);

        Map<String, Object> result = serializer.serialize(entity, request);
        @SuppressWarnings("unchecked")
        Map<String, Object> child = (Map<String, Object>) result.get("child");

        assertTrue(child.containsKey("name"));
        assertFalse(child.containsKey("grandChild"));
    }

    @Test
    void serializeHandlesNullRelationshipAndNullCollectionItem() {
        EntitySerializer serializer = new EntitySerializer(new EntityMetadataRegistry());
        RootEntity entity = sampleEntity();
        entity.child = null;
        entity.tags = java.util.Arrays.asList((ChildEntity) null, sampleEntity().child);

        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("child.name", "tags", "tags.name"));
        request.setMaxDepth(0);

        Map<String, Object> result = serializer.serialize(entity, request);

        assertFalse(result.containsKey("child"));
        assertTrue(result.containsKey("tags"));
        @SuppressWarnings("unchecked")
        List<Object> tags = (List<Object>) result.get("tags");
        assertTrue(tags.contains(null));
    }

    @Test
    void serializeIncludesNullWhenFieldItselfIsIncluded() {
        EntitySerializer serializer = new EntitySerializer(new EntityMetadataRegistry());
        RootEntity entity = sampleEntity();
        entity.notIncluded = null;

        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("notIncluded"));

        Map<String, Object> result = serializer.serialize(entity, request);

        assertTrue(result.containsKey("notIncluded"));
        assertNull(result.get("notIncluded"));
    }

    @Test
    void serializeDoesNotInferDescendantsForScalarFieldNames() {
        EntitySerializer serializer = new EntitySerializer(new EntityMetadataRegistry());
        RootEntity entity = sampleEntity();
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("name.first"));

        Map<String, Object> result = serializer.serialize(entity, request);

        assertFalse(result.containsKey("name"));
    }

    @Test
    void serializeSkipsNullCollectionItemsFromDepthCappedEntities() {
        EntitySerializer serializer = new EntitySerializer(new EntityMetadataRegistry());
        RootEntity entity = sampleEntity();
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("tags", "tags.grandChild"));
        request.setMaxDepth(0);

        @SuppressWarnings("unchecked")
        List<Object> tags = (List<Object>) serializer.serialize(entity, request).get("tags");

        assertTrue(tags.isEmpty());
    }

    @Test
    void getFieldValueWrapsIllegalAccessException() throws Exception {
        EntitySerializer serializer = new EntitySerializer(new EntityMetadataRegistry());
        Method method =
                EntitySerializer.class.getDeclaredMethod(
                        "getFieldValue", Object.class, Field.class);
        method.setAccessible(true);

        Field mockedField = mock(Field.class);
        doThrow(new IllegalAccessException("denied")).when(mockedField).get(any());

        InvocationTargetException ex =
                assertThrows(
                        InvocationTargetException.class,
                        () -> method.invoke(serializer, sampleEntity(), mockedField));

        assertInstanceOf(RuntimeException.class, ex.getCause());
    }

    @Entity
    private static final class RootEntity {
        private String name;
        private String notIncluded;
        @ManyToOne private ChildEntity child;
        @Embedded private Address address;
        @OneToMany private List<ChildEntity> tags;
        @OneToMany private List<String> scores;
    }

    @Entity
    private static final class ChildEntity {
        private String name;
        private String secret;
        @ManyToOne private GrandChildEntity grandChild;
    }

    @Entity
    private static final class GrandChildEntity {
        private String name;
    }

    @Embeddable
    @Entity
    private static final class Address {
        private String street;
        private String zip;
    }

    private static RootEntity sampleEntity() {
        GrandChildEntity grandChild = new GrandChildEntity();
        grandChild.name = "grand";

        ChildEntity child = new ChildEntity();
        child.name = "child";
        child.secret = "s";
        child.grandChild = grandChild;

        Address address = new Address();
        address.street = "Main";
        address.zip = "1234";

        RootEntity root = new RootEntity();
        root.name = "root";
        root.notIncluded = "ignore";
        root.child = child;
        root.address = address;
        root.tags = List.of(child);
        root.scores = List.of("10", "20");
        return root;
    }
}
