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

package nl.datasteel.crudcraft.runtime.export.service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.exception.ExportLimitExceededException;
import nl.datasteel.crudcraft.runtime.export.EntityExportAdapter;
import nl.datasteel.crudcraft.runtime.export.ExportDepthExceededException;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class EnhancedExportServiceTest {

    @Test
    void usesDtoModeWhenExportRequestIsNullOrDto() {
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(config(), null, DemoEntity.class);

        ResponseEntity<StreamingResponseBody> nullRequestResponse =
                service.export(
                        "q",
                        1,
                        "csv",
                        null,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());

        ExportRequest dtoRequest = new ExportRequest();
        ResponseEntity<StreamingResponseBody> dtoRequestResponse =
                service.export(
                        "q",
                        1,
                        "csv",
                        dtoRequest,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());

        assertEquals(HttpStatus.OK, nullRequestResponse.getStatusCode());
        assertEquals(HttpStatus.OK, dtoRequestResponse.getStatusCode());
    }

    @Test
    void throwsWhenEntityModeIsRequestedWithoutAdapter() {
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(config(), null, DemoEntity.class);
        ExportRequest entityMode = new ExportRequest();
        entityMode.setExportMode(ExportRequest.ExportMode.ENTITY);

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.export(
                                "q",
                                1,
                                "csv",
                                entityMode,
                                page -> new PageImpl<>(List.of("x")),
                                Function.identity()));
    }

    @Test
    void throwsWhenEntityModeIsDisabled() {
        EntityExportAdapter adapter = mock(EntityExportAdapter.class);
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(config(), adapter, DemoEntity.class, false);
        ExportRequest entityMode = new ExportRequest();
        entityMode.setExportMode(ExportRequest.ExportMode.ENTITY);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.export(
                                        "q",
                                        1,
                                        "csv",
                                        entityMode,
                                        page -> new PageImpl<>(List.of("x")),
                                        Function.identity()));

        assertTrue(exception.getMessage().contains("allow-entity-mode"));
    }

    @Test
    void entityModeValidatesInputAndStreamsOutput() throws Exception {
        EntityExportAdapter adapter = mock(EntityExportAdapter.class);
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(config(), adapter, DemoEntity.class);
        ExportRequest request = new ExportRequest();
        request.setExportMode(ExportRequest.ExportMode.ENTITY);

        Iterator<Map<String, Object>> iterator =
                List.<Map<String, Object>>of(Map.of("id", 1)).iterator();
        when(adapter.createIterator(eq(DemoEntity.class), eq(request), eq(2), anyInt(), any()))
                .thenReturn(iterator);

        ResponseEntity<StreamingResponseBody> negative =
                service.export(
                        "q",
                        -1,
                        "csv",
                        request,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());
        ResponseEntity<StreamingResponseBody> invalidFormat =
                service.export(
                        "q",
                        1,
                        "xml",
                        request,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());
        ResponseEntity<StreamingResponseBody> response =
                service.export(
                        "q",
                        2,
                        "json",
                        request,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());

        assertEquals(HttpStatus.BAD_REQUEST, negative.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, invalidFormat.getStatusCode());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getBody().writeTo(out);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("\"id\":1"));
        verify(adapter).createIterator(eq(DemoEntity.class), eq(request), eq(2), eq(2), any());
    }

    @Test
    void entityModeSupportsZeroLimit() throws Exception {
        EntityExportAdapter adapter = mock(EntityExportAdapter.class);
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(config(), adapter, DemoEntity.class);
        ExportRequest request = new ExportRequest();
        request.setExportMode(ExportRequest.ExportMode.ENTITY);

        ResponseEntity<StreamingResponseBody> response =
                service.export(
                        "q",
                        0,
                        "json",
                        request,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getBody().writeTo(out);
        assertEquals("[]", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void entityModeRejectsLimitAboveConfiguredMaximum() {
        EntityExportAdapter adapter = mock(EntityExportAdapter.class);
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(config(), adapter, DemoEntity.class);
        ExportRequest request = new ExportRequest();
        request.setExportMode(ExportRequest.ExportMode.ENTITY);

        ExportLimitExceededException exception =
                assertThrows(
                        ExportLimitExceededException.class,
                        () ->
                                service.export(
                                        "q",
                                        99,
                                        "json",
                                        request,
                                        page -> new PageImpl<>(List.of("x")),
                                        Function.identity()));

        assertTrue(exception.getMessage().contains("maximum is 2"));
    }

    @Test
    void entityModeAppliesGlobalRowCap() {
        EntityExportAdapter adapter = mock(EntityExportAdapter.class);
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(
                        new ExportService.ExportConfig(1, 5, 5, 5, 2),
                        adapter,
                        DemoEntity.class);
        ExportRequest request = new ExportRequest();
        request.setExportMode(ExportRequest.ExportMode.ENTITY);

        ExportLimitExceededException exception =
                assertThrows(
                        ExportLimitExceededException.class,
                        () ->
                                service.export(
                                        "q",
                                        2,
                                        "csv",
                                        request,
                                        page -> new PageImpl<>(List.of("x")),
                                        Function.identity()));

        assertTrue(exception.getMessage().contains("maximum is 1"));
    }

    @Test
    void entityModeRejectsFieldDepthBeforeCreatingIterator() {
        EntityExportAdapter adapter = mock(EntityExportAdapter.class);
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(config(), adapter, DemoEntity.class);
        ExportRequest request = new ExportRequest();
        request.setExportMode(ExportRequest.ExportMode.ENTITY);
        request.setMaxDepth(1);
        request.setIncludeFields(Set.of("owner.department.name"));

        ExportDepthExceededException exception =
                assertThrows(
                        ExportDepthExceededException.class,
                        () ->
                                service.export(
                                        "q",
                                        1,
                                        "json",
                                        request,
                                        page -> new PageImpl<>(List.of("x")),
                                        Function.identity()));

        assertTrue(exception.getMessage().contains("configured maximum"));
        verify(adapter, org.mockito.Mockito.never())
                .createIterator(eq(DemoEntity.class), eq(request), anyInt(), anyInt(), any());
    }

    @Test
    void entityModeSupportsCsvAndXlsxAndPageSizeFloor() throws Exception {
        EntityExportAdapter adapter = mock(EntityExportAdapter.class);
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(
                        new ExportService.ExportConfig(5, 5, 5, 0), adapter, DemoEntity.class);
        ExportRequest request = new ExportRequest();
        request.setExportMode(ExportRequest.ExportMode.ENTITY);

        when(adapter.createIterator(eq(DemoEntity.class), eq(request), eq(1), eq(1), any()))
                .thenReturn(List.<Map<String, Object>>of(Map.of("id", 7)).iterator());

        ResponseEntity<StreamingResponseBody> csvResponse =
                service.export(
                        "q",
                        1,
                        "csv",
                        request,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());
        ResponseEntity<StreamingResponseBody> xlsxResponse =
                service.export(
                        "q",
                        1,
                        "xlsx",
                        request,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());

        ByteArrayOutputStream csvOut = new ByteArrayOutputStream();
        csvResponse.getBody().writeTo(csvOut);
        ByteArrayOutputStream xlsxOut = new ByteArrayOutputStream();
        xlsxResponse.getBody().writeTo(xlsxOut);

        assertTrue(csvOut.toString(StandardCharsets.UTF_8).contains("id"));
        assertTrue(xlsxOut.size() > 0);
    }

    @Test
    void entityModeSupportsNullLimitAndPrivateDefaultExporterBranch() throws Exception {
        EntityExportAdapter adapter = mock(EntityExportAdapter.class);
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(config(), adapter, DemoEntity.class);
        ExportRequest request = new ExportRequest();
        request.setExportMode(ExportRequest.ExportMode.ENTITY);

        when(adapter.createIterator(eq(DemoEntity.class), eq(request), eq(2), eq(2), any()))
                .thenReturn(List.<Map<String, Object>>of(Map.of("id", 2)).iterator());

        ResponseEntity<StreamingResponseBody> response =
                service.export(
                        "q",
                        null,
                        "csv",
                        request,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseEntity<StreamingResponseBody> nullFormatResponse =
                service.export(
                        "q",
                        1,
                        null,
                        request,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());
        assertEquals(HttpStatus.BAD_REQUEST, nullFormatResponse.getStatusCode());

        Method getMapExporter =
                EnhancedExportService.class.getDeclaredMethod(
                        "getMapExporter", String.class, ExportRequest.class);
        getMapExporter.setAccessible(true);

        InvocationTargetException ex =
                assertThrows(
                        InvocationTargetException.class,
                        () -> getMapExporter.invoke(service, "invalid", request));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    @Test
    void entityModeAcceptsFormatWithWhitespaceAndMixedCase() {
        EntityExportAdapter adapter = mock(EntityExportAdapter.class);
        EnhancedExportService<String, DemoEntity, String> service =
                new EnhancedExportService<>(config(), adapter, DemoEntity.class);
        ExportRequest request = new ExportRequest();
        request.setExportMode(ExportRequest.ExportMode.ENTITY);

        when(adapter.createIterator(eq(DemoEntity.class), eq(request), eq(1), eq(1), any()))
                .thenReturn(List.<Map<String, Object>>of(Map.of("id", 9)).iterator());

        ResponseEntity<StreamingResponseBody> response =
                service.export(
                        "q",
                        1,
                        "  CsV  ",
                        request,
                        page -> new PageImpl<>(List.of("x")),
                        Function.identity());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private static ExportService.ExportConfig config() {
        return new ExportService.ExportConfig(2, 2, 2, 2);
    }

    private static final class DemoEntity {}
}
