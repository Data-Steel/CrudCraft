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

package nl.datasteel.crudcraft.runtime.export.service;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.datasteel.crudcraft.runtime.export.EntityExportAdapter;
import nl.datasteel.crudcraft.runtime.export.config.ExportProperties;
import org.springframework.beans.factory.ObjectProvider;


/**
 * Factory bean used by generated controllers to build export services without direct {@code new}
 * wiring in generated source.
 */
public class EnhancedExportServiceFactory {

    private final ObjectProvider<EntityExportAdapter> entityExportAdapterProvider;
    private final boolean allowEntityMode;
    private final int maxDepth;

    /**
     * Creates the export service factory.
     *
     * @param entityExportAdapterProvider optional provider for entity export adapter
     * @param allowEntityMode whether entity export mode is allowed
     */
    public EnhancedExportServiceFactory(
            @Nullable ObjectProvider<EntityExportAdapter> entityExportAdapterProvider,
            boolean allowEntityMode) {
        this(entityExportAdapterProvider, allowEntityMode, ExportProperties.DEFAULT_MAX_DEPTH);
    }

    /**
     * Creates the export service factory.
     *
     * @param entityExportAdapterProvider optional provider for entity export adapter
     * @param allowEntityMode whether entity export mode is allowed
     * @param maxDepth configured default max depth
     */
    public EnhancedExportServiceFactory(
            @Nullable ObjectProvider<EntityExportAdapter> entityExportAdapterProvider,
            boolean allowEntityMode,
            int maxDepth) {
        this.entityExportAdapterProvider = entityExportAdapterProvider;
        this.allowEntityMode = allowEntityMode;
        this.maxDepth = Math.max(0, maxDepth);
    }

    /**
     * Builds an enhanced export service for one generated controller.
     *
     * @param maxRows global max rows
     * @param maxCsvRows max CSV rows
     * @param maxJsonRows max JSON rows
     * @param maxXlsxRows max XLSX rows
     * @param maxPageSize max fetch page size
     * @param entityClass JPA entity class
     * @param <R> response DTO type
     * @param <E> entity type
     * @param <S> search request type
     * @return controller-scoped export service
     */
    public <R, E, S> @NonNull EnhancedExportService<R, E, S> create(
            int maxRows,
            int maxCsvRows,
            int maxJsonRows,
            int maxXlsxRows,
            int maxPageSize,
            @NonNull Class<E> entityClass) {
        ExportService.ExportConfig config =
                new ExportService.ExportConfig(
                        maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, maxDepth);
        EntityExportAdapter adapter =
                entityExportAdapterProvider == null
                        ? null
                        : entityExportAdapterProvider.getIfAvailable();
        return new EnhancedExportService<>(config, adapter, entityClass, allowEntityMode);
    }
}
