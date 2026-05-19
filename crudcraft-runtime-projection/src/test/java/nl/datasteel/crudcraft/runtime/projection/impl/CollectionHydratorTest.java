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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CollectionHydratorTest {

    @Test
    void fetchResultIsImmutableAndDefensivelyCopied() {
        List<Object[]> rows = new ArrayList<>(List.<Object[]>of(new Object[] {1L, 10L}));
        List<Class<?>> joinTypes = new ArrayList<>(List.of(ChildEntity.class));
        CollectionHydrator.FetchResult result = new CollectionHydrator.FetchResult(rows, joinTypes);

        rows.add(new Object[] {2L, 20L});
        joinTypes.add(ParentEntity.class);

        assertEquals(1, result.rows().size());
        assertEquals(1, result.joinTypes().size());
        assertThrows(UnsupportedOperationException.class, () -> result.rows().add(new Object[0]));
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.joinTypes().add(ParentEntity.class));
    }

    @Test
    void constructorThrowsForUtilityClass() throws Exception {
        Constructor<CollectionHydrator> constructor =
                CollectionHydrator.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertEquals(IllegalStateException.class, exception.getCause().getClass());
    }

    @Test
    void hydrateCollectionsReturnsEarlyWhenNoIdsOrNoAttributes() {
        AtomicInteger fetchCalls = new AtomicInteger();
        CollectionHydrator.RowFetcher fetcher =
                (entityType, attributes, ids) -> {
                    fetchCalls.incrementAndGet();
                    return new CollectionHydrator.FetchResult(List.of(), List.of());
                };

        CollectionHydrator.hydrateCollections(
                ParentEntity.class, new ParentMetadata(), new HashMap<>(), fetcher);
        assertEquals(0, fetchCalls.get());

        Map<Object, ParentDto> dtoMap = new HashMap<>();
        dtoMap.put(1L, new ParentDto("first"));
        CollectionHydrator.hydrateCollections(
                ParentEntity.class, new ParentMetadata(), List.of(), dtoMap, fetcher);
        assertEquals(0, fetchCalls.get());
    }

    @Test
    void hydrateCollectionsHydratesNestedCollectionsAndDeduplicatesRows() {
        ParentDto first = new ParentDto("first");
        ParentDto second = new ParentDto("second");
        Map<Object, ParentDto> dtoMap = new HashMap<>();
        dtoMap.put(1L, first);
        dtoMap.put(2L, second);

        ChildDto childA = new ChildDto("a");
        ChildDto childB = new ChildDto("b");
        LeafDto leafX = new LeafDto("x");
        LeafDto leafY = new LeafDto("y");

        AtomicInteger fetchCalls = new AtomicInteger();
        CollectionHydrator.RowFetcher fetcher =
                (entityType, attributes, ids) -> {
                    fetchCalls.incrementAndGet();
                    if (entityType.equals(ParentEntity.class)) {
                        return new CollectionHydrator.FetchResult(
                                List.of(
                                        new Object[] {1L, 10L, childA},
                                        new Object[] {1L, 10L, childA},
                                        new Object[] {1L, 11L, childB},
                                        new Object[] {2L, null, null},
                                        new Object[] {2L, 11L, childB}),
                                List.of(ChildEntity.class));
                    }
                    if (entityType.equals(ChildEntity.class)) {
                        return new CollectionHydrator.FetchResult(
                                List.of(
                                        new Object[] {10L, 100L, leafX},
                                        new Object[] {10L, 100L, leafX},
                                        new Object[] {11L, 101L, leafY}),
                                List.of());
                    }
                    throw new IllegalStateException(
                            "Unexpected fetch type: " + entityType.getName());
                };

        CollectionHydrator.hydrateCollections(
                ParentEntity.class, new ParentMetadata(), dtoMap, fetcher);

        assertEquals(2, first.children.size());
        assertEquals(List.of("a", "b"), first.children.stream().map(child -> child.name).toList());
        assertEquals(1, second.children.size());
        assertEquals("b", second.children.getFirst().name);
        assertEquals(List.of("x"), childA.leaves.stream().map(leaf -> leaf.value).toList());
        assertEquals(List.of("y"), childB.leaves.stream().map(leaf -> leaf.value).toList());
        assertEquals(2, fetchCalls.get());
    }

    @Test
    void hydrateCollectionsSkipsRecursiveHydrationWhenNestedMapIsEmpty() {
        ParentDto parent = new ParentDto("parent");
        Map<Object, ParentDto> dtoMap = new HashMap<>();
        dtoMap.put(1L, parent);

        AtomicInteger fetchCalls = new AtomicInteger();
        CollectionHydrator.RowFetcher fetcher =
                (entityType, attributes, ids) -> {
                    fetchCalls.incrementAndGet();
                    return new CollectionHydrator.FetchResult(
                            List.<Object[]>of(new Object[] {1L, null, null}),
                            List.of(ChildEntity.class));
                };

        CollectionHydrator.hydrateCollections(
                ParentEntity.class, new ParentMetadata(), dtoMap, fetcher);

        assertEquals(1, fetchCalls.get());
        assertEquals(0, parent.children.size());
    }

    @Test
    void hydrateCollectionsThrowsWhenRowIsTooShort() {
        ParentDto parent = new ParentDto("parent");
        Map<Object, ParentDto> dtoMap = new HashMap<>();
        dtoMap.put(1L, parent);

        CollectionHydrator.RowFetcher fetcher =
                (entityType, attributes, ids) ->
                        new CollectionHydrator.FetchResult(
                                List.<Object[]>of(new Object[] {1L, 10L}),
                                List.of(ChildEntity.class));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                CollectionHydrator.hydrateCollections(
                                        ParentEntity.class, new ParentMetadata(), dtoMap, fetcher));
        assertEquals(
                "Invalid collection hydration row: expected at least 3 columns but got 2",
                exception.getMessage());
    }

    @Test
    void hydrateCollectionsThrowsWhenJoinTypeIsMissingForNestedCollection() {
        ParentDto parent = new ParentDto("parent");
        Map<Object, ParentDto> dtoMap = new HashMap<>();
        dtoMap.put(1L, parent);

        ChildDto child = new ChildDto("child");
        CollectionHydrator.RowFetcher fetcher =
                (entityType, attributes, ids) ->
                        new CollectionHydrator.FetchResult(
                                List.<Object[]>of(new Object[] {1L, 10L, child}), List.of());

        assertThrows(
                IllegalStateException.class,
                () ->
                        CollectionHydrator.hydrateCollections(
                                ParentEntity.class, new ParentMetadata(), dtoMap, fetcher));
    }

    @Test
    void hydrateCollectionsHandlesMultipleCollectionAttributesInSingleRow() {
        ParentWithTwoCollectionsDto parent = new ParentWithTwoCollectionsDto("parent");
        Map<Object, ParentWithTwoCollectionsDto> dtoMap = new HashMap<>();
        dtoMap.put(1L, parent);

        ChildDto child = new ChildDto("child");
        LeafDto leaf = new LeafDto("leaf");
        CollectionHydrator.RowFetcher fetcher =
                (entityType, attributes, ids) ->
                        new CollectionHydrator.FetchResult(
                                List.<Object[]>of(new Object[] {1L, 10L, child, 20L, leaf}),
                                List.of(ChildEntity.class, LeafEntity.class));

        CollectionHydrator.hydrateCollections(
                ParentEntity.class, new ParentWithTwoCollectionsMetadata(), dtoMap, fetcher);

        assertEquals(List.of("child"), parent.children.stream().map(value -> value.name).toList());
        assertEquals(List.of("leaf"), parent.leaves.stream().map(value -> value.value).toList());
    }

    @Test
    void hydrateCollectionsIgnoresRowsWithNullChildIdEvenWhenChildDtoExists() {
        ParentDto parent = new ParentDto("parent");
        Map<Object, ParentDto> dtoMap = new HashMap<>();
        dtoMap.put(1L, parent);

        CollectionHydrator.RowFetcher fetcher =
                (entityType, attributes, ids) ->
                        new CollectionHydrator.FetchResult(
                                List.<Object[]>of(new Object[] {1L, null, new ChildDto("ghost")}),
                                List.of(ChildEntity.class));

        CollectionHydrator.hydrateCollections(
                ParentEntity.class, new ParentMetadata(), dtoMap, fetcher);

        assertEquals(0, parent.children.size());
    }

    @Test
    void hydrateCollectionsStopsSelfRecursiveHydrationViaVisitedIds() {
        SelfDto root = new SelfDto("root");
        Map<Object, SelfDto> dtoMap = new HashMap<>();
        dtoMap.put(1L, root);

        AtomicInteger fetchCalls = new AtomicInteger();
        CollectionHydrator.RowFetcher fetcher =
                (entityType, attributes, ids) -> {
                    if (fetchCalls.incrementAndGet() > 2) {
                        throw new IllegalStateException("recursive fetch loop");
                    }
                    return new CollectionHydrator.FetchResult(
                            List.<Object[]>of(new Object[] {1L, 1L, new SelfDto("child")}),
                            List.of(SelfEntity.class));
                };

        assertDoesNotThrow(
                () ->
                        CollectionHydrator.hydrateCollections(
                                SelfEntity.class, new SelfMetadata(), dtoMap, fetcher));
        assertEquals(1, fetchCalls.get());
    }

    static final class ParentEntity {}

    static final class ChildEntity {}

    static final class LeafEntity {}

    static final class SelfEntity {}

    static final class ParentDto {
        private final String name;
        private List<ChildDto> children = List.of();

        ParentDto(String name) {
            this.name = name;
        }
    }

    static final class ChildDto {
        private final String name;
        private List<LeafDto> leaves = List.of();

        ChildDto(String name) {
            this.name = name;
        }
    }

    static final class LeafDto {
        private final String value;

        LeafDto(String value) {
            this.value = value;
        }
    }

    static final class ParentWithTwoCollectionsDto {
        private final String name;
        private List<ChildDto> children = List.of();
        private List<LeafDto> leaves = List.of();

        ParentWithTwoCollectionsDto(String name) {
            this.name = name;
        }
    }

    static final class SelfDto {
        private final String name;
        private List<SelfDto> children = List.of();

        SelfDto(String name) {
            this.name = name;
        }
    }

    static final class ParentMetadata implements ProjectionMetadata<ParentDto> {

        @Override
        public Class<ParentDto> dtoType() {
            return ParentDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(
                    new TestAttribute(
                            "children",
                            "children",
                            true,
                            new ChildMetadata(),
                            (dto, values) -> ((ParentDto) dto).children = cast(values)));
        }
    }

    static final class ChildMetadata implements ProjectionMetadata<ChildDto> {

        @Override
        public Class<ChildDto> dtoType() {
            return ChildDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(
                    new TestAttribute(
                            "leaves",
                            "leaves",
                            true,
                            null,
                            (dto, values) -> ((ChildDto) dto).leaves = cast(values)));
        }
    }

    static final class ParentWithTwoCollectionsMetadata
            implements ProjectionMetadata<ParentWithTwoCollectionsDto> {

        @Override
        public Class<ParentWithTwoCollectionsDto> dtoType() {
            return ParentWithTwoCollectionsDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(
                    new TestAttribute(
                            "children",
                            "children",
                            true,
                            new ChildMetadata(),
                            (dto, values) ->
                                    ((ParentWithTwoCollectionsDto) dto).children = cast(values)),
                    new TestAttribute(
                            "leaves",
                            "leaves",
                            true,
                            null,
                            (dto, values) ->
                                    ((ParentWithTwoCollectionsDto) dto).leaves = cast(values)));
        }
    }

    static final class SelfMetadata implements ProjectionMetadata<SelfDto> {

        @Override
        public Class<SelfDto> dtoType() {
            return SelfDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(
                    new TestAttribute(
                            "children",
                            "children",
                            true,
                            this,
                            (dto, values) -> ((SelfDto) dto).children = cast(values)));
        }
    }

    record TestAttribute(
            String dtoFieldName,
            String path,
            boolean collection,
            ProjectionMetadata<?> nested,
            BiConsumer<Object, List<?>> mutator)
            implements ProjectionMetadata.Attribute {}

    @SuppressWarnings("unchecked")
    private static <T> List<T> cast(List<?> values) {
        return (List<T>) values;
    }
}
