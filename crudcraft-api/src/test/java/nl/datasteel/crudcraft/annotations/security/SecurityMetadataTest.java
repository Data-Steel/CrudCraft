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

package nl.datasteel.crudcraft.annotations.security;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SecurityMetadataTest {

    @Test
    void fieldRuleNormalizesNullCollectionsAndDefaultsWritePolicy() {
        FieldSecurityMetadata.FieldRule<TestEntity> rule =
                new FieldSecurityMetadata.FieldRule<>(
                        "name",
                        entity -> entity.name,
                        (entity, value) -> entity.name = (String) value,
                        true,
                        null,
                        List.of("writer"),
                        null);

        assertTrue(rule.readRoles().isEmpty());
        assertEquals(List.of("writer"), rule.writeRoles());
        assertEquals(WritePolicy.SKIP_ON_DENIED, rule.writePolicy());
        assertThrows(UnsupportedOperationException.class, () -> rule.writeRoles().add("extra"));
    }

    @Test
    void fieldRuleRejectsNullNameAndReader() {
        assertThrows(
                NullPointerException.class,
                () ->
                        new FieldSecurityMetadata.FieldRule<TestEntity>(
                                null,
                                entity -> entity.name,
                                (entity, value) -> entity.name = (String) value,
                                false,
                                List.of(),
                                List.of(),
                                WritePolicy.FAIL_ON_DENIED));

        assertThrows(
                NullPointerException.class,
                () ->
                        new FieldSecurityMetadata.FieldRule<TestEntity>(
                                "name",
                                null,
                                (entity, value) -> entity.name = (String) value,
                                false,
                                List.of(),
                                List.of(),
                                WritePolicy.FAIL_ON_DENIED));
    }

    @Test
    void fieldRuleHandlesNullWriterAndNullWriteRoles() {
        FieldSecurityMetadata.FieldRule<TestEntity> rule =
                new FieldSecurityMetadata.FieldRule<>(
                        "status",
                        entity -> entity.status,
                        null,
                        true,
                        List.of("reader"),
                        null,
                        WritePolicy.FAIL_ON_DENIED);

        assertNull(rule.writer());
        assertTrue(rule.writeRoles().isEmpty());
    }

    @Test
    void fieldRuleRejectsRoleListsContainingNull() {
        assertThrows(
                NullPointerException.class,
                () ->
                        new FieldSecurityMetadata.FieldRule<TestEntity>(
                                "name",
                                entity -> entity.name,
                                (entity, value) -> entity.name = (String) value,
                                false,
                                List.of("reader", null),
                                List.of(),
                                WritePolicy.SKIP_ON_DENIED));

        assertThrows(
                NullPointerException.class,
                () ->
                        new FieldSecurityMetadata.FieldRule<TestEntity>(
                                "name",
                                entity -> entity.name,
                                (entity, value) -> entity.name = (String) value,
                                false,
                                List.of(),
                                List.of("writer", null),
                                WritePolicy.SKIP_ON_DENIED));
    }

    @Test
    void metadataCopiesInputAndHandlesNullInput() {
        FieldSecurityMetadata.FieldRule<TestEntity> rule =
                new FieldSecurityMetadata.FieldRule<>(
                        "name",
                        entity -> entity.name,
                        (entity, value) -> entity.name = (String) value,
                        false,
                        List.of("reader"),
                        List.of("writer"),
                        WritePolicy.FAIL_ON_DENIED);
        List<FieldSecurityMetadata.FieldRule<TestEntity>> input = new ArrayList<>(List.of(rule));

        FieldSecurityMetadata<TestEntity> metadata = FieldSecurityMetadata.of(input);
        input.add(
                new FieldSecurityMetadata.FieldRule<>(
                        "status",
                        entity -> entity.status,
                        (entity, value) -> entity.status = (String) value,
                        false,
                        List.of(),
                        List.of(),
                        WritePolicy.SKIP_ON_DENIED));

        assertEquals(1, metadata.fields().size());
        assertEquals(rule, metadata.fields().get(0));
        assertThrows(UnsupportedOperationException.class, () -> metadata.fields().add(rule));

        FieldSecurityMetadata<TestEntity> empty = FieldSecurityMetadata.of(null);
        assertTrue(empty.fields().isEmpty());
    }

    @Test
    void metadataRejectsNullFieldRuleEntries() {
        assertThrows(
                NullPointerException.class,
                () ->
                        FieldSecurityMetadata.of(
                                List.of((FieldSecurityMetadata.FieldRule<TestEntity>) null)));
    }

    @Test
    void fieldRuleRecordMethodsCoverEqualityBranches() {
        FieldSecurityMetadata.FieldRule<TestEntity> rule =
                new FieldSecurityMetadata.FieldRule<>(
                        "name",
                        entity -> entity.name,
                        (entity, value) -> entity.name = (String) value,
                        false,
                        List.of("reader"),
                        List.of("writer"),
                        WritePolicy.FAIL_ON_DENIED);
        FieldSecurityMetadata.FieldRule<TestEntity> sameValues =
                new FieldSecurityMetadata.FieldRule<>(
                        "name",
                        entity -> entity.name,
                        (entity, value) -> entity.name = (String) value,
                        false,
                        List.of("reader"),
                        List.of("writer"),
                        WritePolicy.FAIL_ON_DENIED);
        FieldSecurityMetadata.FieldRule<TestEntity> differentName =
                new FieldSecurityMetadata.FieldRule<>(
                        "status",
                        entity -> entity.status,
                        (entity, value) -> entity.status = (String) value,
                        false,
                        List.of("reader"),
                        List.of("writer"),
                        WritePolicy.FAIL_ON_DENIED);

        assertEquals(rule, rule);
        assertNotEquals(rule, null);
        assertNotEquals(rule, new Object());
        assertNotEquals(rule, sameValues);
        assertNotEquals(rule, differentName);
        assertNotEquals(rule.hashCode(), differentName.hashCode());
        assertTrue(rule.toString().contains("name"));
    }

    @Test
    void adapterAndHandlerDefaultsAreNoOps() {
        FieldSecurityAdapter adapter = FieldSecurityAdapter.NOOP;
        TestEntity entity = new TestEntity("before", "draft");

        assertEquals(entity, adapter.filterRead(entity));
        assertEquals(entity, adapter.filterWrite(entity, new TestEntity("existing", "saved")));
        assertTrue(adapter.canReadField(TestEntity.class, "name"));

        RowSecurityHandler<TestEntity> handler = () -> null;
        assertNull(handler.rowFilter());
        assertDoesNotThrow(() -> handler.apply(entity));
    }

    @Test
    void fieldSecurityAnnotationDefaultsAreStable() throws NoSuchMethodException {
        assertArrayEquals(
                new String[] {"ALL"},
                (String[]) FieldSecurity.class.getMethod("readRoles").getDefaultValue());
        assertArrayEquals(
                new String[] {"ALL"},
                (String[]) FieldSecurity.class.getMethod("writeRoles").getDefaultValue());
        assertEquals(
                WritePolicy.SKIP_ON_DENIED,
                FieldSecurity.class.getMethod("writePolicy").getDefaultValue());
    }

    private static final class TestEntity {
        private String name;
        private String status;

        private TestEntity(String name, String status) {
            this.name = name;
            this.status = status;
        }
    }
}
