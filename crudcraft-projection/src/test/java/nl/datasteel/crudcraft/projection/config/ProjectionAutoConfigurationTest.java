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
package nl.datasteel.crudcraft.projection.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Metamodel;
import java.util.List;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.impl.RoutingProjectionExecutor;
import nl.datasteel.crudcraft.projection.impl.jpa.CriteriaProjectionBuilder;
import nl.datasteel.crudcraft.projection.impl.jpa.JpaProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.impl.querydsl.QuerydslProjectionBuilder;
import nl.datasteel.crudcraft.projection.impl.querydsl.QuerydslProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.mapping.SimpleProjectionMetadataRegistry;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProjectionAutoConfigurationTest {

    @Test
    void jpaProjectionExecutorProviderReturnsProviderWhenEngineMatches() {
        ProjectionAutoConfiguration cfg = new ProjectionAutoConfiguration();
        EntityManager em = Mockito.mock(EntityManager.class);
        CriteriaProjectionBuilder builder = Mockito.mock(CriteriaProjectionBuilder.class);
        ProjectionMetadataRegistry registry = Mockito.mock(ProjectionMetadataRegistry.class);
        ProjectionProperties props = new ProjectionProperties();
        props.setEngine(ProjectionProperties.Engine.CRITERIA);
        ProjectionExecutorProvider provider = cfg.jpaProjectionExecutorProvider(em, builder, registry, props);
        assertTrue(provider instanceof JpaProjectionExecutorProvider);
    }

    @Test
    void jpaProjectionExecutorProviderThrowsOnEngineMismatch() {
        ProjectionAutoConfiguration cfg = new ProjectionAutoConfiguration();
        EntityManager em = Mockito.mock(EntityManager.class);
        CriteriaProjectionBuilder builder = Mockito.mock(CriteriaProjectionBuilder.class);
        ProjectionMetadataRegistry registry = Mockito.mock(ProjectionMetadataRegistry.class);
        ProjectionProperties props = new ProjectionProperties();
        props.setEngine(ProjectionProperties.Engine.QUERYDSL);
        assertThrows(IllegalStateException.class, () -> cfg.jpaProjectionExecutorProvider(em, builder, registry, props));
    }

    @Test
    void querydslProjectionExecutorProviderReturnsProviderWhenEngineMatches() {
        ProjectionAutoConfiguration cfg = new ProjectionAutoConfiguration();
        JPAQueryFactory qf = Mockito.mock(JPAQueryFactory.class);
        EntityManager em = Mockito.mock(EntityManager.class);
        Metamodel metamodel = Mockito.mock(Metamodel.class);
        Mockito.when(em.getMetamodel()).thenReturn(metamodel);
        QuerydslProjectionBuilder builder = Mockito.mock(QuerydslProjectionBuilder.class);
        ProjectionMetadataRegistry registry = Mockito.mock(ProjectionMetadataRegistry.class);
        ProjectionProperties props = new ProjectionProperties();
        props.setEngine(ProjectionProperties.Engine.QUERYDSL);
        ProjectionExecutorProvider provider = cfg.querydslProjectionExecutorProvider(qf, em, builder, registry, props);
        assertTrue(provider instanceof QuerydslProjectionExecutorProvider);
    }

    @Test
    void querydslProjectionExecutorProviderThrowsOnEngineMismatch() {
        ProjectionAutoConfiguration cfg = new ProjectionAutoConfiguration();
        EntityManager em = Mockito.mock(EntityManager.class);
        JPAQueryFactory qf = Mockito.mock(JPAQueryFactory.class);
        QuerydslProjectionBuilder builder = Mockito.mock(QuerydslProjectionBuilder.class);
        ProjectionMetadataRegistry registry = Mockito.mock(ProjectionMetadataRegistry.class);
        ProjectionProperties props = new ProjectionProperties();
        props.setEngine(ProjectionProperties.Engine.CRITERIA);
        assertThrows(IllegalStateException.class, ()
                -> cfg.querydslProjectionExecutorProvider(qf, em, builder, registry, props));
    }

    @Test
    void projectionExecutorReturnsSingleExecutor() {
        ProjectionAutoConfiguration cfg = new ProjectionAutoConfiguration();
        ProjectionExecutor exec = Mockito.mock(ProjectionExecutor.class);
        ProjectionExecutorProvider provider = Mockito.mock(ProjectionExecutorProvider.class);
        Mockito.when(provider.getExecutor()).thenReturn(exec);
        ProjectionExecutor result = cfg.projectionExecutor(List.of(provider));
        assertSame(exec, result);
    }

    @Test
    void projectionExecutorReturnsRoutingExecutorForMultipleProviders() {
        ProjectionAutoConfiguration cfg = new ProjectionAutoConfiguration();
        ProjectionExecutorProvider p1 = Mockito.mock(ProjectionExecutorProvider.class);
        ProjectionExecutorProvider p2 = Mockito.mock(ProjectionExecutorProvider.class);
        ProjectionExecutor result = cfg.projectionExecutor(List.of(p1, p2));
        assertTrue(result instanceof RoutingProjectionExecutor);
    }
}
