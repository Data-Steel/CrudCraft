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

package nl.datasteel.crudcraft.runtime.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import nl.datasteel.crudcraft.runtime.service.projection.ProjectionAdapter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * Resolves optional Spring collaborators for {@link AbstractCrudService}.
 *
 * <p>Spring calls {@link #setApplicationContext(ApplicationContext)} during bean initialization,
 * before generated services handle requests. The method resets the lazy collaborator caches so
 * tests and unusual container refreshes can re-publish the context, but production code should not
 * swap the application context while requests are in flight. The resolved extension list and
 * projection adapter are published through volatile fields after synchronized initialization.
 */
final class ServiceCollaborators<T, U> {

    private volatile ApplicationContext applicationContext;
    private volatile List<CrudRuntimeExtension<T, U>> contextExtensions;
    private volatile boolean contextExtensionsResolved;
    private volatile ProjectionAdapter projectionAdapter;
    private volatile boolean projectionAdapterResolved;
    private final Map<Class<?>, Boolean> projectionSupportCache = new ConcurrentHashMap<>();
    private volatile PlatformTransactionManager transactionManager;
    private volatile boolean transactionManagerResolved;
    private volatile List<ReadDeniedAuditHook> readDeniedAuditHooks;
    private volatile boolean readDeniedAuditHooksResolved;

    /**
     * Publishes the Spring application context used for optional collaborator lookup.
     *
     * @param applicationContext context supplied by Spring during bean initialization
     */
    void setApplicationContext(ApplicationContext applicationContext) {
        synchronized (this) {
            this.applicationContext = applicationContext;
            contextExtensions = null;
            contextExtensionsResolved = false;
            projectionAdapter = null;
            projectionAdapterResolved = false;
            projectionSupportCache.clear();
            transactionManager = null;
            transactionManagerResolved = false;
            readDeniedAuditHooks = null;
            readDeniedAuditHooksResolved = false;
        }
    }

    @SuppressWarnings("unchecked")
    List<CrudRuntimeExtension<T, U>> contextExtensions() {
        if (!contextExtensionsResolved) {
            synchronized (this) {
                if (!contextExtensionsResolved) {
                    if (applicationContext == null) {
                        contextExtensions = List.of();
                    } else {
                        contextExtensions =
                                applicationContext
                                        .getBeansOfType(CrudRuntimeExtension.class)
                                        .values()
                                        .stream()
                                        .map(extension -> (CrudRuntimeExtension<T, U>) extension)
                                        .toList();
                    }
                    contextExtensionsResolved = true;
                }
            }
        }
        return contextExtensions;
    }

    ProjectionAdapter projectionAdapter() {
        if (!projectionAdapterResolved) {
            synchronized (this) {
                if (applicationContext != null) {
                    try {
                        projectionAdapter = applicationContext.getBean(ProjectionAdapter.class);
                    } catch (BeansException ignored) {
                        projectionAdapter = null;
                    }
                }
                projectionAdapterResolved = true;
            }
        }
        return projectionAdapter;
    }

    boolean supportsProjection(Class<?> projectionType) {
        if (projectionType == null) {
            return false;
        }
        ProjectionAdapter adapter = projectionAdapter();
        if (adapter == null) {
            return false;
        }
        return projectionSupportCache.computeIfAbsent(projectionType, adapter::supports);
    }

    PlatformTransactionManager transactionManager() {
        if (!transactionManagerResolved) {
            synchronized (this) {
                if (!transactionManagerResolved) {
                    if (applicationContext != null) {
                        try {
                            transactionManager =
                                    applicationContext.getBean(PlatformTransactionManager.class);
                        } catch (BeansException ignored) {
                            transactionManager = null;
                        }
                    }
                    transactionManagerResolved = true;
                }
            }
        }
        return transactionManager;
    }

    List<ReadDeniedAuditHook> readDeniedAuditHooks() {
        if (!readDeniedAuditHooksResolved) {
            synchronized (this) {
                if (!readDeniedAuditHooksResolved) {
                    if (applicationContext == null) {
                        readDeniedAuditHooks = List.of();
                    } else {
                        readDeniedAuditHooks =
                                List.copyOf(
                                        applicationContext
                                                .getBeansOfType(ReadDeniedAuditHook.class)
                                                .values());
                    }
                    readDeniedAuditHooksResolved = true;
                }
            }
        }
        return readDeniedAuditHooks;
    }
}
