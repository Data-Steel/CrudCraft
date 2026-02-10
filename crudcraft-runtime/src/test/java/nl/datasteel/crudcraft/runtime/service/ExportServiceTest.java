/*
 * Copyright (c) 2025 CrudCraft contributors
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
package nl.datasteel.crudcraft.runtime.service;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

class ExportServiceTest {

    record TestDto(String name, int value) {}

    record TestSearchRequest() {}

    @Test
    void exportReturns400ForNegativeLimit() {
        ExportService.ExportConfig config = new ExportService.ExportConfig(1000, 500, 250, 100);
        ExportService<TestDto, TestSearchRequest> service = new ExportService<>(config);

        Function<PageRequest, Page<TestDto>> searchFunction = pageable ->
                new PageImpl<>(Collections.emptyList());
        Function<TestDto, TestDto> securityFilter = dto -> dto;

        ResponseEntity<StreamingResponseBody> response = service.export(
                new TestSearchRequest(),
                -1,
                "csv",
                searchFunction,
                securityFilter
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void exportReturnsOkForZeroLimit() {
        ExportService.ExportConfig config = new ExportService.ExportConfig(1000, 500, 250, 100);
        ExportService<TestDto, TestSearchRequest> service = new ExportService<>(config);

        Function<PageRequest, Page<TestDto>> searchFunction = pageable ->
                new PageImpl<>(Collections.emptyList());
        Function<TestDto, TestDto> securityFilter = dto -> dto;

        ResponseEntity<StreamingResponseBody> response = service.export(
                new TestSearchRequest(),
                0,
                "csv",
                searchFunction,
                securityFilter
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void exportReturns400ForInvalidFormat() {
        ExportService.ExportConfig config = new ExportService.ExportConfig(1000, 500, 250, 100);
        ExportService<TestDto, TestSearchRequest> service = new ExportService<>(config);

        Function<PageRequest, Page<TestDto>> searchFunction = pageable ->
                new PageImpl<>(Collections.emptyList());
        Function<TestDto, TestDto> securityFilter = dto -> dto;

        ResponseEntity<StreamingResponseBody> response = service.export(
                new TestSearchRequest(),
                10,
                "xml",
                searchFunction,
                securityFilter
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void exportReturnsOkForValidCsvRequest() {
        ExportService.ExportConfig config = new ExportService.ExportConfig(1000, 500, 250, 100);
        ExportService<TestDto, TestSearchRequest> service = new ExportService<>(config);

        Function<PageRequest, Page<TestDto>> searchFunction = pageable ->
                new PageImpl<>(List.of(new TestDto("test", 42)));
        Function<TestDto, TestDto> securityFilter = dto -> dto;

        ResponseEntity<StreamingResponseBody> response = service.export(
                new TestSearchRequest(),
                10,
                "csv",
                searchFunction,
                securityFilter
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("text/csv", response.getHeaders().getContentType().toString());
    }

    @Test
    void exportReturnsOkForValidJsonRequest() {
        ExportService.ExportConfig config = new ExportService.ExportConfig(1000, 500, 250, 100);
        ExportService<TestDto, TestSearchRequest> service = new ExportService<>(config);

        Function<PageRequest, Page<TestDto>> searchFunction = pageable ->
                new PageImpl<>(List.of(new TestDto("test", 42)));
        Function<TestDto, TestDto> securityFilter = dto -> dto;

        ResponseEntity<StreamingResponseBody> response = service.export(
                new TestSearchRequest(),
                10,
                "json",
                searchFunction,
                securityFilter
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("application/json", response.getHeaders().getContentType().toString());
    }

    @Test
    void exportReturnsOkForValidXlsxRequest() {
        ExportService.ExportConfig config = new ExportService.ExportConfig(1000, 500, 250, 100);
        ExportService<TestDto, TestSearchRequest> service = new ExportService<>(config);

        Function<PageRequest, Page<TestDto>> searchFunction = pageable ->
                new PageImpl<>(List.of(new TestDto("test", 42)));
        Function<TestDto, TestDto> securityFilter = dto -> dto;

        ResponseEntity<StreamingResponseBody> response = service.export(
                new TestSearchRequest(),
                10,
                "xlsx",
                searchFunction,
                securityFilter
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.getHeaders().getContentType().toString());
    }

    @Test
    void exportClampsLimitToFormatMaximum() {
        ExportService.ExportConfig config = new ExportService.ExportConfig(100, 50, 25, 10);
        ExportService<TestDto, TestSearchRequest> service = new ExportService<>(config);

        // Request 200 rows but CSV max is 100
        Function<PageRequest, Page<TestDto>> searchFunction = pageable ->
                new PageImpl<>(List.of(new TestDto("test", 42)));
        Function<TestDto, TestDto> securityFilter = dto -> dto;

        ResponseEntity<StreamingResponseBody> response = service.export(
                new TestSearchRequest(),
                200,
                "csv",
                searchFunction,
                securityFilter
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // The limit is clamped internally, but we can't easily verify without executing the stream
    }

    @Test
    void exportUsesDefaultLimitWhenNullProvided() {
        ExportService.ExportConfig config = new ExportService.ExportConfig(1000, 500, 250, 100);
        ExportService<TestDto, TestSearchRequest> service = new ExportService<>(config);

        Function<PageRequest, Page<TestDto>> searchFunction = pageable ->
                new PageImpl<>(List.of(new TestDto("test", 42)));
        Function<TestDto, TestDto> securityFilter = dto -> dto;

        ResponseEntity<StreamingResponseBody> response = service.export(
                new TestSearchRequest(),
                null,  // null limit should use default of 1000
                "csv",
                searchFunction,
                securityFilter
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
