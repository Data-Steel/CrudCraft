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

package nl.datasteel.crudcraft.runtime.projection.impl.jpa;

import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import java.util.List;
import java.util.function.BiConsumer;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.runtime.projection.mapping.ProjectionMapper;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class MetadataCriteriaProjectionBuilderTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void buildCreatesDistinctCriteriaQuery() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        MetadataCriteriaProjectionBuilder builder = new MetadataCriteriaProjectionBuilder(registry);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery criteriaQuery = mock(CriteriaQuery.class);
        Root root = mock(Root.class);
        Selection selection = mock(Selection.class);

        when(cb.createQuery(ResultDto.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Entity.class)).thenReturn(root);
        when(registry.getMetadata(ResultDto.class)).thenReturn(null);
        when(criteriaQuery.select(selection)).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(true)).thenReturn(criteriaQuery);

        ProjectionMapper mapper = mock(ProjectionMapper.class);
        MetadataCriteriaProjectionBuilder builderWithMapper =
                new MetadataCriteriaProjectionBuilder(registry, mapper);
        when(mapper.construct(cb, root, ResultDto.class)).thenReturn(selection);

        CriteriaQuery<ResultDto> built =
                builderWithMapper.build(
                        cb,
                        Entity.class,
                        ResultDto.class,
                        ProjectionQuery.of(new NoopFilter<>(), null));

        assertSame(criteriaQuery, built);
        verify(criteriaQuery).select(selection);
        verify(criteriaQuery).distinct(true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void constructFallsBackToMapperWhenMetadataMissing() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        ProjectionMapper mapper = mock(ProjectionMapper.class);
        MetadataCriteriaProjectionBuilder builder =
                new MetadataCriteriaProjectionBuilder(registry, mapper, null);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        From from = mock(From.class);
        Selection selection = mock(Selection.class);

        when(registry.getMetadata(ResultDto.class)).thenReturn(null);
        when(mapper.construct(cb, from, ResultDto.class)).thenReturn(selection);

        Selection<ResultDto> result = builder.construct(cb, from, ResultDto.class);

        assertSame(selection, result);
        verify(mapper).construct(cb, from, ResultDto.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void constructUsesMetadataWithDeniedAndNestedSelections() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        FieldSecurityAdapter security = new SelectiveSecurityAdapter();
        MetadataCriteriaProjectionBuilder builder =
                new MetadataCriteriaProjectionBuilder(registry, new ProjectionMapper(), security);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        From root = mock(From.class);
        Join profileJoin = mock(Join.class);
        Join addressJoin = mock(Join.class);
        Path allowedPath = mock(Path.class);
        Path agePath = mock(Path.class);
        Path cityPath = mock(Path.class);
        Path zipPath = mock(Path.class);
        Path nicknamePath = mock(Path.class);
        Expression deniedNested = mock(Expression.class);
        Expression deniedPrimitive = mock(Expression.class);
        CompoundSelection nestedConstruct = mock(CompoundSelection.class);
        CompoundSelection dtoConstruct = mock(CompoundSelection.class);

        when(registry.getMetadata(ResultDto.class)).thenReturn(resultMetadata());
        when(root.get("name")).thenReturn(allowedPath);
        when(root.get("age")).thenReturn(agePath);
        when(agePath.getJavaType()).thenReturn((Class) int.class);
        when(root.join("profile", JoinType.LEFT)).thenReturn(profileJoin);
        when(profileJoin.get("nickname")).thenReturn(nicknamePath);
        when(root.join("address", JoinType.LEFT)).thenReturn(addressJoin);
        when(addressJoin.get("city")).thenReturn(cityPath);
        when(addressJoin.get("zip")).thenReturn(zipPath);

        when(cb.nullLiteral((Class) ProfileDto.class)).thenReturn(deniedNested);
        when(cb.literal(0)).thenReturn(deniedPrimitive);
        when(cb.construct(eq(ProfileDto.class), any(Selection[].class)))
                .thenReturn(nestedConstruct);
        when(cb.construct(eq(ResultDto.class), any(Selection[].class))).thenReturn(dtoConstruct);

        Selection<ResultDto> selection = builder.construct(cb, root, ResultDto.class);

        assertSame(dtoConstruct, selection);
        verify(cb).nullLiteral((Class) ProfileDto.class);
        verify(cb).literal(0);
        verify(root, times(1)).join("address", JoinType.LEFT);
        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<Selection[]> selectionCaptor =
                (ArgumentCaptor) ArgumentCaptor.forClass(Selection[].class);
        verify(cb).construct(eq(ResultDto.class), selectionCaptor.capture());
        assertTrue(
                java.util.Arrays.stream(selectionCaptor.getValue())
                        .noneMatch(java.util.Objects::isNull));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void constructCoversAllPrimitiveDefaultValues() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        MetadataCriteriaProjectionBuilder builder =
                new MetadataCriteriaProjectionBuilder(
                        registry, new ProjectionMapper(), new DenyAllSecurity());

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        From root = mock(From.class);
        Path booleanPath = primitivePath(boolean.class);
        Path bytePath = primitivePath(byte.class);
        Path shortPath = primitivePath(short.class);
        Path intPath = primitivePath(int.class);
        Path longPath = primitivePath(long.class);
        Path floatPath = primitivePath(float.class);
        Path doublePath = primitivePath(double.class);
        Path charPath = primitivePath(char.class);
        Path voidPath = primitivePath(void.class);
        Expression literalSelection = mock(Expression.class);
        CompoundSelection dtoSelection = mock(CompoundSelection.class);

        when(registry.getMetadata(PrimitiveDefaultsDto.class)).thenReturn(primitiveMetadata());
        when(root.get("boolValue")).thenReturn(booleanPath);
        when(root.get("byteValue")).thenReturn(bytePath);
        when(root.get("shortValue")).thenReturn(shortPath);
        when(root.get("intValue")).thenReturn(intPath);
        when(root.get("longValue")).thenReturn(longPath);
        when(root.get("floatValue")).thenReturn(floatPath);
        when(root.get("doubleValue")).thenReturn(doublePath);
        when(root.get("charValue")).thenReturn(charPath);
        when(root.get("voidValue")).thenReturn(voidPath);
        when(cb.literal(any())).thenReturn(literalSelection);
        when(cb.construct(eq(PrimitiveDefaultsDto.class), any(Selection[].class)))
                .thenReturn(dtoSelection);

        builder.construct(cb, root, PrimitiveDefaultsDto.class);

        verify(cb).literal(false);
        verify(cb).literal((byte) 0);
        verify(cb).literal((short) 0);
        verify(cb).literal(0);
        verify(cb).literal(0L);
        verify(cb).literal(0f);
        verify(cb).literal(0d);
        verify(cb).literal('\0');
        verify(cb).literal(null);
    }

    @SuppressWarnings("rawtypes")
    private static Path primitivePath(Class<?> type) {
        Path path = mock(Path.class);
        when(path.getJavaType()).thenReturn((Class) type);
        return path;
    }

    private static ProjectionMetadata<ResultDto> resultMetadata() {
        ProjectionMetadata<ProfileDto> profileMetadata =
                new TestMetadata<>(
                        ProfileDto.class,
                        List.of(attribute("nickname", "nickname", false, null, null)));
        return new TestMetadata<>(
                ResultDto.class,
                List.of(
                        attribute("allowed", "name", false, null, null),
                        attribute("denyNested", "manager", false, profileMetadata, null),
                        attribute("denyPrimitive", "age", false, null, null),
                        attribute("profile", "profile", false, profileMetadata, null),
                        attribute("city", "address.city", false, null, null),
                        attribute("zip", "address.zip", false, null, null)));
    }

    private static ProjectionMetadata<PrimitiveDefaultsDto> primitiveMetadata() {
        return new TestMetadata<>(
                PrimitiveDefaultsDto.class,
                List.of(
                        attribute("boolValue", "boolValue", false, null, null),
                        attribute("byteValue", "byteValue", false, null, null),
                        attribute("shortValue", "shortValue", false, null, null),
                        attribute("intValue", "intValue", false, null, null),
                        attribute("longValue", "longValue", false, null, null),
                        attribute("floatValue", "floatValue", false, null, null),
                        attribute("doubleValue", "doubleValue", false, null, null),
                        attribute("charValue", "charValue", false, null, null),
                        attribute("voidValue", "voidValue", false, null, null)));
    }

    private static ProjectionMetadata.Attribute attribute(
            String dtoField,
            String path,
            boolean collection,
            ProjectionMetadata<?> nested,
            BiConsumer<Object, List<?>> mutator) {
        return new TestAttribute(dtoField, path, collection, nested, mutator);
    }

    static final class Entity {}

    static final class ResultDto {}

    static final class ProfileDto {}

    static final class PrimitiveDefaultsDto {}

    static final class NoopFilter<T>
            implements nl.datasteel.crudcraft.runtime.projection.api.FilterCriteria<T> {}

    static final class SelectiveSecurityAdapter implements FieldSecurityAdapter {
        @Override
        public boolean canReadField(Class<?> dtoType, String fieldName) {
            return !fieldName.startsWith("deny");
        }
    }

    static final class DenyAllSecurity implements FieldSecurityAdapter {
        @Override
        public boolean canReadField(Class<?> dtoType, String fieldName) {
            return false;
        }
    }

    record TestAttribute(
            String dtoFieldName,
            String path,
            boolean collection,
            ProjectionMetadata<?> nested,
            BiConsumer<Object, List<?>> mutator)
            implements ProjectionMetadata.Attribute {}

    record TestMetadata<D>(Class<D> dtoType, List<ProjectionMetadata.Attribute> attributes)
            implements ProjectionMetadata<D> {}
}
