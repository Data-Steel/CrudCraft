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
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.runtime.projection.impl.jpa.CriteriaProjectionBuilder;
import nl.datasteel.crudcraft.runtime.projection.impl.jpa.JpaCriteriaProjectionAdapter;
import nl.datasteel.crudcraft.runtime.projection.impl.jpa.JpaProjectionExecutor;
import nl.datasteel.crudcraft.runtime.projection.impl.jpa.MetadataCriteriaProjectionBuilder;
import nl.datasteel.crudcraft.runtime.projection.mapping.SimpleProjectionMetadataRegistry;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.runtime.service.projection.ProjectionAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;


/**
 * Spring Boot auto-configuration for projection support.
 *
 * <p>Generated applications normally get a registry bean because the annotation processor emits an
 * application-local {@code GeneratedProjectionMetadataRegistry} Spring component. If no registry
 * bean exists, this auto-configuration falls back to the class configured by {@code
 * crudcraft.projection.registry-fqcn} and finally to an empty {@link
 * SimpleProjectionMetadataRegistry}. Setting the property to {@code none} or {@code noop}
 * intentionally selects the empty registry.
 *
 * <pre>{@code
 * crudcraft.projection.registry-fqcn=
 *   com.example.projection.GeneratedProjectionMetadataRegistry
 * }</pre>
 */
@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@EnableConfigurationProperties(ProjectionProperties.class)
public class ProjectionAutoConfiguration {

    private static final String DEFAULT_REGISTRY_FQCN =
            "nl.datasteel.crudcraft.runtime.projection.mapping.GeneratedProjectionMetadataRegistry";

    /** Creates the projection auto configuration. */
    public ProjectionAutoConfiguration() {}

    /**
     * Creates the projection metadata registry bean.
     *
     * @param properties projection configuration properties
     * @return metadata registry instance
     */
    @Bean
    @ConditionalOnMissingBean(ProjectionMetadataRegistry.class)
    public ProjectionMetadataRegistry projectionMetadataRegistry(ProjectionProperties properties) {
        String fqcn = properties.getRegistryFqcn();
        if (!StringUtils.hasText(fqcn)
                || "none".equalsIgnoreCase(fqcn)
                || "noop".equalsIgnoreCase(fqcn)) {
            return new SimpleProjectionMetadataRegistry();
        }
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        ClassLoader fallback = ProjectionAutoConfiguration.class.getClassLoader();
        for (ClassLoader loader : new ClassLoader[] {context, fallback}) {
            if (loader == null) {
                continue;
            }
            try {
                Class<?> type = Class.forName(fqcn, true, loader);
                return (ProjectionMetadataRegistry) type.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException ignored) {
                // try next loader
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(
                        "Failed to instantiate projection metadata registry", ex);
            }
        }
        if (!DEFAULT_REGISTRY_FQCN.equals(fqcn)) {
            throw new IllegalStateException(
                    "Configured projection registry class not found: " + fqcn);
        }
        return new SimpleProjectionMetadataRegistry();
    }

    /**
     * Creates the criteria projection builder bean.
     *
     * @param registry metadata registry
     * @param fieldSecurityAdapter optional field security adapter provider
     * @return criteria projection builder
     */
    @Bean
    public CriteriaProjectionBuilder criteriaProjectionBuilder(
            ProjectionMetadataRegistry registry,
            ObjectProvider<FieldSecurityAdapter> fieldSecurityAdapter) {
        return new MetadataCriteriaProjectionBuilder(
                registry, resolveFieldSecurityAdapter(fieldSecurityAdapter));
    }

    /**
     * Creates the JPA projection executor bean.
     *
     * @param entityManager JPA entity manager
     * @param criteriaBuilder criteria projection builder
     * @param registry metadata registry
     * @param fieldSecurityAdapter optional field security adapter provider
     * @param properties projection configuration properties
     * @return projection executor implementation
     */
    @Bean
    @ConditionalOnClass(EntityManager.class)
    @ConditionalOnBean(EntityManager.class)
    public ProjectionExecutor jpaProjectionExecutor(
            EntityManager entityManager,
            CriteriaProjectionBuilder criteriaBuilder,
            ProjectionMetadataRegistry registry,
            ObjectProvider<FieldSecurityAdapter> fieldSecurityAdapter,
            ProjectionProperties properties) {
        return new JpaProjectionExecutor(
                entityManager,
                criteriaBuilder,
                registry,
                resolveFieldSecurityAdapter(fieldSecurityAdapter),
                properties);
    }

    /**
     * Creates the core projection adapter bean.
     *
     * @param projectionExecutor projection executor implementation
     * @param registry metadata registry
     * @return projection adapter exposed to runtime-core
     */
    @Bean
    @Primary
    @ConditionalOnBean(ProjectionExecutor.class)
    public ProjectionAdapter projectionAdapter(
            ProjectionExecutor projectionExecutor, ProjectionMetadataRegistry registry) {
        return new JpaCriteriaProjectionAdapter(projectionExecutor, registry);
    }

    private FieldSecurityAdapter resolveFieldSecurityAdapter(
            ObjectProvider<FieldSecurityAdapter> fieldSecurityAdapter) {
        FieldSecurityAdapter resolved = fieldSecurityAdapter.getIfAvailable();
        return resolved == null ? FieldSecurityAdapter.NOOP : resolved;
    }
}
