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

package nl.datasteel.crudcraft.runtime.security.row;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.security.AccessDeniedException;
import nl.datasteel.crudcraft.runtime.security.scope.PrincipalScopeAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ClaimScopedRowSecurityHandlerTest {

    @BeforeEach
    @AfterEach
    void clearPropertyCache() {
        try {
            java.lang.reflect.Field cacheField =
                    ClaimScopedRowSecurityHandler.class.getDeclaredField("PROPERTY_CACHE");
            cacheField.setAccessible(true);
            Object value = cacheField.get(null);
            if (value instanceof java.util.Map<?, ?> map) {
                map.clear();
            }
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to clear property cache", ex);
        }
    }

    @Test
    void setsScopeFieldOnCreateWhenMissing() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-1"));
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        TestEntity entity = new TestEntity();

        handler.apply(entity);

        assertEquals("t-1", entity.getTenantId());
    }

    @Test
    void rejectsScopeMismatch() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-2"));
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        TestEntity entity = new TestEntity();
        entity.setTenantId("t-1");

        assertThrows(AccessDeniedException.class, () -> handler.apply(entity));
    }

    @Test
    void missingClaimFailsClosed() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.empty());
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        TestEntity entity = new TestEntity();

        assertThrows(AccessDeniedException.class, () -> handler.apply(entity));
    }

    @Test
    void rowFilterUsesClaimValueWhenPresent() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-1"));
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        @SuppressWarnings("unchecked")
        Root<TestEntity> root = (Root<TestEntity>) mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<Object> path = (Path<Object>) mock(Path.class);
        Predicate predicate = mock(Predicate.class);
        when(root.get("tenantId")).thenReturn(path);
        doReturn(TestEntity.class).when(root).getJavaType();
        when(cb.equal(path, "t-1")).thenReturn(predicate);

        Predicate result = handler.rowFilter().toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).equal(path, "t-1");
    }

    @Test
    void rowFilterConvertsClaimValueToFieldType() {
        UUID id = UUID.randomUUID();
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of(id.toString()));
        ClaimScopedRowSecurityHandler<UuidEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        @SuppressWarnings("unchecked")
        Root<UuidEntity> root = (Root<UuidEntity>) mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<Object> path = (Path<Object>) mock(Path.class);
        Predicate predicate = mock(Predicate.class);
        when(root.get("tenantId")).thenReturn(path);
        doReturn(UuidEntity.class).when(root).getJavaType();
        when(cb.equal(path, id)).thenReturn(predicate);

        Predicate result = handler.rowFilter().toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).equal(path, id);
    }

    @Test
    void rowFilterUsesDisjunctionWhenClaimMissing() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.empty());
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        @SuppressWarnings("unchecked")
        Root<TestEntity> root = (Root<TestEntity>) mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate disjunction = mock(Predicate.class);
        when(cb.disjunction()).thenReturn(disjunction);

        Predicate result = handler.rowFilter().toPredicate(root, query, cb);

        assertSame(disjunction, result);
    }

    @Test
    void convertsClaimToLong() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("12"));
        ClaimScopedRowSecurityHandler<LongEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        LongEntity entity = new LongEntity();

        handler.apply(entity);

        assertEquals(12L, entity.getTenantId());
    }

    @Test
    void convertsClaimToInteger() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("9"));
        ClaimScopedRowSecurityHandler<IntegerEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        IntegerEntity entity = new IntegerEntity();

        handler.apply(entity);

        assertEquals(9, entity.getTenantId());
    }

    @Test
    void convertsClaimToUuid() {
        UUID id = UUID.randomUUID();
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of(id.toString()));
        ClaimScopedRowSecurityHandler<UuidEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        UuidEntity entity = new UuidEntity();

        handler.apply(entity);

        assertEquals(id, entity.getTenantId());
    }

    @Test
    void convertsClaimToEnum() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("PREMIUM"));
        ClaimScopedRowSecurityHandler<EnumEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tier", "tier", "tenant_id", accessor);
        EnumEntity entity = new EnumEntity();

        handler.apply(entity);

        assertEquals(Tier.PREMIUM, entity.getTier());
    }

    @Test
    void failsWhenScopeFieldIsReadOnlyAndUnset() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-1"));
        ClaimScopedRowSecurityHandler<ReadOnlyEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);

        assertThrows(AccessDeniedException.class, () -> handler.apply(new ReadOnlyEntity()));
    }

    @Test
    void failsWhenScopeFieldDoesNotExist() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-1"));
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "missing", "tenant_id", accessor);

        assertThrows(IllegalStateException.class, () -> handler.apply(new TestEntity()));
    }

    @Test
    void propagatesRuntimeFromPropertyAccessor() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-1"));
        ClaimScopedRowSecurityHandler<RuntimeThrowingEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);

        assertThrows(
                IllegalArgumentException.class, () -> handler.apply(new RuntimeThrowingEntity()));
    }

    @Test
    void propagatesErrorFromPropertyAccessor() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-1"));
        ClaimScopedRowSecurityHandler<ErrorThrowingEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);

        assertThrows(AssertionError.class, () -> handler.apply(new ErrorThrowingEntity()));
    }

    @Test
    void wrapsCheckedExceptionFromPropertyAccessor() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-1"));
        ClaimScopedRowSecurityHandler<CheckedThrowingEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);

        assertThrows(IllegalStateException.class, () -> handler.apply(new CheckedThrowingEntity()));
    }

    @Test
    void convertsNonStringClaimToStringWhenTargetFieldIsString() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of(42));
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        TestEntity entity = new TestEntity();

        handler.apply(entity);

        assertEquals("42", entity.getTenantId());
    }

    @Test
    void returnsOriginalValueForUnsupportedTargetTypeAndFailsFast() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("not-a-number"));
        ClaimScopedRowSecurityHandler<NumberEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        NumberEntity entity = new NumberEntity();

        assertThrows(IllegalArgumentException.class, () -> handler.apply(entity));
    }

    @Test
    void privateConvertValueCoversNullInput() throws Exception {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.empty());
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        Method method =
                ClaimScopedRowSecurityHandler.class.getDeclaredMethod(
                        "convertValue", Object.class, Class.class);
        method.setAccessible(true);

        Object value = method.invoke(handler, null, String.class);

        assertNull(value);
    }

    @Test
    void applyWrapsIllegalAccessForPackagePrivateForeignEntity() throws Exception {
        Class<?> type =
                Class.forName(
                        "nl.datasteel.crudcraft.runtime.security.row.fixture.PackagePrivateScopedEntity");
        Constructor<?> ctor = type.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object entity = ctor.newInstance();
        assertNotNull(entity);

        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-1"));
        @SuppressWarnings("unchecked")
        ClaimScopedRowSecurityHandler<Object> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);

        assertThrows(IllegalStateException.class, () -> handler.apply(entity));
    }

    @Test
    void supportsPrimitiveLongAndIntConversion() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("0"));

        ClaimScopedRowSecurityHandler<PrimitiveLongEntity> longHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        PrimitiveLongEntity longEntity = new PrimitiveLongEntity();
        longHandler.apply(longEntity);
        assertEquals(0L, longEntity.getTenantId());

        ClaimScopedRowSecurityHandler<PrimitiveIntEntity> intHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        PrimitiveIntEntity intEntity = new PrimitiveIntEntity();
        intHandler.apply(intEntity);
        assertEquals(0, intEntity.getTenantId());
    }

    @Test
    void treatsConvertedCurrentValueAsMatch() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of(42));
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        TestEntity entity = new TestEntity();
        entity.setTenantId("42");

        handler.apply(entity);

        assertEquals("42", entity.getTenantId());
    }

    @Test
    void writeOnlyScopePropertyIsDeniedAsUnreadable() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("x"));
        ClaimScopedRowSecurityHandler<WriteOnlyScopeEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);

        assertThrows(AccessDeniedException.class, () -> handler.apply(new WriteOnlyScopeEntity()));
    }

    @Test
    void rejectsNullAndBlankConstructorArguments() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);

        assertThrows(
                NullPointerException.class,
                () -> new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", null));
        assertThrows(
                NullPointerException.class,
                () -> new ClaimScopedRowSecurityHandler<>(null, "tenantId", "tenant_id", accessor));
        assertThrows(
                NullPointerException.class,
                () -> new ClaimScopedRowSecurityHandler<>("tenant", null, "tenant_id", accessor));
        assertThrows(
                NullPointerException.class,
                () -> new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", null, accessor));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ClaimScopedRowSecurityHandler<>(" ", "tenantId", "tenant_id", accessor));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ClaimScopedRowSecurityHandler<>("tenant", " ", "tenant_id", accessor));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", " ", accessor));
    }

    @Test
    void applyRejectsNullEntity() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("t-1"));
        ClaimScopedRowSecurityHandler<TestEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);

        assertThrows(NullPointerException.class, () -> handler.apply(null));
    }

    @Test
    void convertsClaimToBooleanAndRejectsInvalidBoolean() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("true"));
        ClaimScopedRowSecurityHandler<BooleanEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantFlag", "tenant_id", accessor);
        BooleanEntity entity = new BooleanEntity();

        handler.apply(entity);
        assertEquals(Boolean.TRUE, entity.getTenantFlag());

        when(accessor.claim("tenant_id")).thenReturn(Optional.of("not-bool"));
        assertThrows(IllegalArgumentException.class, () -> handler.apply(new BooleanEntity()));
    }

    @Test
    void convertsClaimToAdditionalNumericTypes() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("7"));

        ClaimScopedRowSecurityHandler<ShortEntity> shortHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        ShortEntity shortEntity = new ShortEntity();
        shortHandler.apply(shortEntity);
        assertEquals((short) 7, shortEntity.getTenantId());

        ClaimScopedRowSecurityHandler<ByteEntity> byteHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        ByteEntity byteEntity = new ByteEntity();
        byteHandler.apply(byteEntity);
        assertEquals((byte) 7, byteEntity.getTenantId());

        when(accessor.claim("tenant_id")).thenReturn(Optional.of("7.5"));
        ClaimScopedRowSecurityHandler<DoubleEntity> doubleHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        DoubleEntity doubleEntity = new DoubleEntity();
        doubleHandler.apply(doubleEntity);
        assertEquals(7.5d, doubleEntity.getTenantId());

        ClaimScopedRowSecurityHandler<FloatEntity> floatHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        FloatEntity floatEntity = new FloatEntity();
        floatHandler.apply(floatEntity);
        assertEquals(7.5f, floatEntity.getTenantId());
    }

    @Test
    void convertsClaimToPrimitiveBooleanAndBooleanFalse() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("false"));
        ClaimScopedRowSecurityHandler<PrimitiveBooleanEntity> handler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantFlag", "tenant_id", accessor);
        PrimitiveBooleanEntity entity = new PrimitiveBooleanEntity();

        handler.apply(entity);

        assertFalse(entity.isTenantFlag());
    }

    @Test
    void convertsClaimToWrapperNumericTypes() {
        PrincipalScopeAccessor accessor = mock(PrincipalScopeAccessor.class);
        when(accessor.claim("tenant_id")).thenReturn(Optional.of("12"));
        ClaimScopedRowSecurityHandler<WrapperShortEntity> shortHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        WrapperShortEntity shortEntity = new WrapperShortEntity();
        shortHandler.apply(shortEntity);
        assertEquals(Short.valueOf((short) 12), shortEntity.getTenantId());

        ClaimScopedRowSecurityHandler<WrapperByteEntity> byteHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        WrapperByteEntity byteEntity = new WrapperByteEntity();
        byteHandler.apply(byteEntity);
        assertEquals(Byte.valueOf((byte) 12), byteEntity.getTenantId());

        when(accessor.claim("tenant_id")).thenReturn(Optional.of("12.25"));
        ClaimScopedRowSecurityHandler<WrapperDoubleEntity> doubleHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        WrapperDoubleEntity doubleEntity = new WrapperDoubleEntity();
        doubleHandler.apply(doubleEntity);
        assertEquals(Double.valueOf(12.25d), doubleEntity.getTenantId());

        ClaimScopedRowSecurityHandler<WrapperFloatEntity> floatHandler =
                new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", accessor);
        WrapperFloatEntity floatEntity = new WrapperFloatEntity();
        floatHandler.apply(floatEntity);
        assertEquals(Float.valueOf(12.25f), floatEntity.getTenantId());
    }

    @Test
    void privateIsUnsetValueCoversCharAndDefaultPrimitiveFallback() throws Exception {
        Method method =
                ClaimScopedRowSecurityHandler.class.getDeclaredMethod(
                        "isUnsetValue", Object.class, Class.class);
        method.setAccessible(true);

        assertEquals(Boolean.TRUE, method.invoke(null, '\0', char.class));
        assertEquals(Boolean.FALSE, method.invoke(null, 'x', char.class));
        assertEquals(Boolean.FALSE, method.invoke(null, "value", void.class));
    }

    public static class TestEntity {
        private String tenantId;

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class LongEntity {
        private Long tenantId;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class IntegerEntity {
        private Integer tenantId;

        public Integer getTenantId() {
            return tenantId;
        }

        public void setTenantId(Integer tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class UuidEntity {
        private UUID tenantId;

        public UUID getTenantId() {
            return tenantId;
        }

        public void setTenantId(UUID tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class EnumEntity {
        private Tier tier;

        public Tier getTier() {
            return tier;
        }

        public void setTier(Tier tier) {
            this.tier = tier;
        }
    }

    public static class ReadOnlyEntity {
        private String tenantId;

        public String getTenantId() {
            return tenantId;
        }
    }

    public static class RuntimeThrowingEntity {
        public String getTenantId() {
            throw new IllegalArgumentException("boom");
        }

        public void setTenantId(String tenantId) {}
    }

    public static class ErrorThrowingEntity {
        public String getTenantId() {
            throw new AssertionError("boom");
        }

        public void setTenantId(String tenantId) {}
    }

    public static class CheckedThrowingEntity {
        public String getTenantId() throws Exception {
            throw new Exception("boom");
        }

        public void setTenantId(String tenantId) {}
    }

    public enum Tier {
        BASIC,
        PREMIUM
    }

    public static class NumberEntity {
        private Number tenantId;

        public Number getTenantId() {
            return tenantId;
        }

        public void setTenantId(Number tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class PrimitiveLongEntity {
        private long tenantId;

        public long getTenantId() {
            return tenantId;
        }

        public void setTenantId(long tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class PrimitiveIntEntity {
        private int tenantId;

        public int getTenantId() {
            return tenantId;
        }

        public void setTenantId(int tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class WriteOnlyScopeEntity {
        private String tenantId;

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class BooleanEntity {
        private Boolean tenantFlag;

        public Boolean getTenantFlag() {
            return tenantFlag;
        }

        public void setTenantFlag(Boolean tenantFlag) {
            this.tenantFlag = tenantFlag;
        }
    }

    public static class ShortEntity {
        private short tenantId;

        public short getTenantId() {
            return tenantId;
        }

        public void setTenantId(short tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class ByteEntity {
        private byte tenantId;

        public byte getTenantId() {
            return tenantId;
        }

        public void setTenantId(byte tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class DoubleEntity {
        private double tenantId;

        public double getTenantId() {
            return tenantId;
        }

        public void setTenantId(double tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class FloatEntity {
        private float tenantId;

        public float getTenantId() {
            return tenantId;
        }

        public void setTenantId(float tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class PrimitiveBooleanEntity {
        private boolean tenantFlag;

        public boolean isTenantFlag() {
            return tenantFlag;
        }

        public void setTenantFlag(boolean tenantFlag) {
            this.tenantFlag = tenantFlag;
        }
    }

    public static class WrapperShortEntity {
        private Short tenantId;

        public Short getTenantId() {
            return tenantId;
        }

        public void setTenantId(Short tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class WrapperByteEntity {
        private Byte tenantId;

        public Byte getTenantId() {
            return tenantId;
        }

        public void setTenantId(Byte tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class WrapperDoubleEntity {
        private Double tenantId;

        public Double getTenantId() {
            return tenantId;
        }

        public void setTenantId(Double tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class WrapperFloatEntity {
        private Float tenantId;

        public Float getTenantId() {
            return tenantId;
        }

        public void setTenantId(Float tenantId) {
            this.tenantId = tenantId;
        }
    }
}
