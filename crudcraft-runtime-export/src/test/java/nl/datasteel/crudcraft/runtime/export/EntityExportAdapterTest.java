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

package nl.datasteel.crudcraft.runtime.export;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class EntityExportAdapterTest {

    @Test
    void iteratorFetchesPagesAndRespectsMaxItems() {
        EntityExportService exportService = mock(EntityExportService.class);
        EntitySerializer serializer = mock(EntitySerializer.class);
        EntityExportAdapter adapter = new EntityExportAdapter(exportService, serializer);
        ExportRequest request = new ExportRequest();

        DemoEntity e1 = new DemoEntity("one");
        DemoEntity e2 = new DemoEntity("two");
        DemoEntity e3 = new DemoEntity("three");

        when(exportService.fetchWithRelationships(eq(DemoEntity.class), eq(request), any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(e1, e2)))
                .thenReturn(new PageImpl<>(java.util.List.of(e3)))
                .thenReturn(Page.empty());
        when(serializer.serialize(eq(e1), eq(request))).thenReturn(Map.of("name", "one"));
        when(serializer.serialize(eq(e2), eq(request))).thenReturn(Map.of("name", "two"));
        when(serializer.serialize(eq(e3), eq(request))).thenReturn(Map.of("name", "three"));

        Iterator<Map<String, Object>> iterator =
                adapter.createIterator(DemoEntity.class, request, 2, 10);

        assertTrue(iterator.hasNext());
        assertEquals("one", iterator.next().get("name"));
        assertTrue(iterator.hasNext());
        assertEquals("two", iterator.next().get("name"));
        assertFalse(iterator.hasNext());
        assertThrows(java.util.NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorStopsWhenServiceReturnsEmptyPage() {
        EntityExportService exportService = mock(EntityExportService.class);
        EntitySerializer serializer = mock(EntitySerializer.class);
        EntityExportAdapter adapter = new EntityExportAdapter(exportService, serializer);
        ExportRequest request = new ExportRequest();

        when(exportService.fetchWithRelationships(eq(DemoEntity.class), eq(request), any(), any()))
                .thenReturn(Page.empty());

        Iterator<Map<String, Object>> iterator =
                adapter.createIterator(DemoEntity.class, request, 5, 2);

        assertFalse(iterator.hasNext());
        assertThrows(java.util.NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorUsesNonTruncatingPathWhenUnderLimit() {
        EntityExportService exportService = mock(EntityExportService.class);
        EntitySerializer serializer = mock(EntitySerializer.class);
        EntityExportAdapter adapter = new EntityExportAdapter(exportService, serializer);
        ExportRequest request = new ExportRequest();

        DemoEntity e1 = new DemoEntity("one");
        when(exportService.fetchWithRelationships(eq(DemoEntity.class), eq(request), any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(e1)))
                .thenReturn(Page.empty());
        when(serializer.serialize(eq(e1), eq(request))).thenReturn(Map.of("name", "one"));

        Iterator<Map<String, Object>> iterator =
                adapter.createIterator(DemoEntity.class, request, 5, 2);
        assertTrue(iterator.hasNext());
        assertEquals("one", iterator.next().get("name"));
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorTruncatesWhenPageExceedsRemainingLimit() {
        EntityExportService exportService = mock(EntityExportService.class);
        EntitySerializer serializer = mock(EntitySerializer.class);
        EntityExportAdapter adapter = new EntityExportAdapter(exportService, serializer);
        ExportRequest request = new ExportRequest();

        DemoEntity e1 = new DemoEntity("one");
        DemoEntity e2 = new DemoEntity("two");
        when(exportService.fetchWithRelationships(eq(DemoEntity.class), eq(request), any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(e1, e2)));
        when(serializer.serialize(eq(e1), eq(request))).thenReturn(Map.of("name", "one"));
        when(serializer.serialize(eq(e2), eq(request))).thenReturn(Map.of("name", "two"));

        Iterator<Map<String, Object>> iterator =
                adapter.createIterator(DemoEntity.class, request, 1, 10);
        assertTrue(iterator.hasNext());
        assertEquals("one", iterator.next().get("name"));
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorWithZeroLimitDoesNotCallService() {
        EntityExportService exportService = mock(EntityExportService.class);
        EntitySerializer serializer = mock(EntitySerializer.class);
        EntityExportAdapter adapter = new EntityExportAdapter(exportService, serializer);
        ExportRequest request = new ExportRequest();

        Iterator<Map<String, Object>> iterator =
                adapter.createIterator(DemoEntity.class, request, 0, 10);

        assertFalse(iterator.hasNext());
        verify(exportService, never())
                .fetchWithRelationships(eq(DemoEntity.class), eq(request), any(), any());
    }

    @Test
    void iteratorTruncationUsesRemainingSlotsAfterPreviousFetch() {
        EntityExportService exportService = mock(EntityExportService.class);
        EntitySerializer serializer = mock(EntitySerializer.class);
        EntityExportAdapter adapter = new EntityExportAdapter(exportService, serializer);
        ExportRequest request = new ExportRequest();

        DemoEntity e1 = new DemoEntity("one");
        DemoEntity e2 = new DemoEntity("two");
        DemoEntity e3 = new DemoEntity("three");
        when(exportService.fetchWithRelationships(eq(DemoEntity.class), eq(request), any(), any()))
                .thenReturn(new PageImpl<>(List.of(e1)))
                .thenReturn(new PageImpl<>(List.of(e2, e3)))
                .thenReturn(Page.empty());
        when(serializer.serialize(eq(e1), eq(request))).thenReturn(Map.of("name", "one"));
        when(serializer.serialize(eq(e2), eq(request))).thenReturn(Map.of("name", "two"));
        when(serializer.serialize(eq(e3), eq(request))).thenReturn(Map.of("name", "three"));

        Iterator<Map<String, Object>> iterator =
                adapter.createIterator(DemoEntity.class, request, 2, 10);

        assertEquals("one", iterator.next().get("name"));
        assertEquals("two", iterator.next().get("name"));
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorDoesNotUseSubListWhenPageExactlyMatchesLimit() throws Exception {
        EntityExportService exportService = mock(EntityExportService.class);
        EntitySerializer serializer = mock(EntitySerializer.class);
        EntityExportAdapter adapter = new EntityExportAdapter(exportService, serializer);
        ExportRequest request = new ExportRequest();

        DemoEntity e1 = new DemoEntity("one");
        DemoEntity e2 = new DemoEntity("two");
        when(exportService.fetchWithRelationships(eq(DemoEntity.class), eq(request), any(), any()))
                .thenReturn(new PageImpl<>(List.of(e1, e2)));
        when(serializer.serialize(eq(e1), eq(request))).thenReturn(Map.of("name", "one"));
        when(serializer.serialize(eq(e2), eq(request))).thenReturn(Map.of("name", "two"));

        Iterator<Map<String, Object>> iterator =
                adapter.createIterator(DemoEntity.class, request, 2, 10);
        assertTrue(iterator.hasNext());

        Field currentField = iterator.getClass().getDeclaredField("current");
        currentField.setAccessible(true);
        Object current = currentField.get(iterator);
        assertFalse(current.getClass().getName().contains("SubList"));
    }

    private record DemoEntity(String name) {}
}
