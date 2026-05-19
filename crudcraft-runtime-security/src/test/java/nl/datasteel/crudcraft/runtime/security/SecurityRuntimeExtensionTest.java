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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.annotations.security.RowPredicate;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.row.RowSecurityRuntimeExtension;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class SecurityRuntimeExtensionTest {

    @Test
    void fieldExtensionAppliesReadAndWriteFiltering() {
        RecordingFieldSecurityAdapter adapter = new RecordingFieldSecurityAdapter();
        FieldSecurityRuntimeExtension<TestEntity, TestRequest> extension =
                new FieldSecurityRuntimeExtension<>(adapter);

        TestRequest request = new TestRequest("new");
        TestResponse response = new TestResponse("shown");

        assertEquals(request, extension.beforeCreate(request));
        assertEquals(request, extension.beforeUpdate(request, new TestEntity("old")));
        assertEquals(response, extension.afterRead(response));
        assertEquals(2, adapter.writeCalls);
        assertEquals(1, adapter.readCalls);
    }

    @Test
    void fieldExtensionRejectsNullAdapter() {
        assertThrows(
                NullPointerException.class,
                () -> new FieldSecurityRuntimeExtension<TestEntity, TestRequest>(null));
    }

    @Test
    void fieldExtensionCanAssertDeniedReadFieldsAreRedacted() {
        FieldSecurityRuntimeExtension<TestEntity, TestRequest> extension =
                new FieldSecurityRuntimeExtension<>(new DenyingAdapter(), true);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> extension.afterRead(new TestResponse("secret")));
        assertEquals(
                "Field-security adapter did not redact denied field: "
                        + TestResponse.class.getName()
                        + ".value",
                exception.getMessage());
    }

    @Test
    void fieldExtensionKeepsPerThreadReadFilteringIsolated()
            throws InterruptedException, ExecutionException {
        ThreadLocalFilteringAdapter adapter = new ThreadLocalFilteringAdapter();
        FieldSecurityRuntimeExtension<TestEntity, TestRequest> extension =
                new FieldSecurityRuntimeExtension<>(adapter, true);
        TestResponse shared = new TestResponse("secret");
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                boolean allow = i % 2 == 0;
                tasks.add(
                        () -> {
                            adapter.allowRead.set(allow);
                            try {
                                TestResponse filtered = extension.afterRead(shared);
                                return allow
                                        ? "secret".equals(filtered.value())
                                        : filtered.value() == null;
                            } finally {
                                adapter.allowRead.remove();
                            }
                        });
            }

            for (Future<Boolean> result : executor.invokeAll(tasks)) {
                assertEquals(Boolean.TRUE, result.get());
            }
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void fieldExtensionAssertsPojoDeniedFieldMustBeRedacted() {
        FieldSecurityRuntimeExtension<TestEntity, TestRequest> extension =
                new FieldSecurityRuntimeExtension<>(new DenyingPojoAdapter(), true);

        PojoView original = new PojoView("secret", "ok", null, null, null, AccessLevel.USER);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> extension.afterRead(original));
        assertEquals(
                "Field-security adapter did not redact denied field: "
                        + PojoView.class.getName()
                        + ".secret",
                exception.getMessage());
    }

    @Test
    void fieldExtensionValidatesNestedPojoArrayAndCollectionRedaction() {
        FieldSecurityRuntimeExtension<TestEntity, TestRequest> extension =
                new FieldSecurityRuntimeExtension<>(new RedactingPojoAdapter(), true);

        NestedPojo nested = new NestedPojo("nested-secret", "nested-public");
        PojoView original =
                new PojoView(
                        "top-secret",
                        "top-public",
                        nested,
                        new NestedPojo[] {new NestedPojo("array-secret", "array-public")},
                        Arrays.asList(new NestedPojo("list-secret", "list-public")),
                        AccessLevel.ADMIN);

        PojoView filtered = extension.afterRead(original);

        assertEquals("top-public", filtered.publicValue);
        assertNull(filtered.secret);
        assertNotNull(filtered.nested);
        assertNull(filtered.nested.secret);
        assertNull(filtered.arrayItems[0].secret);
        assertNull(filtered.listItems.getFirst().secret);
        assertEquals(AccessLevel.ADMIN, filtered.level);
    }

    @Test
    void rowExtensionComposesReadFilterAndAppliesWriteChecks() {
        RecordingRowSecurityHandler handler = new RecordingRowSecurityHandler();
        RowSecurityRuntimeExtension<TestEntity, TestRequest> extension =
                new RowSecurityRuntimeExtension<>(List.of(handler));

        assertNotNull(extension.readFilter(TestEntity.class));
        extension.beforeSave(new TestEntity("save"));
        extension.beforeDelete(new TestEntity("delete"));

        assertEquals(2, handler.applyCalls);
        assertEquals(1, handler.filterCalls);
    }

    @Test
    void rowExtensionHandlesNullHandlerList() {
        RowSecurityRuntimeExtension<TestEntity, TestRequest> extension =
                new RowSecurityRuntimeExtension<>(null);

        assertNull(extension.readFilter(TestEntity.class));
        extension.beforeSave(new TestEntity("save"));
        extension.beforeDelete(new TestEntity("delete"));
    }

    @Test
    void rowExtensionSkipsNullFiltersAndCombinesNonNullFilters() {
        RecordingRowSecurityHandler first = new RecordingRowSecurityHandler();
        first.returnNullSpecification = true;
        RecordingRowSecurityHandler second = new RecordingRowSecurityHandler();
        RecordingRowSecurityHandler third = new RecordingRowSecurityHandler();
        RowSecurityRuntimeExtension<TestEntity, TestRequest> extension =
                new RowSecurityRuntimeExtension<>(List.of(first, second, third));

        assertNotNull(extension.readFilter(TestEntity.class));
        assertEquals(1, first.filterCalls);
        assertEquals(1, second.filterCalls);
        assertEquals(1, third.filterCalls);
    }

    @Test
    void rowExtensionRejectsNullHandlerEntry() {
        assertThrows(
                NullPointerException.class,
                () ->
                        new RowSecurityRuntimeExtension<TestEntity, TestRequest>(
                                java.util.Arrays.asList((RowSecurityHandler<TestEntity>) null)));
    }

    private static final class RecordingFieldSecurityAdapter implements FieldSecurityAdapter {
        private int readCalls;
        private int writeCalls;

        @Override
        public <T> T filterRead(T dto) {
            readCalls++;
            return dto;
        }

        @Override
        public <T> T filterWrite(T request, Object existing) {
            writeCalls++;
            return request;
        }
    }

    private static final class DenyingAdapter implements FieldSecurityAdapter {
        @Override
        public boolean canReadField(Class<?> dtoType, String fieldName) {
            return false;
        }
    }

    private static final class ThreadLocalFilteringAdapter implements FieldSecurityAdapter {
        private final ThreadLocal<Boolean> allowRead = ThreadLocal.withInitial(() -> false);

        @Override
        @SuppressWarnings("unchecked")
        public <T> T filterRead(T dto) {
            if (allowRead.get()) {
                return dto;
            }
            return (T) new TestResponse(null);
        }

        @Override
        public boolean canReadField(Class<?> dtoType, String fieldName) {
            return allowRead.get();
        }
    }

    private static final class DenyingPojoAdapter implements FieldSecurityAdapter {
        @Override
        public boolean canReadField(Class<?> dtoType, String fieldName) {
            return !"secret".equals(fieldName);
        }
    }

    private static final class RedactingPojoAdapter implements FieldSecurityAdapter {
        @Override
        public boolean canReadField(Class<?> dtoType, String fieldName) {
            return !"secret".equals(fieldName);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T filterRead(T dto) {
            if (dto instanceof PojoView view) {
                NestedPojo nested = view.nested == null ? null : redactNested(view.nested);
                NestedPojo[] array =
                        view.arrayItems == null
                                ? null
                                : Arrays.stream(view.arrayItems)
                                        .map(this::redactNested)
                                        .toArray(NestedPojo[]::new);
                List<NestedPojo> list =
                        view.listItems == null
                                ? null
                                : view.listItems.stream().map(this::redactNested).toList();
                return (T)
                        new PojoView(
                                null, view.publicValue, nested, array, list, view.level);
            }
            return dto;
        }

        private NestedPojo redactNested(NestedPojo value) {
            return value == null ? null : new NestedPojo(null, value.publicValue);
        }
    }

    private static final class RecordingRowSecurityHandler
            implements RowSecurityHandler<TestEntity> {
        private int filterCalls;
        private int applyCalls;
        private boolean returnNullSpecification;

        @Override
        public RowPredicate<TestEntity> rowFilter() {
            filterCalls++;
            if (returnNullSpecification) {
                return null;
            }
            return (root, query, cb) -> cb.conjunction();
        }

        @Override
        public void apply(TestEntity entity) {
            applyCalls++;
        }
    }

    private record TestEntity(String value) {}

    private record TestRequest(String value) {}

    private record TestResponse(String value) {}

    private enum AccessLevel {
        USER,
        ADMIN
    }

    private static final class NestedPojo {
        private final String secret;
        private final String publicValue;

        private NestedPojo(String secret, String publicValue) {
            this.secret = secret;
            this.publicValue = publicValue;
        }
    }

    private static final class PojoView {
        private static final String STATIC_MARKER = "STATIC";
        private final String secret;
        private final String publicValue;
        private final NestedPojo nested;
        private final NestedPojo[] arrayItems;
        private final List<NestedPojo> listItems;
        private final AccessLevel level;

        private PojoView(
                String secret,
                String publicValue,
                NestedPojo nested,
                NestedPojo[] arrayItems,
                List<NestedPojo> listItems,
                AccessLevel level) {
            this.secret = secret;
            this.publicValue = publicValue;
            this.nested = nested;
            this.arrayItems = arrayItems;
            this.listItems = listItems;
            this.level = level;
        }
    }
}
