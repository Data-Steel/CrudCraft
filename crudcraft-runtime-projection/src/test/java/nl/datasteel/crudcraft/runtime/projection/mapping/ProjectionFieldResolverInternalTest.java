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

package nl.datasteel.crudcraft.runtime.projection.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.runtime.projection.dto.fixture.InsidePackageDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SuppressWarnings({"unchecked", "rawtypes"})
class ProjectionFieldResolverInternalTest {

    private final ProjectionFieldResolver resolver = new ProjectionFieldResolver();

    @Test
    void resolveProjectionFieldValueHandlesNonProjectionAnnotationAndNullValueAndFailure()
            throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "resolveProjectionFieldValue", Field.class);
        method.setAccessible(true);

        Field deprecatedField = InternalFixture.class.getDeclaredField("deprecatedOnly");
        assertNull(method.invoke(resolver, deprecatedField));

        Field field = mock(Field.class);
        ProjectionField nullValue = mock(ProjectionField.class);
        when(nullValue.annotationType()).thenReturn((Class) ProjectionField.class);
        when(nullValue.value()).thenReturn(null);
        when(field.getAnnotations()).thenReturn(new Annotation[] {nullValue});
        assertNull(method.invoke(resolver, field));

        ProjectionField failing = mock(ProjectionField.class);
        when(failing.annotationType()).thenReturn((Class) ProjectionField.class);
        when(failing.value()).thenThrow(new IllegalStateException("boom"));
        when(field.getAnnotations()).thenReturn(new Annotation[] {failing});
        assertNull(method.invoke(resolver, field));
    }

    @Test
    void hasProjectionFieldAnnotationDetectsPresenceAndAbsence() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "hasProjectionFieldAnnotation", Field.class);
        method.setAccessible(true);

        Field projectionField = InternalFixture.class.getDeclaredField("projected");
        Field plain = InternalFixture.class.getDeclaredField("deprecatedOnly");

        assertTrue((Boolean) method.invoke(resolver, projectionField));
        assertFalse((Boolean) method.invoke(resolver, plain));
    }

    @Test
    void resolveCollectionElementCoversParameterizedAndWildcardVariants() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "resolveCollectionElement", Field.class);
        method.setAccessible(true);

        Field listOfClass = InternalFixture.class.getDeclaredField("names");
        Field listOfParameterized = InternalFixture.class.getDeclaredField("nestedLists");
        Field wildcardClass = InternalFixture.class.getDeclaredField("wildcardChildren");
        Field wildcardParameterized = InternalFixture.class.getDeclaredField("wildcardNested");

        assertEquals(String.class, method.invoke(resolver, listOfClass));
        assertEquals(List.class, method.invoke(resolver, listOfParameterized));
        assertEquals(InternalChildDto.class, method.invoke(resolver, wildcardClass));
        assertEquals(Object.class, method.invoke(resolver, wildcardParameterized));
    }

    @Test
    void isDtoHandlesNullPackageAndNamingHeuristic() throws Exception {
        Method method = ProjectionFieldResolver.class.getDeclaredMethod("isDto", Class.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(resolver, int.class));
        assertFalse((Boolean) method.invoke(resolver, InternalFixture.class));
        assertFalse((Boolean) method.invoke(resolver, OutsideDto.class));
        assertTrue((Boolean) method.invoke(resolver, InsidePackageDto.class));
    }

    @Test
    void resolveFallsBackToFieldNameWhenProjectionAnnotationIsEmpty() {
        List<ProjectionFieldResolver.FieldMapping> mappings =
                resolver.resolve(EmptyProjectionPathDto.class);

        assertEquals(1, mappings.size());
        assertEquals("fallbackName", mappings.getFirst().path());
    }

    @Test
    void findFieldThrowsWhenIndexIsNegativeAndNoMatchesExist() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "findField",
                        Class.class,
                        Parameter.class,
                        int.class,
                        List.class,
                        List.class);
        method.setAccessible(true);

        Constructor<FindFieldFixture> constructor =
                FindFieldFixture.class.getDeclaredConstructor(String.class);
        Parameter parameter = constructor.getParameters()[0];

        InvocationTargetException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        InvocationTargetException.class,
                        () ->
                                method.invoke(
                                        resolver,
                                        FindFieldFixture.class,
                                        parameter,
                                        -1,
                                        List.of(),
                                        List.of()));
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    void getFieldAtOrNullReturnsNullWhenIndexIsOutOfBounds() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "getFieldAtOrNull", List.class, int.class);
        method.setAccessible(true);

        Field field = InternalFixture.class.getDeclaredField("deprecatedOnly");

        assertEquals(field, method.invoke(resolver, List.of(field), 0));
        assertNull(method.invoke(resolver, List.of(field), 1));
    }

    @Test
    void resolveCollectionElementHandlesEdgeCasesForUnreachableGenericsBranches() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "resolveCollectionElement", Field.class);
        method.setAccessible(true);

        Field field = mock(Field.class);
        ParameterizedType topType = mock(ParameterizedType.class);
        ParameterizedType innerType = mock(ParameterizedType.class);
        when(innerType.getRawType())
                .thenReturn(
                        new Type() {
                            @Override
                            public String getTypeName() {
                                return "custom";
                            }
                        });
        when(topType.getActualTypeArguments()).thenReturn(new Type[] {innerType});
        when(field.getGenericType()).thenReturn(topType);
        assertEquals(Object.class, method.invoke(resolver, field));

        WildcardType wildcard = mock(WildcardType.class);
        when(wildcard.getUpperBounds()).thenReturn(new Type[0]);
        when(topType.getActualTypeArguments()).thenReturn(new Type[] {wildcard});
        assertEquals(Object.class, method.invoke(resolver, field));
    }

    @Test
    void findConstructorKeepsFirstConstructorWhenParameterCountsAreEqual() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod("findConstructor", Class.class);
        method.setAccessible(true);

        Constructor<?>[] constructors = EqualParameterCountFixture.class.getDeclaredConstructors();
        Constructor<?> selected =
                (Constructor<?>) method.invoke(resolver, EqualParameterCountFixture.class);

        assertEquals(constructors[0], selected);
    }

    @Test
    void findFieldFallsBackToDeclaredIndexWhenOriginalSlotWasConsumed() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "findField",
                        Class.class,
                        Parameter.class,
                        int.class,
                        List.class,
                        List.class);
        method.setAccessible(true);

        Constructor<FindFieldBoundaryFixture> constructor =
                FindFieldBoundaryFixture.class.getDeclaredConstructor(Integer.class, Object.class);
        Parameter firstParam = constructor.getParameters()[0];
        Parameter secondParam = constructor.getParameters()[1];

        List<Field> allDeclared =
                new ArrayList<>(Arrays.asList(FindFieldBoundaryFixture.class.getDeclaredFields()));
        List<Field> remaining = new ArrayList<>(allDeclared);

        Field first =
                (Field)
                        method.invoke(
                                resolver,
                                FindFieldBoundaryFixture.class,
                                firstParam,
                                0,
                                remaining,
                                allDeclared);
        remaining.remove(first);

        Field second =
                (Field)
                        method.invoke(
                                resolver,
                                FindFieldBoundaryFixture.class,
                                secondParam,
                                1,
                                remaining,
                                allDeclared);

        assertEquals("beta", first.getName());
        assertEquals("gamma", second.getName());
    }

    @Test
    void findFieldFallsBackToOriginalDeclaredSlotAtIndexZero() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "findField",
                        Class.class,
                        Parameter.class,
                        int.class,
                        List.class,
                        List.class);
        method.setAccessible(true);

        Constructor<IndexZeroDeclaredFixture> constructor =
                IndexZeroDeclaredFixture.class.getDeclaredConstructor(Object.class);
        Parameter parameter = constructor.getParameters()[0];

        List<Field> allDeclared =
                new ArrayList<>(Arrays.asList(IndexZeroDeclaredFixture.class.getDeclaredFields()));
        List<Field> remaining = new ArrayList<>(allDeclared);

        Field resolved =
                (Field)
                        method.invoke(
                                resolver,
                                IndexZeroDeclaredFixture.class,
                                parameter,
                                0,
                                remaining,
                                allDeclared);

        assertEquals("alpha", resolved.getName());
    }

    @Test
    void findFieldFallsBackToRemainingDeclaredSlotAtIndexZero() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "findField",
                        Class.class,
                        Parameter.class,
                        int.class,
                        List.class,
                        List.class);
        method.setAccessible(true);

        Constructor<IndexZeroDeclaredFixture> constructor =
                IndexZeroDeclaredFixture.class.getDeclaredConstructor(Object.class);
        Parameter parameter = constructor.getParameters()[0];

        List<Field> allDeclared =
                new ArrayList<>(Arrays.asList(IndexZeroDeclaredFixture.class.getDeclaredFields()));
        List<Field> remaining = new ArrayList<>(allDeclared);
        Field consumed = allDeclared.getFirst();
        remaining.remove(consumed);

        Field resolved =
                (Field)
                        method.invoke(
                                resolver,
                                IndexZeroDeclaredFixture.class,
                                parameter,
                                0,
                                remaining,
                                allDeclared);

        assertEquals("beta", resolved.getName());
    }

    @Test
    void findFieldPrefersOriginalDeclaredOrderAtIndexZeroBeforeRemainingOrder() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "findField",
                        Class.class,
                        Parameter.class,
                        int.class,
                        List.class,
                        List.class);
        method.setAccessible(true);

        Constructor<IndexZeroDeclaredFixture> constructor =
                IndexZeroDeclaredFixture.class.getDeclaredConstructor(Object.class);
        Parameter parameter = constructor.getParameters()[0];

        List<Field> allDeclared =
                new ArrayList<>(Arrays.asList(IndexZeroDeclaredFixture.class.getDeclaredFields()));
        List<Field> reorderedRemaining = new ArrayList<>();
        reorderedRemaining.add(allDeclared.get(1));
        reorderedRemaining.add(allDeclared.getFirst());

        Field resolved =
                (Field)
                        method.invoke(
                                resolver,
                                IndexZeroDeclaredFixture.class,
                                parameter,
                                0,
                                reorderedRemaining,
                                allDeclared);

        assertEquals("alpha", resolved.getName());
    }

    @Test
    void findFieldPrefersSingleDtoCollectionElementType() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "findField",
                        Class.class,
                        Parameter.class,
                        int.class,
                        List.class,
                        List.class);
        method.setAccessible(true);

        Constructor<CollectionDtoTieBreakerFixture> constructor =
                CollectionDtoTieBreakerFixture.class.getDeclaredConstructor(List.class);
        Parameter parameter = constructor.getParameters()[0];
        List<Field> fields =
                new ArrayList<>(
                        Arrays.asList(CollectionDtoTieBreakerFixture.class.getDeclaredFields()));

        Field resolved =
                (Field)
                        method.invoke(
                                resolver,
                                CollectionDtoTieBreakerFixture.class,
                                parameter,
                                0,
                                fields,
                                fields);

        assertEquals("children", resolved.getName());
    }

    @Test
    void findFieldFallsThroughWhenMultipleDtoCollectionCandidatesExist() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "findField",
                        Class.class,
                        Parameter.class,
                        int.class,
                        List.class,
                        List.class);
        method.setAccessible(true);

        Constructor<MultipleDtoCollectionFixture> constructor =
                MultipleDtoCollectionFixture.class.getDeclaredConstructor(List.class);
        Parameter parameter = constructor.getParameters()[0];
        List<Field> fields =
                new ArrayList<>(Arrays.asList(MultipleDtoCollectionFixture.class.getDeclaredFields()));

        Field resolved =
                (Field)
                        method.invoke(
                                resolver,
                                MultipleDtoCollectionFixture.class,
                                parameter,
                                0,
                                fields,
                                fields);

        assertEquals("first", resolved.getName());
    }

    @Test
    void findFieldThrowsWhenPositiveIndexDoesNotResolveAnyFallbackField() throws Exception {
        Method method =
                ProjectionFieldResolver.class.getDeclaredMethod(
                        "findField",
                        Class.class,
                        Parameter.class,
                        int.class,
                        List.class,
                        List.class);
        method.setAccessible(true);

        Constructor<FindFieldFixture> constructor =
                FindFieldFixture.class.getDeclaredConstructor(String.class);
        Parameter parameter = constructor.getParameters()[0];

        InvocationTargetException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        InvocationTargetException.class,
                        () ->
                                method.invoke(
                                        resolver,
                                        FindFieldFixture.class,
                                        parameter,
                                        5,
                                        List.of(),
                                        List.of()));
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    static final class InternalFixture {
        @Deprecated private String deprecatedOnly;

        @ProjectionField("entity.path")
        private String projected;

        private List<String> names;
        private List<List<String>> nestedLists;
        private List<? extends InternalChildDto> wildcardChildren;
        private List<? extends List<String>> wildcardNested;
    }

    static final class EmptyProjectionPathDto {
        @ProjectionField("")
        private String fallbackName;

        EmptyProjectionPathDto(String fallbackName) {
            this.fallbackName = fallbackName;
        }
    }

    static final class FindFieldFixture {
        private String other;

        FindFieldFixture(String value) {
            this.other = value;
        }
    }

    static final class EqualParameterCountFixture {
        private String value;

        EqualParameterCountFixture(String value) {
            this.value = value;
        }

        EqualParameterCountFixture(Integer value) {
            this.value = value == null ? null : value.toString();
        }
    }

    static final class FindFieldBoundaryFixture {
        private String alpha;
        private Integer beta;
        private String gamma;

        FindFieldBoundaryFixture(Integer first, Object second) {
            this.alpha = "a";
            this.beta = first;
            this.gamma = second == null ? "" : second.toString();
        }
    }

    static final class IndexZeroDeclaredFixture {
        private String alpha;
        private String beta;

        IndexZeroDeclaredFixture(Object value) {
            this.alpha = value == null ? "" : value.toString();
            this.beta = this.alpha;
        }
    }

    static final class CollectionDtoTieBreakerFixture {
        private List<String> names;
        private List<InsidePackageDto> children;

        CollectionDtoTieBreakerFixture(List<?> value) {
            this.names = List.of();
            this.children = List.of();
        }
    }

    static final class MultipleDtoCollectionFixture {
        private List<InsidePackageDto> first;
        private List<InsidePackageDto> second;

        MultipleDtoCollectionFixture(List<?> value) {
            this.first = List.of();
            this.second = List.of();
        }
    }

    static final class InternalChildDto {}

    static final class OutsideDto {}
}
