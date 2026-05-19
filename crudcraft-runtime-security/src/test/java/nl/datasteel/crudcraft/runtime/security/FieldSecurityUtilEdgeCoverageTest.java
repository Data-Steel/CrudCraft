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

package nl.datasteel.crudcraft.runtime.security;

import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FieldSecurityUtilEdgeCoverageTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        clearMetadataCache();
    }

    @Test
    void canReadFieldHandlesNullFieldName() {
        assertTrue(FieldSecurityUtil.canReadField(SecuredDto.class, null));
    }

    @Test
    void filterReadReturnsNullForNullInput() {
        assertNull(FieldSecurityUtil.filterRead(null));
    }

    @Test
    void filterReadHandlesPlainObjectInstance() {
        Object input = new Object();
        Object result = FieldSecurityUtil.filterRead(input);
        assertSame(input, result);
    }

    @Test
    void canReadFieldAllowsUnsecuredMetadataRule() {
        assertTrue(FieldSecurityUtil.canReadField(UnsecuredMetadataDto.class, "secret"));
    }

    @Test
    void readFilterKeepsSecuredFieldWhenRoleIsPresent() {
        authenticateAdmin();
        SecuredDto dto = new SecuredDto();
        dto.secret = "visible";

        FieldSecurityUtil.filterRead(dto);

        assertEquals("visible", dto.secret);
    }

    @Test
    void readFilterHandlesNullNestedValues() {
        authenticateUser();
        NestedContainer dto = new NestedContainer();
        dto.child = null;
        dto.array = new InnerDto[] {null};
        dto.list = new ArrayList<>();
        dto.list.add(null);

        FieldSecurityUtil.filterRead(dto);

        assertNull(dto.child);
        assertNull(dto.array[0]);
        assertNull(dto.list.getFirst());
    }

    @Test
    void writeFilterKeepsSecuredFieldWhenRoleIsPresent() {
        authenticateAdmin();
        SecuredDto dto = new SecuredDto();
        dto.secret = "changed";

        SecuredDto result = FieldSecurityUtil.filterWrite(dto);

        assertSame(dto, result);
        assertEquals("changed", dto.secret);
    }

    @Test
    void writeFilterHandlesSelfReferences() {
        authenticateUser();
        SelfRefDto existing = new SelfRefDto();
        existing.secret = "persisted";
        existing.self = existing;
        SelfRefDto request = new SelfRefDto();
        request.secret = "changed";
        request.self = request;

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("persisted", request.secret);
        assertSame(request, request.self);
    }

    @Test
    void metadataWriteWithUnsecuredRuleRecurses() {
        authenticateUser();
        UnsecuredWriteDto existing = new UnsecuredWriteDto();
        existing.nested = InnerDto.of("persisted");
        UnsecuredWriteDto request = new UnsecuredWriteDto();
        request.nested = InnerDto.of("changed");

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("persisted", request.nested.secret);
    }

    @Test
    void metadataWriteDeniedWithNoWriterLeavesValue() {
        authenticateUser();
        NoWriterMetadataDto existing = new NoWriterMetadataDto();
        existing.secret = "persisted";
        NoWriterMetadataDto request = new NoWriterMetadataDto();
        request.secret = "changed";

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("changed", request.secret);
    }

    @Test
    void metadataWriteUsingJdkFieldTriggersInaccessibleObjectFailureMode() {
        authenticateUser();
        JdkExistingValueDto request = new JdkExistingValueDto();
        request.value = "changed";

        assertThrows(
                InaccessibleObjectException.class,
                () -> FieldSecurityUtil.filterWrite(request, "existing"));
    }

    @Test
    void filterWriteHandlesPlainObjectInstance() {
        Object input = new Object();
        Object result = FieldSecurityUtil.filterWrite(input, new Object());
        assertSame(input, result);
    }

    @Test
    void readFilterSkipsJavaLangElementsDuringRecursion() {
        authenticateUser();
        MixedContainer dto = new MixedContainer();
        dto.array = new Object[] {"plain-text"};
        dto.list = new ArrayList<>();
        dto.list.add("plain-text");

        FieldSecurityUtil.filterRead(dto);

        assertEquals("plain-text", dto.array[0]);
        assertEquals("plain-text", dto.list.getFirst());
    }

    @Test
    void writeFilterSkipsJavaLangElementsDuringRecursion() {
        authenticateUser();
        MixedContainer request = new MixedContainer();
        request.array = new Object[] {"new-value"};
        request.list = new ArrayList<>();
        request.list.add("new-value");

        MixedContainer existing = new MixedContainer();
        existing.array = new Object[] {"existing"};
        existing.list = new ArrayList<>();
        existing.list.add("existing");

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("new-value", request.array[0]);
        assertEquals("new-value", request.list.getFirst());
    }

    @Test
    void readFilterRedactsDeepNestedFieldBeyondFiveLevels() {
        authenticateUser();
        Level1 root = new Level1();
        root.level2 = new Level2();
        root.level2.level3 = new Level3();
        root.level2.level3.level4 = new Level4();
        root.level2.level3.level4.level5 = new Level5();
        root.level2.level3.level4.level5.level6 = new Level6();
        root.level2.level3.level4.level5.level6.secret = "deep-secret";

        FieldSecurityUtil.filterRead(root);

        assertNull(root.level2.level3.level4.level5.level6.secret);
    }

    @Test
    void reflectiveFieldReadHelperWrapsIllegalAccess() throws Exception {
        Method method =
                FieldSecurityUtil.class.getDeclaredMethod(
                        "getFieldValue", java.lang.reflect.Field.class, Object.class);
        method.setAccessible(true);
        java.lang.reflect.Field field = PrivateFieldHolder.class.getDeclaredField("value");
        PrivateFieldHolder holder = new PrivateFieldHolder();

        InvocationTargetException thrown =
                assertThrows(
                        InvocationTargetException.class, () -> method.invoke(null, field, holder));

        assertTrue(thrown.getCause() instanceof IllegalStateException);
        assertTrue(thrown.getCause().getCause() instanceof IllegalAccessException);
    }

    @Test
    void reflectiveFieldWriteHelperWrapsIllegalAccess() throws Exception {
        Method method =
                FieldSecurityUtil.class.getDeclaredMethod(
                        "setFieldValue", java.lang.reflect.Field.class, Object.class, Object.class);
        method.setAccessible(true);
        java.lang.reflect.Field field = PrivateFieldHolder.class.getDeclaredField("value");
        PrivateFieldHolder holder = new PrivateFieldHolder();

        InvocationTargetException thrown =
                assertThrows(
                        InvocationTargetException.class,
                        () -> method.invoke(null, field, holder, "changed"));

        assertTrue(thrown.getCause() instanceof IllegalStateException);
        assertTrue(thrown.getCause().getCause() instanceof IllegalAccessException);
    }

    @Test
    void metadataWriteReadsExistingValueViaReflectionFallback() {
        authenticateUser();
        ReflectionFallbackRequest request = new ReflectionFallbackRequest();
        request.secret = "changed";
        ReflectionFallbackExisting existing = new ReflectionFallbackExisting();
        existing.secret = "persisted";

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("persisted", request.secret);
    }

    @Test
    void writeRecursionHandlesArrayAndCollectionLengthMismatch() {
        authenticateUser();
        MismatchedContainer request = new MismatchedContainer();
        request.array = new InnerDto[] {InnerDto.of("a1"), InnerDto.of("a2")};
        request.list = new ArrayList<>(List.of(InnerDto.of("l1"), InnerDto.of("l2")));

        MismatchedContainer existing = new MismatchedContainer();
        existing.array = new InnerDto[] {InnerDto.of("a1-persisted")};
        existing.list = new ArrayList<>(List.of(InnerDto.of("l1-persisted")));

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("a1-persisted", request.array[0].secret);
        assertNull(request.array[1].secret);
        assertEquals("l1-persisted", request.list.get(0).secret);
        assertNull(request.list.get(1).secret);
    }

    @Test
    void privateFindFieldHandlesInterfaceInput() throws Exception {
        Method findField =
                FieldSecurityUtil.class.getDeclaredMethod("findField", Class.class, String.class);
        findField.setAccessible(true);

        assertNull(findField.invoke(null, MarkerInterface.class, "missing"));
    }

    @Test
    void privateConstructorIsCoveredForUtilityClass() throws Exception {
        Constructor<FieldSecurityUtil> constructor = FieldSecurityUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertEquals(FieldSecurityUtil.class, constructor.newInstance().getClass());
    }

    @Test
    void privateWriteRecursionCoversArrayAndCollectionNullAndTypeMismatchBranches()
            throws Exception {
        authenticateUser();
        Method method =
                FieldSecurityUtil.class.getDeclaredMethod(
                        "processWriteRecursively", Object.class, Object.class, java.util.Map.class);
        method.setAccessible(true);

        Object[] arrayWithNull = new Object[] {null};
        Object[] nullArrayResult =
                (Object[]) method.invoke(null, arrayWithNull, "not-an-array", new IdentityHashMap<>());
        assertFalse(nullArrayResult == arrayWithNull);
        assertNull(nullArrayResult[0]);

        InnerDto[] securedArray = new InnerDto[] {InnerDto.of("changed")};
        InnerDto[] existingArray = new InnerDto[] {InnerDto.of("existing")};
        InnerDto[] securedArrayResult =
                (InnerDto[])
                        method.invoke(
                                null, securedArray, existingArray, new IdentityHashMap<>());
        assertFalse(securedArrayResult == securedArray);
        assertEquals("existing", securedArrayResult[0].secret);

        List<Object> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        Object listResult = method.invoke(null, listWithNull, List.of("existing"), new IdentityHashMap<>());
        assertTrue(listResult instanceof List<?>);
    }

    @Test
    void privateReadRecursionReturnsCopiedArrayWithFilteredElements() throws Exception {
        authenticateUser();
        Method method =
                FieldSecurityUtil.class.getDeclaredMethod(
                        "processReadRecursively", Object.class, java.util.Map.class);
        method.setAccessible(true);
        InnerDto[] array = new InnerDto[] {InnerDto.of("secret")};

        InnerDto[] result = (InnerDto[]) method.invoke(null, array, new IdentityHashMap<>());

        assertFalse(result == array);
        assertNull(result[0].secret);
    }

    @Test
    void privateRoleAndUtilityMethodsCoverNullAndEnumBranches() throws Exception {
        Method hasAnyRole = FieldSecurityUtil.class.getDeclaredMethod("hasAnyRole", List.class);
        hasAnyRole.setAccessible(true);
        assertFalse((Boolean) hasAnyRole.invoke(null, (Object) null));
        assertFalse((Boolean) hasAnyRole.invoke(null, List.of("", " ")));

        Method findField =
                FieldSecurityUtil.class.getDeclaredMethod("findField", Class.class, String.class);
        findField.setAccessible(true);
        assertNull(findField.invoke(null, Object.class, "missing"));

        Method isJavaLangType =
                FieldSecurityUtil.class.getDeclaredMethod("isJavaLangType", Class.class);
        isJavaLangType.setAccessible(true);
        assertEquals(Boolean.TRUE, isJavaLangType.invoke(null, LocalEnum.class));
        assertEquals(Boolean.TRUE, isJavaLangType.invoke(null, int.class));

        assertFalse((Boolean) hasAnyRole.invoke(null, java.util.Arrays.asList((String) null, " ")));
    }

    private void authenticateUser() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private void authenticateAdmin() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "admin", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    static class SecuredDto {
        @FieldSecurity(
                readRoles = {"ADMIN"},
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private String secret;
    }

    static class InnerDto {
        @FieldSecurity(
                readRoles = {"ADMIN"},
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private String secret;

        static InnerDto of(String secret) {
            InnerDto dto = new InnerDto();
            dto.secret = secret;
            return dto;
        }
    }

    static class NestedContainer {
        private InnerDto child;
        private InnerDto[] array;
        private List<InnerDto> list;
    }

    static class MixedContainer {
        private Object[] array;
        private List<Object> list;
    }

    static class PrivateFieldHolder {
        private String value = "initial";
    }

    interface MarkerInterface {}

    static class ReflectionFallbackRequest {
        String secret;

        public static FieldSecurityMetadata<ReflectionFallbackRequest> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    dto -> dto.secret,
                                    (dto, value) -> dto.secret = (String) value,
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class ReflectionFallbackExisting {
        private String secret;
    }

    static class MismatchedContainer {
        private InnerDto[] array;
        private List<InnerDto> list;
    }

    static class SelfRefDto {
        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private String secret;

        private SelfRefDto self;
    }

    static class UnsecuredMetadataDto {
        private String secret;

        public static FieldSecurityMetadata<UnsecuredMetadataDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    dto -> dto.secret,
                                    (dto, value) -> dto.secret = (String) value,
                                    false,
                                    List.of(),
                                    List.of(),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class UnsecuredWriteDto {
        private InnerDto nested;

        public static FieldSecurityMetadata<UnsecuredWriteDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "nested",
                                    dto -> dto.nested,
                                    (dto, value) -> dto.nested = (InnerDto) value,
                                    false,
                                    List.of(),
                                    List.of(),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class NoWriterMetadataDto {
        private String secret;

        public static FieldSecurityMetadata<NoWriterMetadataDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    dto -> dto.secret,
                                    null,
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class JdkExistingValueDto {
        private String value;

        public static FieldSecurityMetadata<JdkExistingValueDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "value",
                                    dto -> dto.value,
                                    (dto, newValue) -> dto.value = (String) newValue,
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    enum LocalEnum {
        A
    }

    static class Level1 {
        Level2 level2;
    }

    static class Level2 {
        Level3 level3;
    }

    static class Level3 {
        Level4 level4;
    }

    static class Level4 {
        Level5 level5;
    }

    static class Level5 {
        Level6 level6;
    }

    static class Level6 {
        @FieldSecurity(readRoles = {"ADMIN"}, writeRoles = {"ADMIN"})
        String secret;
    }

    private static void clearMetadataCache() {
        FieldSecurityUtil.clearMetadataCacheForTesting();
    }
}
