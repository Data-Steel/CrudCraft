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
package nl.datasteel.crudcraft.projection.impl.jpa;

import jakarta.persistence.EntityManager;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaProjectionExecutorProviderTest {

    @Test
    void supportsWhenPredicateMissingOrNullQuery() {
        EntityManager em = mock(EntityManager.class);
        CriteriaProjectionBuilder builder = mock(CriteriaProjectionBuilder.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JpaProjectionExecutorProvider provider = new JpaProjectionExecutorProvider(em, builder, registry);

        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.empty());
        assertTrue(provider.supports(query));
        assertTrue(provider.supports(null));
    }

    @Test
    void doesNotSupportWhenPredicatePresent() {
        EntityManager em = mock(EntityManager.class);
        CriteriaProjectionBuilder builder = mock(CriteriaProjectionBuilder.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JpaProjectionExecutorProvider provider = new JpaProjectionExecutorProvider(em, builder, registry);

        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.of(mock(com.querydsl.core.types.Predicate.class)));
        assertFalse(provider.supports(query));
        assertNotNull(provider.getExecutor());
    }

    @Test
    void getExecutorReturnsSameInstance() {
        EntityManager em = mock(EntityManager.class);
        CriteriaProjectionBuilder builder = mock(CriteriaProjectionBuilder.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JpaProjectionExecutorProvider provider = new JpaProjectionExecutorProvider(em, builder, registry);
        assertSame(provider.getExecutor(), provider.getExecutor());
    }

    @Test
    void supportsIgnoresSpecification() {
        EntityManager em = mock(EntityManager.class);
        CriteriaProjectionBuilder builder = mock(CriteriaProjectionBuilder.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JpaProjectionExecutorProvider provider = new JpaProjectionExecutorProvider(em, builder, registry);

        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.empty());
        when(query.asSpecification()).thenReturn(Optional.of(mock(Specification.class)));
        assertTrue(provider.supports(query));
    }
}
