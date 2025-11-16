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
package nl.datasteel.crudcraft.projection.impl.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.metamodel.Metamodel;
import java.util.Optional;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.data.jpa.domain.Specification;

class QuerydslProjectionExecutorProviderTest {

    @Test
    void supportsWhenPredicatePresent() {
        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        Metamodel metamodel = mock(Metamodel.class);
        QuerydslProjectionExecutorProvider provider = new QuerydslProjectionExecutorProvider(factory, metamodel, builder, registry);
        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.of(mock(com.querydsl.core.types.Predicate.class)));
        assertTrue(provider.supports(query));
    }

    @Test
    void doesNotSupportWhenNoPredicate() {
        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        Metamodel metamodel = mock(Metamodel.class);
        QuerydslProjectionExecutorProvider provider = new QuerydslProjectionExecutorProvider(factory, metamodel, builder, registry);
        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.empty());
        assertFalse(provider.supports(query));
        assertNotNull(provider.getExecutor());
    }

    @Test
    void supportsNullQueryReturnsFalse() {
        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        Metamodel metamodel = mock(Metamodel.class);
        QuerydslProjectionExecutorProvider provider = new QuerydslProjectionExecutorProvider(factory, metamodel, builder, registry);
        assertFalse(provider.supports(null));
    }

    @Test
    void getExecutorReturnsSameInstance() {
        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        Metamodel metamodel = mock(Metamodel.class);
        QuerydslProjectionExecutorProvider provider = new QuerydslProjectionExecutorProvider(factory, metamodel, builder, registry);
        assertSame(provider.getExecutor(), provider.getExecutor());
    }

    @Test
    void supportsIgnoresSpecificationWhenPredicateMissing() {
        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        Metamodel metamodel = mock(Metamodel.class);
        QuerydslProjectionExecutorProvider provider = new QuerydslProjectionExecutorProvider(factory, metamodel, builder, registry);
        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.empty());
        when(query.asSpecification()).thenReturn(Optional.of(mock(Specification.class)));
        assertFalse(provider.supports(query));
    }
}
