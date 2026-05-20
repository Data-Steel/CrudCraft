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

package nl.datasteel.crudcraft.runtime.projection.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;


/** Utility class that hydrates collection attributes for projection DTOs. */
public final class CollectionHydrator {

    /** Private constructor to prevent instantiation of this utility class. */
    private CollectionHydrator() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    /** Functional interface used to fetch collection data for a given entity type. */
    @FunctionalInterface
    public interface RowFetcher {

        /**
         * Fetches rows for the specified entity type and attributes. The rows are returned as a
         * list of object arrays, where each array represents a row with the first element being the
         * parent ID, followed by child IDs and their corresponding DTOs.
         *
         * @param entityType the type of the entity being projected
         * @param attributes the collection attributes to fetch
         * @param ids the IDs of the entities to fetch data for
         * @return a FetchResult containing the fetched rows and join types
         */
        FetchResult fetch(
                Class<?> entityType,
                List<ProjectionMetadata.Attribute> attributes,
                List<Object> ids);
    }

    /**
     * Result returned by the {@link RowFetcher} containing the fetched rows and the Java types of
     * the joined associations.
     *
     * @param rows fetched rows with parent and nested values
     * @param joinTypes Java types of joined associations
     */
    public record FetchResult(List<Object[]> rows, List<Class<?>> joinTypes) {

        /**
         * Constructs a new FetchResult with the given rows and join types. The lists are copied to
         * ensure immutability.
         *
         * @param rows the fetched rows, where each row is an array of objects
         * @param joinTypes the Java types of the joined associations
         */
        public FetchResult {
            rows = copyRows(rows);
            joinTypes = List.copyOf(joinTypes);
        }

        /**
         * Returns the fetched rows as an immutable list. Each row is represented as an array of
         * objects.
         *
         * @return an immutable list of rows
         */
        @Override
        public List<Object[]> rows() {
            return copyRows(rows);
        }

        /**
         * Returns the Java types of the joined associations as an immutable list. This is useful
         * for determining the types of nested DTOs that need to be hydrated recursively.
         *
         * @return an immutable list of join types
         */
        @Override
        public List<Class<?>> joinTypes() {
            return List.copyOf(joinTypes);
        }

        private static List<Object[]> copyRows(List<Object[]> source) {
            return source.stream().map(row -> Arrays.copyOf(row, row.length)).toList();
        }
    }

    /**
     * Hydrates collection attributes in the DTO map by fetching the necessary data from the
     * database.
     *
     * @param entityType the type of the entity being projected
     * @param metadata the projection metadata for the DTO type
     * @param dtoMap a map of IDs to DTO instances to hydrate
     * @param fetcher a function that fetches collection data for the given entity type
     * @param <T> entity type
     * @param <D> projected DTO type
     */
    public static <T, D> void hydrateCollections(
            Class<T> entityType,
            ProjectionMetadata<D> metadata,
            Map<Object, D> dtoMap,
            RowFetcher fetcher) {
        List<ProjectionMetadata.Attribute> attributes =
                metadata.attributes().stream()
                        .filter(ProjectionMetadata.Attribute::collection)
                        .toList();
        hydrateCollections(entityType, attributes, dtoMap, fetcher, new HashMap<>());
    }

    /**
     * Hydrates selected collection attributes in the DTO map.
     *
     * @param entityType the type of the entity being projected
     * @param metadata projection metadata for the DTO type
     * @param attributes collection attributes to hydrate
     * @param dtoMap map of parent IDs to DTO instances
     * @param fetcher callback used to fetch collection rows
     * @param <T> entity type
     * @param <D> projected DTO type
     */
    public static <T, D> void hydrateCollections(
            Class<T> entityType,
            ProjectionMetadata<D> metadata,
            List<ProjectionMetadata.Attribute> attributes,
            Map<Object, D> dtoMap,
            RowFetcher fetcher) {
        metadata.dtoType();
        hydrateCollections(entityType, attributes, dtoMap, fetcher, new HashMap<>());
    }

    /**
     * Hydrates collection attributes in the DTO map by fetching the necessary data from the
     * database.
     *
     * @param entityType the type of the entity being projected
     * @param attributes the projection metadata attributes
     * @param dtoMap a map of IDs to DTO instances to hydrate
     * @param fetcher a function that fetches collection data for the given entity type
     * @param visited the already visited attributes
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T, D> void hydrateCollections(
            Class<T> entityType,
            List<ProjectionMetadata.Attribute> attributes,
            Map<Object, D> dtoMap,
            RowFetcher fetcher,
            Map<Class<?>, Set<Object>> visited) {
        Set<Object> visitedIds = visited.computeIfAbsent(entityType, k -> new HashSet<>());
        List<Object> ids = dtoMap.keySet().stream().filter(id -> visitedIds.add(id)).toList();
        if (ids.isEmpty()) {
            return;
        }

        if (attributes.isEmpty()) {
            return;
        }

        FetchResult result = fetcher.fetch(entityType, attributes, ids);
        List<Object[]> rows = result.rows();

        int attrCount = attributes.size();
        int expectedColumns = 1 + (attrCount * 2);
        @SuppressWarnings("unchecked")
        Map<Object, Map<Object, Object>>[] groupedLists = new Map[attrCount];
        @SuppressWarnings("unchecked")
        Map<Object, Object>[] nestedMaps = new Map[attrCount];
        for (int i = 0; i < attrCount; i++) {
            groupedLists[i] = new HashMap<>();
            nestedMaps[i] = new LinkedHashMap<>();
        }

        for (Object[] row : rows) {
            if (row.length < expectedColumns) {
                throw new IllegalStateException(
                        "Invalid collection hydration row: expected at least "
                                + expectedColumns
                                + " columns but got "
                                + row.length);
            }
            Object parentId = row[0];
            int offset = 1;
            for (int i = 0; i < attrCount; i++) {
                Object childId = row[offset++];
                Object childDto = row[offset++];
                if (childId != null) {
                    groupedLists[i]
                            .computeIfAbsent(parentId, k -> new LinkedHashMap<>())
                            .putIfAbsent(childId, childDto);
                    nestedMaps[i].putIfAbsent(childId, childDto);
                }
            }
        }

        for (Map.Entry<Object, D> entry : dtoMap.entrySet()) {
            Object parentId = entry.getKey();
            D dto = entry.getValue();
            for (int i = 0; i < attrCount; i++) {
                ProjectionMetadata.Attribute attribute = attributes.get(i);
                Map<Object, Object> children =
                        groupedLists[i].getOrDefault(parentId, Collections.emptyMap());
                List<Object> list = new ArrayList<>(children.values());
                attribute.mutator().accept(dto, list);
            }
        }

        List<Class<?>> joinTypes = result.joinTypes();
        for (int i = 0; i < attrCount; i++) {
            ProjectionMetadata.Attribute attribute = attributes.get(i);
            ProjectionMetadata nested = attribute.nested();
            if (nested != null && !nestedMaps[i].isEmpty()) {
                if (i >= joinTypes.size()) {
                    throw new IllegalStateException(
                            "Missing join type for nested collection attribute '"
                                    + attribute.path()
                                    + "'");
                }
                Class joinType = joinTypes.get(i);
                List<ProjectionMetadata.Attribute> nestedAttributes = new ArrayList<>();
                for (Object nestedAttribute : nested.attributes()) {
                    ProjectionMetadata.Attribute typedAttribute =
                            (ProjectionMetadata.Attribute) nestedAttribute;
                    if (typedAttribute.collection()) {
                        nestedAttributes.add(typedAttribute);
                    }
                }
                hydrateCollections(joinType, nestedAttributes, nestedMaps[i], fetcher, visited);
            }
        }
    }
}
