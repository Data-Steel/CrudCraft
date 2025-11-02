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
package nl.datasteel.crudcraft.runtime.security.row;

import java.util.UUID;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import nl.datasteel.crudcraft.runtime.security.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OwnerBasedRowSecurityTest {

    // --- Test fixtures -------------------------------------------------------

    static class Entity {
        private UUID ownerId;
        public UUID getOwnerId() { return ownerId; }
        public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    }

    static class NoOwnerEntity { }

    static class MissingSetterEntity {
        private UUID ownerId;
        public UUID getOwnerId() { return ownerId; }
        // no setter
    }

    static class MissingGetterEntity {
        private UUID ownerId;
        // no getter
        public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    }

    static class CustomEntity {
        private UUID creatorId;
        public UUID getCreatorId() { return creatorId; }
        public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }
    }

    // --- apply(): behavior & exception mapping --------------------------------

    @Test
    void applySetsOwnerWhenNull() {
        UUID user = UUID.randomUUID();
        var security = new OwnerBasedRowSecurity<Entity>(() -> user);
        var entity = new Entity();

        security.apply(entity);

        assertEquals(user, entity.getOwnerId());
    }

    @Test
    void applyDoesNothingWhenOwnershipMatches() {
        UUID user = UUID.randomUUID();
        var security = new OwnerBasedRowSecurity<Entity>(() -> user);
        var entity = new Entity();
        entity.setOwnerId(user);

        // must not throw
        assertDoesNotThrow(() -> security.apply(entity));
        // must not change
        assertEquals(user, entity.getOwnerId());
    }

    @Test
    void applyThrowsWhenUserMissing() {
        var security = new OwnerBasedRowSecurity<Entity>(() -> null);
        var entity = new Entity();

        assertThrows(AccessDeniedException.class, () -> security.apply(entity));
    }

    @Test
    void applyThrowsWhenOwnershipMismatch_andIsNotWrapped() {
        UUID user = UUID.randomUUID();
        var security = new OwnerBasedRowSecurity<Entity>(() -> user);
        var entity = new Entity();
        entity.setOwnerId(UUID.randomUUID());

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> security.apply(entity));
        // ensure it's not wrapped by IllegalStateException
        assertNull(ex.getCause(), "AccessDeniedException should not be wrapped");
    }

    @Test
    void applyThrowsWhenFieldMissing_andIsWrappedWithNoSuchFieldCause() {
        UUID user = UUID.randomUUID();
        var security = new OwnerBasedRowSecurity<NoOwnerEntity>(() -> user);
        var entity = new NoOwnerEntity();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> security.apply(entity));
        assertNotNull(ex.getCause(), "Expected cause to be present");
        assertEquals(NoSuchFieldException.class, ex.getCause().getClass(),
                "Missing field must be wrapped with NoSuchFieldException as cause");
    }

    @Test
    void applyThrowsWhenSetterMissing_andIsNotWrapped() {
        UUID user = UUID.randomUUID();
        var security = new OwnerBasedRowSecurity<MissingSetterEntity>(() -> user);
        var entity = new MissingSetterEntity();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> security.apply(entity));
        // thrown directly by ensureOwnership (not wrapped by apply)
        assertNull(ex.getCause(), "Missing setter should throw IllegalStateException directly");
    }

    @Test
    void applyThrowsWhenGetterMissing_andIsNotWrapped() {
        UUID user = UUID.randomUUID();
        var security = new OwnerBasedRowSecurity<MissingGetterEntity>(() -> user);
        var entity = new MissingGetterEntity();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> security.apply(entity));
        // thrown directly by ensureOwnership (not wrapped by apply)
        assertNull(ex.getCause(), "Missing getter should throw IllegalStateException directly");
    }

    @Test
    void applySupportsCustomFieldName() {
        UUID user = UUID.randomUUID();
        var security = new OwnerBasedRowSecurity<CustomEntity>("creatorId", () -> user);
        var entity = new CustomEntity();

        security.apply(entity);

        assertEquals(user, entity.getCreatorId());
    }

    // --- rowFilter(): behavior ------------------------------------------------

    @Test
    void rowFilterReturnsDisjunctionWhenNoUser() {
        var security = new OwnerBasedRowSecurity<Entity>(() -> null);
        var spec = security.rowFilter();

        @SuppressWarnings("unchecked")
        Root<Entity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate disjunction = mock(Predicate.class);

        when(cb.disjunction()).thenReturn(disjunction);

        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(disjunction, result);
    }

    @Test
    void rowFilterUsesOwnerFieldWhenUserPresent() {
        UUID user = UUID.randomUUID();
        var security = new OwnerBasedRowSecurity<Entity>(() -> user);
        var spec = security.rowFilter();

        @SuppressWarnings("unchecked")
        Root<Entity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<Object> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("ownerId")).thenReturn(path);
        when(cb.equal(path, user)).thenReturn(predicate);

        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(root).get("ownerId");
        verify(cb).equal(path, user);
    }

    @Test
    void rowFilterUsesCustomOwnerField() {
        UUID user = UUID.randomUUID();
        var security = new OwnerBasedRowSecurity<CustomEntity>("creatorId", () -> user);
        var spec = security.rowFilter();

        @SuppressWarnings("unchecked")
        Root<CustomEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<Object> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("creatorId")).thenReturn(path);
        when(cb.equal(path, user)).thenReturn(predicate);

        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(root).get("creatorId");
        verify(cb).equal(path, user);
    }
}
