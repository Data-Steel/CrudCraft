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

package nl.datasteel.crudcraft.runtime.security.row;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class OwnerBasedRowSecurityTest {

    @Test
    void rowFilterReturnsDisjunctionWhenUserMissing() {
        OwnerBasedRowSecurity<TestEntity> handler = new OwnerBasedRowSecurity<>(() -> null);
        SpecificationState<TestEntity> state = SpecificationState.create("ownerId");

        Predicate result = handler.rowFilter().toPredicate(state.root, state.query, state.builder);

        assertSame(state.disjunction, result);
    }

    @Test
    void rowFilterReturnsOwnerEqualityWhenUserPresent() {
        UUID userId = UUID.randomUUID();
        OwnerBasedRowSecurity<TestEntity> handler = new OwnerBasedRowSecurity<>(() -> userId);
        SpecificationState<TestEntity> state = SpecificationState.create("ownerId");
        Predicate equalsPredicate = mock(Predicate.class);
        when(state.builder.equal(state.path, userId)).thenReturn(equalsPredicate);

        Predicate result = handler.rowFilter().toPredicate(state.root, state.query, state.builder);

        assertSame(equalsPredicate, result);
        verify(state.builder).equal(state.path, userId);
    }

    @Test
    void applySetsOwnerWhenMissing() {
        UUID userId = UUID.randomUUID();
        OwnerBasedRowSecurity<TestEntity> handler = new OwnerBasedRowSecurity<>(() -> userId);
        TestEntity entity = new TestEntity();

        handler.apply(entity);

        assertEquals(userId, entity.getOwnerId());
    }

    @Test
    void applyAllowsMatchingOwner() {
        UUID userId = UUID.randomUUID();
        OwnerBasedRowSecurity<TestEntity> handler = new OwnerBasedRowSecurity<>(() -> userId);
        TestEntity entity = new TestEntity();
        entity.setOwnerId(userId);

        handler.apply(entity);

        assertEquals(userId, entity.getOwnerId());
    }

    @Test
    void applyRejectsMismatchedOwner() {
        OwnerBasedRowSecurity<TestEntity> handler = new OwnerBasedRowSecurity<>(UUID::randomUUID);
        TestEntity entity = new TestEntity();
        entity.setOwnerId(UUID.randomUUID());

        assertThrows(
                nl.datasteel.crudcraft.runtime.security.AccessDeniedException.class,
                () -> handler.apply(entity));
    }

    @Test
    void applyFailsClosedWhenUserMissing() {
        OwnerBasedRowSecurity<TestEntity> handler = new OwnerBasedRowSecurity<>(() -> null);

        assertThrows(
                nl.datasteel.crudcraft.runtime.security.AccessDeniedException.class,
                () -> handler.apply(new TestEntity()));
    }

    @Test
    void applyFailsWhenOwnerFieldDoesNotExist() {
        OwnerBasedRowSecurity<TestEntity> handler =
                new OwnerBasedRowSecurity<>("missingOwnerId", UUID::randomUUID);

        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> handler.apply(new TestEntity()));

        assertEquals(
                "Scope field 'missingOwnerId' not found on "
                        + "nl.datasteel.crudcraft.runtime.security.row."
                        + "OwnerBasedRowSecurityTest$TestEntity",
                thrown.getMessage());
    }

    @Test
    void applyFailsWhenOwnerFieldNotWritable() {
        OwnerBasedRowSecurity<ReadOnlyEntity> handler =
                new OwnerBasedRowSecurity<>(UUID::randomUUID);

        assertThrows(IllegalStateException.class, () -> handler.apply(new ReadOnlyEntity()));
    }

    @Test
    void applyWrapsCheckedReflectionFailure() {
        OwnerBasedRowSecurity<CheckedThrowingEntity> handler =
                new OwnerBasedRowSecurity<>(UUID::randomUUID);

        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> handler.apply(new CheckedThrowingEntity()));

        assertEquals(
                java.lang.reflect.InvocationTargetException.class, thrown.getCause().getClass());
    }

    @Test
    void applyFailsWhenOwnerFieldHasNoGetter() {
        OwnerBasedRowSecurity<WriteOnlyOwnerEntity> handler =
                new OwnerBasedRowSecurity<>(UUID::randomUUID);

        assertThrows(IllegalStateException.class, () -> handler.apply(new WriteOnlyOwnerEntity()));
    }

    @Test
    void applyHandlesOwnerAccessorOnExternalEntity() {
        UUID userId = UUID.randomUUID();
        OwnerBasedRowSecurity<
                        nl.datasteel.crudcraft.runtime.security.row.fixture.DuplicateOwnerEntity>
                handler = new OwnerBasedRowSecurity<>(() -> userId);
        nl.datasteel.crudcraft.runtime.security.row.fixture.DuplicateOwnerEntity entity =
                new nl.datasteel.crudcraft.runtime.security.row.fixture.DuplicateOwnerEntity();

        handler.apply(entity);

        assertEquals(userId, entity.getOwnerId());
    }

    @Test
    void applyLeavesUnrelatedPropertiesUntouched() {
        UUID userId = UUID.randomUUID();
        OwnerBasedRowSecurity<
                        nl.datasteel.crudcraft.runtime.security.row.fixture.OrderedOwnerEntity>
                handler = new OwnerBasedRowSecurity<>(() -> userId);
        nl.datasteel.crudcraft.runtime.security.row.fixture.OrderedOwnerEntity entity =
                new nl.datasteel.crudcraft.runtime.security.row.fixture.OrderedOwnerEntity();
        entity.setAfterOwner("keep");

        handler.apply(entity);

        assertEquals(userId, entity.getOwnerId());
        assertEquals("keep", entity.getAfterOwner());
    }

    @Test
    void constructorsAndApplyValidateNullArguments() {
        assertThrows(
                NullPointerException.class,
                () ->
                        new OwnerBasedRowSecurity<TestEntity>(
                                (java.util.function.Supplier<UUID>) null));
        assertThrows(
                NullPointerException.class,
                () -> new OwnerBasedRowSecurity<TestEntity>(null, UUID::randomUUID));
        assertThrows(
                NullPointerException.class,
                () -> new OwnerBasedRowSecurity<TestEntity>("ownerId", null));

        OwnerBasedRowSecurity<TestEntity> handler = new OwnerBasedRowSecurity<>(UUID::randomUUID);
        assertThrows(NullPointerException.class, () -> handler.apply(null));
    }

    private static final class TestEntity {
        private UUID ownerId;

        public UUID getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(UUID ownerId) {
            this.ownerId = ownerId;
        }
    }

    private static final class ReadOnlyEntity {
        private UUID ownerId;

        public UUID getOwnerId() {
            return ownerId;
        }
    }

    private static final class CheckedThrowingEntity {
        public UUID getOwnerId() throws Exception {
            throw new Exception("boom");
        }

        public void setOwnerId(UUID ownerId) {
            if (ownerId != null) {
                ownerId.getLeastSignificantBits();
            }
        }
    }

    private static final class WriteOnlyOwnerEntity {
        private UUID ownerId;

        public void setOwnerId(UUID ownerId) {
            this.ownerId = ownerId;
        }
    }

    private static final class SpecificationState<T> {
        private final Root<T> root;
        private final CriteriaQuery<?> query;
        private final CriteriaBuilder builder;
        private final Path<Object> path;
        private final Predicate disjunction;

        private SpecificationState(
                Root<T> root,
                CriteriaQuery<?> query,
                CriteriaBuilder builder,
                Path<Object> path,
                Predicate disjunction) {
            this.root = root;
            this.query = query;
            this.builder = builder;
            this.path = path;
            this.disjunction = disjunction;
        }

        private static <T> SpecificationState<T> create(String field) {
            @SuppressWarnings("unchecked")
            Root<T> root = (Root<T>) mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder builder = mock(CriteriaBuilder.class);
            @SuppressWarnings("unchecked")
            Path<Object> path = (Path<Object>) mock(Path.class);
            Predicate disjunction = mock(Predicate.class);

            when(root.get(field)).thenReturn(path);
            when(builder.disjunction()).thenReturn(disjunction);

            return new SpecificationState<>(root, query, builder, path, disjunction);
        }
    }
}
