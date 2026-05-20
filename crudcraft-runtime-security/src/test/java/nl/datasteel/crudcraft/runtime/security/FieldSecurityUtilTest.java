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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import nl.datasteel.crudcraft.runtime.security.hidden.HiddenSecurityFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FieldSecurityUtilTest {

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
        clearMetadataCache();
    }

    @Test
    void readFilterRedactsDeniedField() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SensitiveDto dto = new SensitiveDto();
        dto.secret = "top-secret";

        FieldSecurityUtil.filterRead(dto);

        assertNull(dto.secret);
    }

    @Test
    void writeFilterFailsOnDeniedWhenConfigured() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SensitiveDto request = new SensitiveDto();
        request.secret = "mutate";

        assertThrows(AccessDeniedException.class, () -> FieldSecurityUtil.filterWrite(request));
    }

    @Test
    void clearMetadataCacheForTestingRemovesCachedEntries() {
        FieldSecurityUtil.canReadField(MetadataDto.class, "secret");
        int populatedSize = metadataCacheSize();

        FieldSecurityUtil.clearMetadataCacheForTesting();

        assertTrue(populatedSize > 0);
        assertEquals(0, metadataCacheSize());
    }

    @Test
    void writeFilterCopiesExistingValueWhenSkipPolicy() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SkipDto existing = new SkipDto();
        existing.secret = "persisted";
        SkipDto request = new SkipDto();
        request.secret = "attempted-change";

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("persisted", request.secret);
    }

    @Test
    void generatedMetadataReadFilterRedactsDeniedField() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        MetadataDto dto = new MetadataDto();
        dto.setSecret("top-secret");

        FieldSecurityUtil.filterRead(dto);

        assertNull(dto.getSecret());
    }

    @Test
    void generatedMetadataWriteFilterCopiesExistingValue() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        MetadataDto existing = new MetadataDto();
        existing.setSecret("persisted");
        MetadataDto request = new MetadataDto();
        request.setSecret("attempted-change");

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("persisted", request.getSecret());
    }

    @Test
    void recordMetadataReadFilterReturnsRedactedCopy() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        RecordMetadataDto dto =
                new RecordMetadataDto("top-secret", "visible", 42, List.of(new RecordChild("kid")));

        RecordMetadataDto result = FieldSecurityUtil.filterRead(dto);

        assertNull(result.secret());
        assertEquals("visible", result.visible());
        assertEquals(0, result.missing());
        assertNull(result.children().getFirst().secret());
        assertEquals("top-secret", dto.secret());
    }

    @Test
    void recordMetadataWriteFilterCopiesExistingOrRedactsDeniedComponents() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        RecordMetadataDto existing =
                new RecordMetadataDto("persisted", "old", 9, List.of(new RecordChild("persisted-child")));
        RecordMetadataDto request =
                new RecordMetadataDto("attempt", "new", 7, List.of(new RecordChild("attempt-child")));

        RecordMetadataDto result = FieldSecurityUtil.filterWrite(request, existing);
        RecordMetadataDto withoutExisting = FieldSecurityUtil.filterWrite(request, null);

        assertEquals("persisted", result.secret());
        assertEquals("new", result.visible());
        assertEquals(0, result.missing());
        assertEquals("persisted-child", result.children().getFirst().secret());
        assertNull(withoutExisting.secret());
        assertNull(withoutExisting.children().getFirst().secret());
    }

    @Test
    void recordMetadataWriteFilterFailsWhenConfigured() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThrows(
                AccessDeniedException.class,
                () -> FieldSecurityUtil.filterWrite(new RecordFailDto("attempt"), null));
    }

    @Test
    void canReadFieldUsesAnnotationRules() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        assertTrue(!FieldSecurityUtil.canReadField(SensitiveDto.class, "secret"));
    }

    @Test
    void canReadFieldUsesGeneratedMetadataRules() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "admin", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertTrue(FieldSecurityUtil.canReadField(MetadataDto.class, "secret"));
    }

    @Test
    void canReadAndWriteReturnFalseWhenNoRolesDefined() {
        assertFalse(FieldSecurityUtil.canRead(null));
        assertFalse(FieldSecurityUtil.canWrite(new String[] {}));
    }

    @Test
    void canReadFieldDefaultsToTrueForInvalidInputOrMissingField() {
        assertTrue(FieldSecurityUtil.canReadField(null, "secret"));
        assertTrue(FieldSecurityUtil.canReadField(SensitiveDto.class, " "));
        assertTrue(FieldSecurityUtil.canReadField(SensitiveDto.class, "unknown"));
    }

    @Test
    void canReadFieldTreatsAllRoleAsAlwaysAllowed() {
        assertTrue(FieldSecurityUtil.canReadField(AllRoleDto.class, "openSecret"));
    }

    @Test
    void metadataCacheSupportsConcurrentAccess() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> checks =
                    java.util.stream.IntStream.range(0, 200)
                            .<Callable<Boolean>>mapToObj(
                                    index -> {
                                        Math.abs(index);
                                        return () ->
                                                FieldSecurityUtil.canReadField(
                                                        PlainDto.class, "value");
                                    })
                            .toList();

            for (Future<Boolean> result : executor.invokeAll(checks)) {
                assertTrue(result.get());
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void readFilterRecursesForNestedObjectArrayAndCollection() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        RecursiveContainer dto = new RecursiveContainer();
        dto.child = NestedDto.of("child-secret");
        dto.array = new NestedDto[] {NestedDto.of("array-secret")};
        dto.list = new ArrayList<>(List.of(NestedDto.of("list-secret")));
        dto.primitiveText = "visible";

        FieldSecurityUtil.filterRead(dto);

        assertNull(dto.child.secret);
        assertNull(dto.array[0].secret);
        assertNull(dto.list.getFirst().secret);
        assertEquals("visible", dto.primitiveText);
    }

    @Test
    void readFilterHandlesSelfReferencesWithoutLooping() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SelfRefDto dto = new SelfRefDto();
        dto.secret = "top";
        dto.self = dto;

        FieldSecurityUtil.filterRead(dto);

        assertNull(dto.secret);
        assertEquals(dto, dto.self);
    }

    @Test
    void writeFilterRecursesForNestedObjectArrayAndCollection() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        WriteRecursiveContainer existing = WriteRecursiveContainer.sample("persisted");
        WriteRecursiveContainer request = WriteRecursiveContainer.sample("changed");

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("persisted-child", request.child.secret);
        assertEquals("persisted-array", request.array[0].secret);
        assertEquals("persisted-list", request.list.getFirst().secret);
    }

    @Test
    void writeFilterRedactsAllPrimitiveTypesWhenDenied() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        PrimitiveDto dto = PrimitiveDto.nonDefault();

        FieldSecurityUtil.filterWrite(dto);

        assertFalse(dto.booleanValue);
        assertEquals((byte) 0, dto.byteValue);
        assertEquals((short) 0, dto.shortValue);
        assertEquals(0, dto.intValue);
        assertEquals(0L, dto.longValue);
        assertEquals(0f, dto.floatValue);
        assertEquals(0d, dto.doubleValue);
        assertEquals('\0', dto.charValue);
    }

    @Test
    void canReadFieldFallsBackWhenMetadataMethodIsNotStatic() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertFalse(FieldSecurityUtil.canReadField(NonStaticMetadataDto.class, "secret"));
    }

    @Test
    void runtimeFailureFromMetadataMethodIsPropagated() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FieldSecurityUtil.canReadField(RuntimeThrowingMetadataDto.class, "secret"));
    }

    @Test
    void checkedFailureFromMetadataMethodIsWrapped() {
        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                FieldSecurityUtil.canReadField(
                                        CheckedThrowingMetadataDto.class, "secret"));
        assertTrue(ex.getMessage().contains("Failed to resolve generated field security metadata"));
    }

    @Test
    void nonMetadataReturnFallsBackToAnnotationPath() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertFalse(FieldSecurityUtil.canReadField(NonMetadataReturnDto.class, "secret"));
    }

    @Test
    void generatedMetadataWriteFailPolicyThrowsAccessDenied() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        MetadataFailDto dto = new MetadataFailDto();
        dto.setSecret("mutate");

        assertThrows(AccessDeniedException.class, () -> FieldSecurityUtil.filterWrite(dto, null));
    }

    @Test
    void canReadFieldWithMetadataAndUnknownFieldDefaultsOpen() {
        assertTrue(FieldSecurityUtil.canReadField(MetadataDto.class, "other"));
    }

    @Test
    void canReadFieldWithExistingFieldButNoAnnotationReturnsTrue() {
        assertTrue(FieldSecurityUtil.canReadField(NoAnnotationFieldDto.class, "visible"));
    }

    @Test
    void metadataReadFilterKeepsReadableFieldWhenUserHasRole() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "admin", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        MetadataDto dto = new MetadataDto();
        dto.setSecret("visible");

        MetadataDto result = FieldSecurityUtil.filterRead(dto);

        assertSame(dto, result);
        assertEquals("visible", dto.getSecret());
    }

    @Test
    void metadataReadFilterOnUnsecuredRuleRecursesIntoNestedValue() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        MetadataNestedDto dto = new MetadataNestedDto();
        dto.setNested(NestedDto.of("nested-secret"));

        FieldSecurityUtil.filterRead(dto);

        assertNull(dto.getNested().secret);
    }

    @Test
    void metadataReadFilterWithNullWriterDoesNotThrow() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        MetadataNoWriterDto dto = new MetadataNoWriterDto();
        dto.setSecret("keep-value");

        FieldSecurityUtil.filterRead(dto);

        assertEquals("keep-value", dto.getSecret());
    }

    @Test
    void filterWriteReturnsNullForNullDto() {
        assertNull(FieldSecurityUtil.filterWrite(null));
        assertNull(FieldSecurityUtil.filterWrite(null, new Object()));
    }

    @Test
    void metadataWriteFilterAllowsMutationWhenRoleIsPresent() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "admin", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        MetadataDto existing = new MetadataDto();
        existing.setSecret("old");
        MetadataDto request = new MetadataDto();
        request.setSecret("new");

        MetadataDto result = FieldSecurityUtil.filterWrite(request, existing);

        assertSame(request, result);
        assertEquals("new", request.getSecret());
    }

    @Test
    void metadataWriteFilterHandlesDuplicateExistingRules() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "admin", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        DuplicateExistingMetadataDto existing = new DuplicateExistingMetadataDto();
        existing.setSecret("persisted");
        DuplicateExistingMetadataDto request = new DuplicateExistingMetadataDto();
        request.setSecret("changed");

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("changed", request.getSecret());
    }

    @Test
    void metadataWriteFilterUsesReflectionWhenExistingMetadataDoesNotContainField() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        ReflectionExistingDto existing = new ReflectionExistingDto();
        existing.secret = "persisted";
        ReflectionExistingDto request = new ReflectionExistingDto();
        request.secret = "changed";

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("persisted", request.secret);
    }

    @Test
    void metadataWriteFilterFallsBackToRedactionWhenExistingFieldMissing() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        ExistingWithoutField existing = new ExistingWithoutField();
        MissingFieldMetadataDto request = new MissingFieldMetadataDto();
        request.secret = "changed";

        FieldSecurityUtil.filterWrite(request, existing);

        assertNull(request.secret);
    }

    @Test
    void writeRecursivePathsHandleNullAndMismatchedExistingContainers() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));

        WriteRecursiveContainer existing = new WriteRecursiveContainer();
        existing.child = null;
        existing.array = null;
        existing.list = null;

        WriteRecursiveContainer request = WriteRecursiveContainer.sample("changed");
        FieldSecurityUtil.filterWrite(request, existing);

        assertNull(request.child.secret);
        assertNull(request.array[0].secret);
        assertNull(request.list.getFirst().secret);

        WriteRecursiveContainer requestWithoutExisting =
                WriteRecursiveContainer.sample("changed-again");
        FieldSecurityUtil.filterWrite(requestWithoutExisting, "not-a-container");
        assertNull(requestWithoutExisting.list.getFirst().secret);
    }

    @Test
    void finalFieldsCanStillBeRedactedWithoutThrowing() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        FinalFieldDto readDto = new FinalFieldDto("secret");
        FinalFieldDto writeDto = new FinalFieldDto("secret");

        FieldSecurityUtil.filterRead(readDto);
        FieldSecurityUtil.filterWrite(writeDto);

        assertNull(readDto.secret);
        assertNull(writeDto.secret);
    }

    @Test
    void privateDefaultPrimitiveFallbackReturnsNullForUnsupportedType() throws Exception {
        Method method =
                FieldSecurityUtil.class.getDeclaredMethod("defaultPrimitiveValue", Class.class);
        method.setAccessible(true);
        Object value = method.invoke(null, String.class);

        assertNull(value);
    }

    @Test
    void resolveMetadataHandlesIllegalAccessByReturningEmptyUsingHiddenPackageClass()
            throws Exception {
        Class<?> hiddenType =
                Class.forName(HiddenSecurityFixtures.hiddenMetadataCarrierType().getName());
        assertNotNull(hiddenType);
        assertTrue(FieldSecurityUtil.canReadField(hiddenType, "secret"));
    }

    @Test
    void canReadFieldWithSecuredMetadataAndNoAuthReturnsFalse() {
        assertFalse(FieldSecurityUtil.canReadField(MetadataDto.class, "secret"));
    }

    @Test
    void canReadIgnoresBlankRoleEntries() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user",
                                "n/a",
                                Set.of(
                                        (org.springframework.security.core.GrantedAuthority)
                                                () -> "")));

        assertFalse(FieldSecurityUtil.canRead(new String[] {""}));
    }

    @Test
    void privateFilterReadInternalReturnsVisitedInstance() throws Exception {
        Method method =
                FieldSecurityUtil.class.getDeclaredMethod(
                        "filterReadInternal", Object.class, Map.class);
        method.setAccessible(true);
        MetadataDto dto = new MetadataDto();
        Map<Object, Boolean> visited = new java.util.IdentityHashMap<>();
        visited.put(dto, Boolean.TRUE);

        Object result = method.invoke(null, dto, visited);

        assertSame(dto, result);
    }

    @Test
    void privateFilterWriteReturnsVisitedInstance() throws Exception {
        Method method =
                FieldSecurityUtil.class.getDeclaredMethod(
                        "filterWrite", Object.class, Object.class, Map.class);
        method.setAccessible(true);
        MetadataDto dto = new MetadataDto();
        Map<Object, Boolean> visited = new java.util.IdentityHashMap<>();
        visited.put(dto, Boolean.TRUE);

        Object result = method.invoke(null, dto, new Object(), visited);

        assertSame(dto, result);
    }

    @Test
    void metadataWriteReadsExistingValueFromExistingMetadataType() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        ExistingMetadataOnlySource existing = new ExistingMetadataOnlySource();
        existing.backingValue = "persisted";
        MetadataTargetDto request = new MetadataTargetDto();
        request.secret = "changed";

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("persisted", request.secret);
    }

    @Test
    void metadataWritePrefersFirstDuplicateExistingRuleWithoutFieldFallback() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        DuplicateExistingMetadataOnlySource existing = new DuplicateExistingMetadataOnlySource();
        existing.primary = "persisted-primary";
        existing.secondary = "persisted-secondary";
        MetadataTargetDto request = new MetadataTargetDto();
        request.secret = "changed";

        FieldSecurityUtil.filterWrite(request, existing);

        assertEquals("persisted-primary", request.secret);
    }

    @Test
    void skipOnDeniedWriteStillEvaluatesNestedFailOnDeniedForReflectionPath() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        ReflectionOuterExisting existing = new ReflectionOuterExisting();
        existing.inner = InnerFailingWrite.of("persisted");
        ReflectionOuterRequest request = new ReflectionOuterRequest();
        request.inner = InnerFailingWrite.of("changed");

        assertThrows(
                AccessDeniedException.class,
                () -> FieldSecurityUtil.filterWrite(request, existing));
    }

    @Test
    void skipOnDeniedWriteStillEvaluatesNestedFailOnDeniedForMetadataPath() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
        MetadataOuterExisting existing = new MetadataOuterExisting();
        existing.inner = InnerFailingWrite.of("persisted");
        MetadataOuterRequest request = new MetadataOuterRequest();
        request.inner = InnerFailingWrite.of("changed");

        assertThrows(
                AccessDeniedException.class,
                () -> FieldSecurityUtil.filterWrite(request, existing));
    }

    private static void clearMetadataCache() {
        FieldSecurityUtil.clearMetadataCacheForTesting();
    }

    private static int metadataCacheSize() {
        try {
            java.lang.reflect.Field cacheField =
                    FieldSecurityUtil.class.getDeclaredField("METADATA_CACHE");
            cacheField.setAccessible(true);
            Object value = cacheField.get(null);
            if (value instanceof Map<?, ?> map) {
                return map.size();
            }
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to read metadata cache", ex);
        }
        return -1;
    }

    static class SensitiveDto {
        @FieldSecurity(
                readRoles = {"ADMIN"},
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.FAIL_ON_DENIED)
        private String secret;
    }

    static class SkipDto {
        @FieldSecurity(
                readRoles = {"ADMIN"},
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private String secret;
    }

    static class MetadataDto {
        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public static FieldSecurityMetadata<MetadataDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    MetadataDto::getSecret,
                                    (dto, value) -> dto.setSecret((String) value),
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    record RecordMetadataDto(String secret, String visible, int missing, List<RecordChild> children) {
        public static FieldSecurityMetadata<RecordMetadataDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    RecordMetadataDto::secret,
                                    null,
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED),
                            new FieldSecurityMetadata.FieldRule<>(
                                    "visible",
                                    RecordMetadataDto::visible,
                                    null,
                                    false,
                                    List.of("ALL"),
                                    List.of("ALL"),
                                    WritePolicy.SKIP_ON_DENIED),
                            new FieldSecurityMetadata.FieldRule<>(
                                    "children",
                                    RecordMetadataDto::children,
                                    null,
                                    false,
                                    List.of("ALL"),
                                    List.of("ALL"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    record RecordChild(String secret) {
        public static FieldSecurityMetadata<RecordChild> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    RecordChild::secret,
                                    null,
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    record RecordFailDto(String secret) {
        public static FieldSecurityMetadata<RecordFailDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    RecordFailDto::secret,
                                    null,
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.FAIL_ON_DENIED)));
        }
    }

    static class AllRoleDto {
        @FieldSecurity(readRoles = {"ALL"})
        private String openSecret;
    }

    static class NoAnnotationFieldDto {
        private String visible;
    }

    static class NestedDto {
        @FieldSecurity(
                readRoles = {"ADMIN"},
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private String secret;

        static NestedDto of(String secret) {
            NestedDto dto = new NestedDto();
            dto.secret = secret;
            return dto;
        }
    }

    static class RecursiveContainer {
        private NestedDto child;
        private NestedDto[] array;
        private List<NestedDto> list;
        private String primitiveText;
    }

    static class SelfRefDto {
        @FieldSecurity(readRoles = {"ADMIN"})
        private String secret;

        private SelfRefDto self;
    }

    static class WriteRecursiveContainer {
        private NestedDto child;
        private NestedDto[] array;
        private List<NestedDto> list;

        static WriteRecursiveContainer sample(String prefix) {
            WriteRecursiveContainer container = new WriteRecursiveContainer();
            container.child = NestedDto.of(prefix + "-child");
            container.array = new NestedDto[] {NestedDto.of(prefix + "-array")};
            container.list = new ArrayList<>(List.of(NestedDto.of(prefix + "-list")));
            return container;
        }
    }

    static class PrimitiveDto {
        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private boolean booleanValue;

        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private byte byteValue;

        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private short shortValue;

        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private int intValue;

        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private long longValue;

        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private float floatValue;

        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private double doubleValue;

        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private char charValue;

        static PrimitiveDto nonDefault() {
            PrimitiveDto dto = new PrimitiveDto();
            dto.booleanValue = true;
            dto.byteValue = 7;
            dto.shortValue = 9;
            dto.intValue = 11;
            dto.longValue = 13L;
            dto.floatValue = 15f;
            dto.doubleValue = 17d;
            dto.charValue = 'x';
            return dto;
        }
    }

    static class NonStaticMetadataDto {
        @FieldSecurity(readRoles = {"ADMIN"})
        private String secret;

        public FieldSecurityMetadata<NonStaticMetadataDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(List.of());
        }
    }

    static class RuntimeThrowingMetadataDto {
        public static FieldSecurityMetadata<RuntimeThrowingMetadataDto> fieldSecurityMetadata() {
            throw new IllegalArgumentException("boom");
        }
    }

    static class CheckedThrowingMetadataDto {
        public static FieldSecurityMetadata<CheckedThrowingMetadataDto> fieldSecurityMetadata()
                throws Exception {
            throw new Exception("boom");
        }
    }

    static class NonMetadataReturnDto {
        @FieldSecurity(readRoles = {"ADMIN"})
        private String secret;

        public static Object fieldSecurityMetadata() {
            return "not-metadata";
        }
    }

    static class MetadataFailDto {
        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public static FieldSecurityMetadata<MetadataFailDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    MetadataFailDto::getSecret,
                                    (dto, value) -> dto.setSecret((String) value),
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.FAIL_ON_DENIED)));
        }
    }

    static class MetadataNestedDto {
        private NestedDto nested;

        public NestedDto getNested() {
            return nested;
        }

        public void setNested(NestedDto nested) {
            this.nested = nested;
        }

        public static FieldSecurityMetadata<MetadataNestedDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "nested",
                                    MetadataNestedDto::getNested,
                                    (dto, value) -> dto.setNested((NestedDto) value),
                                    false,
                                    List.of(),
                                    List.of(),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class MetadataNoWriterDto {
        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public static FieldSecurityMetadata<MetadataNoWriterDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    MetadataNoWriterDto::getSecret,
                                    null,
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class DuplicateExistingMetadataDto {
        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public static FieldSecurityMetadata<DuplicateExistingMetadataDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    DuplicateExistingMetadataDto::getSecret,
                                    (dto, value) -> dto.setSecret((String) value),
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED),
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    DuplicateExistingMetadataDto::getSecret,
                                    (dto, value) -> dto.setSecret((String) value),
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class ReflectionExistingDto {
        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public static FieldSecurityMetadata<ReflectionExistingDto> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    ReflectionExistingDto::getSecret,
                                    (dto, value) -> dto.setSecret((String) value),
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class PlainDto {
        String value;
    }

    static class ExistingWithoutField {
        private String other = "other";

        @SuppressWarnings("unused")
        public static FieldSecurityMetadata<ExistingWithoutField> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "other",
                                    dto -> dto.other,
                                    (dto, value) -> dto.other = (String) value,
                                    false,
                                    List.of(),
                                    List.of(),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class MissingFieldMetadataDto {
        String secret;

        public static FieldSecurityMetadata<MissingFieldMetadataDto> fieldSecurityMetadata() {
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

    static class FinalFieldDto {
        @FieldSecurity(
                readRoles = {"ADMIN"},
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private final String secret;

        FinalFieldDto(String secret) {
            this.secret = secret;
        }
    }

    static class MetadataTargetDto {
        String secret;

        public static FieldSecurityMetadata<MetadataTargetDto> fieldSecurityMetadata() {
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

    static class ExistingMetadataOnlySource {
        String backingValue;

        public static FieldSecurityMetadata<ExistingMetadataOnlySource> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    dto -> dto.backingValue,
                                    (dto, value) -> dto.backingValue = (String) value,
                                    false,
                                    List.of(),
                                    List.of(),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class DuplicateExistingMetadataOnlySource {
        String primary;
        String secondary;

        public static FieldSecurityMetadata<DuplicateExistingMetadataOnlySource>
                fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    dto -> dto.primary,
                                    (dto, value) -> dto.primary = (String) value,
                                    false,
                                    List.of(),
                                    List.of(),
                                    WritePolicy.SKIP_ON_DENIED),
                            new FieldSecurityMetadata.FieldRule<>(
                                    "secret",
                                    dto -> dto.secondary,
                                    (dto, value) -> dto.secondary = (String) value,
                                    false,
                                    List.of(),
                                    List.of(),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class InnerFailingWrite {
        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.FAIL_ON_DENIED)
        private String locked;

        static InnerFailingWrite of(String value) {
            InnerFailingWrite inner = new InnerFailingWrite();
            inner.locked = value;
            return inner;
        }
    }

    static class ReflectionOuterRequest {
        @FieldSecurity(
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private InnerFailingWrite inner;
    }

    static class ReflectionOuterExisting {
        private InnerFailingWrite inner;
    }

    static class MetadataOuterRequest {
        private InnerFailingWrite inner;

        public static FieldSecurityMetadata<MetadataOuterRequest> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "inner",
                                    dto -> dto.inner,
                                    (dto, value) -> dto.inner = (InnerFailingWrite) value,
                                    true,
                                    List.of("ADMIN"),
                                    List.of("ADMIN"),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }

    static class MetadataOuterExisting {
        private InnerFailingWrite inner;

        public static FieldSecurityMetadata<MetadataOuterExisting> fieldSecurityMetadata() {
            return FieldSecurityMetadata.of(
                    List.of(
                            new FieldSecurityMetadata.FieldRule<>(
                                    "inner",
                                    dto -> dto.inner,
                                    (dto, value) -> dto.inner = (InnerFailingWrite) value,
                                    false,
                                    List.of(),
                                    List.of(),
                                    WritePolicy.SKIP_ON_DENIED)));
        }
    }
}
