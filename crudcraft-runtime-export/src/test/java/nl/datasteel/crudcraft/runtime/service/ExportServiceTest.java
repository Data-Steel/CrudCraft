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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.exception.ExportLimitExceededException;
import nl.datasteel.crudcraft.runtime.export.ExportDepthExceededException;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ExportServiceTest {

    @Test
    void returnsBadRequestForNegativeLimitAndInvalidFormat() {
        ExportService<Map<String, Object>, String> service = serviceWith(10, 10, 10, 5);
        Function<PageRequest, Page<Map<String, Object>>> search =
                page -> new PageImpl<>(List.of(Map.of("name", "a")));

        ResponseEntity<StreamingResponseBody> negative =
                service.export("q", -1, "csv", new ExportRequest(), search, Function.identity());
        ResponseEntity<StreamingResponseBody> invalidFormat =
                service.export("q", 1, "xml", new ExportRequest(), search, Function.identity());

        assertEquals(HttpStatus.BAD_REQUEST, negative.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, invalidFormat.getStatusCode());
    }

    @Test
    void supportsZeroLimitAsEmptyExport() throws Exception {
        ExportService<Map<String, Object>, String> service = serviceWith(10, 10, 10, 5);

        ResponseEntity<StreamingResponseBody> response =
                service.export(
                        "q",
                        0,
                        "json",
                        new ExportRequest(),
                        page -> new PageImpl<>(List.of(Map.of("name", "x"))),
                        Function.identity());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getBody().writeTo(out);
        assertEquals("[]", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void rejectsExportLimitAboveConfiguredMaximum() {
        ExportService<Map<String, Object>, String> service = serviceWith(3, 3, 3, 0);
        Function<PageRequest, Page<Map<String, Object>>> search =
                page -> new PageImpl<>(List.of(Map.of("name", "a")));

        ExportLimitExceededException exception =
                assertThrows(
                        ExportLimitExceededException.class,
                        () ->
                                service.export(
                                        "q",
                                        99,
                                        "csv",
                                        new ExportRequest(),
                                        search,
                                        Function.identity()));

        assertTrue(exception.getMessage().contains("maximum is 3"));
    }

    @Test
    void appliesGlobalRowCapBeforePerFormatStreaming() {
        ExportService<Map<String, Object>, String> service =
                new ExportService<>(new ExportService.ExportConfig(2, 10, 10, 10, 5));
        Function<PageRequest, Page<Map<String, Object>>> search =
                page -> new PageImpl<>(List.of(Map.of("name", "a")));

        ExportLimitExceededException exception =
                assertThrows(
                        ExportLimitExceededException.class,
                        () ->
                                service.export(
                                        "q",
                                        3,
                                        "json",
                                        new ExportRequest(),
                                        search,
                                        Function.identity()));

        assertTrue(exception.getMessage().contains("maximum is 2"));
    }

    @Test
    void exportsWithPaginationAndSecurityFiltering() throws Exception {
        ExportService<Map<String, Object>, String> service = serviceWith(3, 3, 3, 0);
        List<PageRequest> calls = new ArrayList<>();
        Function<PageRequest, Page<Map<String, Object>>> search =
                page -> {
                    calls.add(page);
                    if (page.getPageNumber() == 0) {
                        return new PageImpl<>(List.of(Map.of("name", "a"), Map.of("name", "b")));
                    }
                    if (page.getPageNumber() == 1) {
                        return new PageImpl<>(List.of(Map.of("name", "c")));
                    }
                    return Page.empty();
                };

        ResponseEntity<StreamingResponseBody> response =
                service.export(
                        "q",
                        3,
                        "csv",
                        new ExportRequest(),
                        search,
                        value -> Map.of("name", String.valueOf(value.get("name")).toUpperCase()));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains(".csv"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getBody().writeTo(out);
        String csv = out.toString(StandardCharsets.UTF_8);

        assertTrue(csv.contains("A"));
        assertTrue(csv.contains("B"));
        assertTrue(csv.contains("C"));
        assertFalse(csv.contains("D"));
        assertEquals(2, calls.size());
        assertEquals(1, calls.get(0).getPageSize());
    }

    @Test
    void overloadWithoutExportRequestDelegatesSuccessfully() {
        ExportService<Map<String, Object>, String> service = serviceWith(10, 10, 10, 10);

        ResponseEntity<StreamingResponseBody> response =
                service.export(
                        "q",
                        1,
                        "csv",
                        page -> new PageImpl<>(List.of(Map.of("name", "x"))),
                        Function.identity());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void supportsJsonAndXlsxFormatsAndEmptyPageResults() throws Exception {
        ExportService<Map<String, Object>, String> service = serviceWith(10, 10, 10, 2);

        ResponseEntity<StreamingResponseBody> jsonResponse =
                service.export(
                        "q",
                        2,
                        "json",
                        new ExportRequest(),
                        page -> new PageImpl<>(List.of(Map.of("name", "x"))),
                        Function.identity());
        ResponseEntity<StreamingResponseBody> xlsxResponse =
                service.export(
                        "q",
                        2,
                        "xlsx",
                        new ExportRequest(),
                        page -> new PageImpl<>(List.of(Map.of("name", "x"))),
                        Function.identity());
        ResponseEntity<StreamingResponseBody> emptyCsvResponse =
                service.export(
                        "q",
                        2,
                        "csv",
                        new ExportRequest(),
                        page -> Page.empty(),
                        Function.identity());

        ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
        jsonResponse.getBody().writeTo(jsonOut);
        ByteArrayOutputStream xlsxOut = new ByteArrayOutputStream();
        xlsxResponse.getBody().writeTo(xlsxOut);
        ByteArrayOutputStream emptyCsvOut = new ByteArrayOutputStream();
        emptyCsvResponse.getBody().writeTo(emptyCsvOut);

        assertTrue(jsonOut.toString(StandardCharsets.UTF_8).contains("\"name\":\"x\""));
        assertTrue(xlsxOut.size() > 0);
        assertEquals(0, emptyCsvOut.size());
    }

    @Test
    void handlesNullLimitAndNullFormat() {
        ExportService<Map<String, Object>, String> service = serviceWith(10, 10, 10, 10);

        ResponseEntity<StreamingResponseBody> nullLimit =
                service.export(
                        "q",
                        null,
                        "csv",
                        new ExportRequest(),
                        page -> new PageImpl<>(List.of(Map.of("name", "x"))),
                        Function.identity());
        ResponseEntity<StreamingResponseBody> nullFormat =
                service.export(
                        "q",
                        1,
                        null,
                        new ExportRequest(),
                        page -> new PageImpl<>(List.of(Map.of("name", "x"))),
                        Function.identity());

        assertEquals(HttpStatus.OK, nullLimit.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, nullFormat.getStatusCode());
    }

    @Test
    void acceptsFormatWithWhitespaceAndMixedCase() {
        ExportService<Map<String, Object>, String> service = serviceWith(10, 10, 10, 10);

        ResponseEntity<StreamingResponseBody> response =
                service.export(
                        "q",
                        1,
                        "  JsOn  ",
                        new ExportRequest(),
                        page -> new PageImpl<>(List.of(Map.of("name", "x"))),
                        Function.identity());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void validatesDtoDepthBeforeCreatingStreamingResponse() {
        ExportService<Map<String, Object>, String> service = serviceWith(10, 10, 10, 10);
        ExportRequest request = new ExportRequest();
        request.setMaxDepth(0);
        request.setIncludeFields(Set.of("author.name"));

        ExportDepthExceededException exception =
                assertThrows(
                        ExportDepthExceededException.class,
                        () ->
                                service.export(
                                        "q",
                                        1,
                                        "json",
                                        request,
                                        page -> {
                                            throw new AssertionError("search must not run");
                                        },
                                        Function.identity()));

        assertEquals("author.name", exception.getContext().get("requested_field"));
    }

    @Test
    void acceptsDtoDepthAtConfiguredBoundaryForIncludeAndExcludeFields() {
        ExportService<Map<String, Object>, String> service = serviceWith(10, 10, 10, 10);
        ExportRequest request = new ExportRequest();
        request.setMaxDepth(1);
        request.setIncludeFields(Set.of("author.name"));
        request.setExcludeFields(Set.of("author.email"));

        ResponseEntity<StreamingResponseBody> response =
                service.export(
                        "q",
                        1,
                        "json",
                        request,
                        page -> new PageImpl<>(List.of(Map.of("author", Map.of("name", "Ada")))),
                        Function.identity());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void privateExporterAndIteratorFailureBranchesAreCovered() throws Exception {
        ExportService<Map<String, Object>, String> service = serviceWith(10, 10, 10, 10);
        Method getExporter =
                ExportService.class.getDeclaredMethod(
                        "getExporter", String.class, ExportRequest.class);
        getExporter.setAccessible(true);

        InvocationTargetException ex =
                assertThrows(
                        InvocationTargetException.class,
                        () -> getExporter.invoke(service, "invalid", new ExportRequest()));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());

        Class<?> iteratorClass =
                Class.forName(
                        "nl.datasteel.crudcraft.runtime.export.service.ExportService$PaginatedIterator");
        Constructor<?> constructor =
                iteratorClass.getDeclaredConstructor(
                        Function.class, Function.class, int.class, int.class);
        constructor.setAccessible(true);
        @SuppressWarnings("unchecked")
        Iterator<Map<String, Object>> iterator =
                (Iterator<Map<String, Object>>)
                        constructor.newInstance(
                                (Function<PageRequest, Page<Map<String, Object>>>)
                                        page -> Page.empty(),
                                (Function<Map<String, Object>, Map<String, Object>>) value -> value,
                                0,
                                1);

        assertThrows(java.util.NoSuchElementException.class, iterator::next);
    }

    @Test
    void paginatedIteratorKeepsFullPageWhenExactlyAtLimit() throws Exception {
        Class<?> iteratorClass =
                Class.forName(
                        "nl.datasteel.crudcraft.runtime.export.service.ExportService$PaginatedIterator");
        Constructor<?> constructor =
                iteratorClass.getDeclaredConstructor(
                        Function.class, Function.class, int.class, int.class);
        constructor.setAccessible(true);

        @SuppressWarnings("unchecked")
        Iterator<Map<String, Object>> iterator =
                (Iterator<Map<String, Object>>)
                        constructor.newInstance(
                                (Function<PageRequest, Page<Map<String, Object>>>)
                                        page ->
                                                new PageImpl<>(
                                                        List.of(Map.of("id", 1), Map.of("id", 2))),
                                (Function<Map<String, Object>, Map<String, Object>>) value -> value,
                                2,
                                10);

        assertTrue(iterator.hasNext());
        Field currentField = iteratorClass.getDeclaredField("current");
        currentField.setAccessible(true);
        Object current = currentField.get(iterator);
        assertFalse(current.getClass().getName().contains("SubList"));
    }

    private static ExportService<Map<String, Object>, String> serviceWith(
            int csvRows, int jsonRows, int xlsxRows, int pageSize) {
        return new ExportService<>(
                new ExportService.ExportConfig(csvRows, jsonRows, xlsxRows, pageSize));
    }
}
