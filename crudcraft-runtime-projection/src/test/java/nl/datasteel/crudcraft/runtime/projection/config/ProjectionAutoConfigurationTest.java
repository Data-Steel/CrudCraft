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

package nl.datasteel.crudcraft.runtime.projection.config;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.runtime.projection.impl.jpa.CriteriaProjectionBuilder;
import nl.datasteel.crudcraft.runtime.projection.impl.jpa.JpaCriteriaProjectionAdapter;
import nl.datasteel.crudcraft.runtime.projection.impl.jpa.JpaProjectionExecutor;
import nl.datasteel.crudcraft.runtime.projection.impl.jpa.MetadataCriteriaProjectionBuilder;
import nl.datasteel.crudcraft.runtime.projection.mapping.SimpleProjectionMetadataRegistry;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.runtime.service.projection.ProjectionAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ProjectionAutoConfigurationTest {

    private final ProjectionAutoConfiguration autoConfiguration = new ProjectionAutoConfiguration();

    @Test
    void usesNoopRegistryWhenConfiguredAsNoop() {
        ProjectionProperties properties = new ProjectionProperties();
        properties.setRegistryFqcn("noop");

        ProjectionMetadataRegistry registry =
                autoConfiguration.projectionMetadataRegistry(properties);

        assertInstanceOf(SimpleProjectionMetadataRegistry.class, registry);
    }

    @Test
    void usesNoopRegistryWhenConfiguredAsNoneOrBlank() {
        ProjectionProperties none = new ProjectionProperties();
        none.setRegistryFqcn("none");
        assertInstanceOf(
                SimpleProjectionMetadataRegistry.class,
                autoConfiguration.projectionMetadataRegistry(none));

        ProjectionProperties blank = new ProjectionProperties();
        blank.setRegistryFqcn("   ");
        assertInstanceOf(
                SimpleProjectionMetadataRegistry.class,
                autoConfiguration.projectionMetadataRegistry(blank));
    }

    @Test
    void loadsConfiguredRegistryClass() {
        ProjectionProperties properties = new ProjectionProperties();
        properties.setRegistryFqcn(SimpleProjectionMetadataRegistry.class.getName());

        ProjectionMetadataRegistry registry =
                autoConfiguration.projectionMetadataRegistry(properties);

        assertInstanceOf(SimpleProjectionMetadataRegistry.class, registry);
    }

    @Test
    void skipsNullContextClassLoaderAndLoadsFromFallback() {
        ProjectionProperties properties = new ProjectionProperties();
        properties.setRegistryFqcn(SimpleProjectionMetadataRegistry.class.getName());
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        try {
            ProjectionMetadataRegistry registry =
                    autoConfiguration.projectionMetadataRegistry(properties);
            assertInstanceOf(SimpleProjectionMetadataRegistry.class, registry);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Test
    void throwsWhenCustomRegistryClassIsMissing() {
        ProjectionProperties properties = new ProjectionProperties();
        properties.setRegistryFqcn("com.example.missing.CustomProjectionRegistry");

        assertThrows(
                IllegalStateException.class,
                () -> autoConfiguration.projectionMetadataRegistry(properties));
    }

    @Test
    void throwsWhenRegistryInstantiationFailsWithReflectiveError() {
        ProjectionProperties properties = new ProjectionProperties();
        properties.setRegistryFqcn(ThrowingRegistry.class.getName());

        assertThrows(
                IllegalStateException.class,
                () -> autoConfiguration.projectionMetadataRegistry(properties));
    }

    @Test
    void fallsBackToSimpleRegistryWhenDefaultGeneratedRegistryIsMissing() {
        ProjectionProperties properties = new ProjectionProperties();

        ProjectionMetadataRegistry registry =
                autoConfiguration.projectionMetadataRegistry(properties);

        assertInstanceOf(SimpleProjectionMetadataRegistry.class, registry);
    }

    @Test
    void projectionPropertiesExposeRuntimeSafeguards() {
        ProjectionProperties properties = new ProjectionProperties();

        assertEquals(5, properties.getMaxDepth());
        assertTrue(properties.isWarnOnCollectionHydration());

        properties.setMaxDepth(-4);
        properties.setWarnOnCollectionHydration(false);

        assertEquals(0, properties.getMaxDepth());
        assertFalse(properties.isWarnOnCollectionHydration());
    }

    @Test
    void createsCriteriaBuilderWithNoopSecurityWhenProviderEmpty() {
        @SuppressWarnings("unchecked")
        ObjectProvider<FieldSecurityAdapter> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        CriteriaProjectionBuilder builder =
                autoConfiguration.criteriaProjectionBuilder(
                        new SimpleProjectionMetadataRegistry(), provider);

        assertInstanceOf(MetadataCriteriaProjectionBuilder.class, builder);
    }

    @Test
    void createsJpaProjectionExecutorAndProjectionAdapterBeans() {
        EntityManager entityManager = mock(EntityManager.class);
        CriteriaProjectionBuilder criteriaProjectionBuilder = mock(CriteriaProjectionBuilder.class);
        ProjectionMetadataRegistry registry = new SimpleProjectionMetadataRegistry();
        @SuppressWarnings("unchecked")
        ObjectProvider<FieldSecurityAdapter> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        ProjectionExecutor executor =
                autoConfiguration.jpaProjectionExecutor(
                        entityManager,
                        criteriaProjectionBuilder,
                        registry,
                        provider,
                        new ProjectionProperties());

        assertInstanceOf(JpaProjectionExecutor.class, executor);
        ProjectionAdapter adapter = autoConfiguration.projectionAdapter(executor, registry);
        assertInstanceOf(JpaCriteriaProjectionAdapter.class, adapter);
    }

    @Test
    void propagatesCustomFieldSecurityAdapterWhenProviderReturnsOne() throws Exception {
        ProjectionMetadataRegistry registry = new SimpleProjectionMetadataRegistry();
        FieldSecurityAdapter adapter = new FieldSecurityAdapter() {};
        @SuppressWarnings("unchecked")
        ObjectProvider<FieldSecurityAdapter> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(adapter);

        CriteriaProjectionBuilder builder =
                autoConfiguration.criteriaProjectionBuilder(registry, provider);
        assertInstanceOf(MetadataCriteriaProjectionBuilder.class, builder);
        assertSame(adapter, readField(builder, "fieldSecurityAdapter"));

        ProjectionExecutor executor =
                autoConfiguration.jpaProjectionExecutor(
                        mock(EntityManager.class),
                        mock(CriteriaProjectionBuilder.class),
                        registry,
                        provider,
                        new ProjectionProperties());
        assertInstanceOf(JpaProjectionExecutor.class, executor);
        assertSame(adapter, readField(executor, "fieldSecurityAdapter"));
    }

    private static Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    public static final class ThrowingRegistry implements ProjectionMetadataRegistry {

        private ThrowingRegistry() {
            throw new UnsupportedOperationException("boom");
        }

        @Override
        public <D> ProjectionMetadata<D> getMetadata(Class<D> dtoType) {
            return null;
        }
    }
}
