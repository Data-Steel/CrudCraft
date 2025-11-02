/**
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
package nl.datasteel.crudcraft.projection.impl;

import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class CollectionHydratorTest {

    static class ParentDto {
        List<ChildDto> children = new ArrayList<>();
    }
    static class ChildDto {
        String name;
        List<TagDto> tags = new ArrayList<>();
        ChildDto(String name) { this.name = name; }
    }
    static class TagDto {
        String value;
        TagDto(String value) { this.value = value; }
    }
    static class NodeDto {
        List<NodeDto> children = new ArrayList<>();
    }

    record Meta<D>(Class<D> type, List<ProjectionMetadata.Attribute> attrs) implements ProjectionMetadata<D> {
        @Override public Class<D> dtoType() { return type; }
        @Override public List<ProjectionMetadata.Attribute> attributes() { return attrs; }
    }
    record Attr(String path, ProjectionMetadata<?> nested, boolean collection,
                BiConsumer<Object,List<?>> mutator) implements ProjectionMetadata.Attribute {
        @Override public String path() { return path; }
        @Override public ProjectionMetadata<?> nested() { return nested; }
        @Override public boolean collection() { return collection; }
        @Override public BiConsumer<Object,List<?>> mutator() { return mutator; }
    }

    @Test
    void doesNotFetchWhenNoCollectionAttributes() {
        ProjectionMetadata<ParentDto> metadata = new Meta<>(ParentDto.class, List.of(
                new Attr("name", null, false, (d,l)->{})
        ));
        Map<Object, ParentDto> map = new HashMap<>();
        map.put(1L, new ParentDto());
        CollectionHydrator.hydrateCollections(Object.class, metadata, map, (t,a,i)->{
            fail("Should not fetch");
            return null;
        });
    }

    @Test
    void hydratesCollectionsRecursively() {
        ProjectionMetadata<TagDto> tagMeta = new Meta<>(TagDto.class, List.of());
        ProjectionMetadata<ChildDto> childMeta = new Meta<>(ChildDto.class, List.of(
                new Attr("name", null, false, (d,l)->{}),
                new Attr("tags", tagMeta, true, (dto,list)-> ((ChildDto)dto).tags = (List<TagDto>) list)
        ));
        ProjectionMetadata<ParentDto> parentMeta = new Meta<>(ParentDto.class, List.of(
                new Attr("children", childMeta, true, (dto,list)-> ((ParentDto)dto).children = (List<ChildDto>) list)
        ));

        ParentDto parent = new ParentDto();
        Map<Object, ParentDto> map = new HashMap<>();
        map.put(1L, parent);

        CollectionHydrator.RowFetcher fetcher = new CollectionHydrator.RowFetcher() {
            @Override
            public CollectionHydrator.FetchResult fetch(Class<?> type, List<ProjectionMetadata.Attribute> attrs, List<Object> ids) {
                if (type == Object.class) {
                    ChildDto c1 = new ChildDto("c1");
                    ChildDto c2 = new ChildDto("c2");
                    // child join type is represented by ChildDto.class
                    return new CollectionHydrator.FetchResult(List.of(
                            new Object[]{1L,10L,c1},
                            new Object[]{1L,11L,c2}
                    ), List.of(ChildDto.class));
                } else {
                    TagDto t1 = new TagDto("t1");
                    TagDto t2 = new TagDto("t2");
                    return new CollectionHydrator.FetchResult(List.of(
                            new Object[]{10L,100L,t1},
                            new Object[]{10L,101L,t2}
                    ), List.of(Object.class));
                }
            }
        };

        CollectionHydrator.hydrateCollections(Object.class, parentMeta, map, fetcher);
        assertEquals(2, parent.children.size());
        assertEquals("c1", parent.children.get(0).name);
        assertEquals(2, parent.children.get(0).tags.size());
        assertEquals("t1", parent.children.get(0).tags.get(0).value);
        assertTrue(parent.children.get(1).tags.isEmpty());
    }

    @Test
    void skipsFetchingWhenDtoMapEmpty() {
        ProjectionMetadata<ParentDto> metadata = new Meta<>(ParentDto.class, List.of(
                new Attr("children", null, true, (dto,list)->{})
        ));
        Map<Object, ParentDto> map = new HashMap<>();
        CollectionHydrator.hydrateCollections(Object.class, metadata, map, (t,a,i) -> {
            fail("Should not fetch");
            return null;
        });
    }

    @Test
    void deduplicatesRowsAndIgnoresNullChildIds() {
        ProjectionMetadata<ChildDto> childMeta = new Meta<>(ChildDto.class, List.of());
        ProjectionMetadata<ParentDto> meta = new Meta<>(ParentDto.class, List.of(
                new Attr("children", childMeta, true, (dto,list)-> ((ParentDto)dto).children = (List<ChildDto>) list)
        ));
        ParentDto parent = new ParentDto();
        Map<Object, ParentDto> map = new HashMap<>();
        map.put(1L, parent);
        CollectionHydrator.RowFetcher fetcher = (type, attrs, ids) ->
                new CollectionHydrator.FetchResult(List.of(
                        new Object[]{1L,10L,new ChildDto("a")},
                        new Object[]{1L,10L,new ChildDto("duplicate")},
                        new Object[]{1L,null,null}
                ), List.of(Object.class));
        CollectionHydrator.hydrateCollections(Object.class, meta, map, fetcher);
        assertEquals(1, parent.children.size());
        assertEquals("a", parent.children.get(0).name);
    }

    @Test
    void handlesSelfReferentialCollectionsWithoutInfiniteLoop() {
        Meta<NodeDto> nodeMeta = new Meta<>(NodeDto.class, new ArrayList<>());
        Attr childAttr = new Attr("children", nodeMeta, true,
                (dto,list)-> ((NodeDto)dto).children = (List<NodeDto>) list);
        nodeMeta.attributes().add(childAttr);
        NodeDto root = new NodeDto();
        Map<Object, NodeDto> map = new HashMap<>();
        map.put(1L, root);
        int[] calls = {0};
        CollectionHydrator.RowFetcher fetcher = (type, attrs, ids) -> {
            calls[0]++;
            if (ids.contains(1L)) {
                return new CollectionHydrator.FetchResult(
                        java.util.Collections.singletonList(new Object[]{1L, 2L, new NodeDto()}),
                        java.util.List.of(Object.class)
                );
            } else if (ids.contains(2L)) {
                return new CollectionHydrator.FetchResult(
                        java.util.Collections.singletonList(new Object[]{2L, 1L, new NodeDto()}),
                        java.util.List.of(Object.class)
                );
            }
            return new CollectionHydrator.FetchResult(List.of(), List.of(Object.class));
        };
        CollectionHydrator.hydrateCollections(Object.class, nodeMeta, (Map) map, fetcher);
        assertEquals(2, calls[0]);
        assertEquals(1, root.children.size());
        assertEquals(1, root.children.get(0).children.size());
    }

    @Test
    void constructorIsPrivate() throws Exception {
        Constructor<CollectionHydrator> ctor = CollectionHydrator.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertThrows(IllegalStateException.class, () -> {
            try {
                ctor.newInstance();
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }
}
