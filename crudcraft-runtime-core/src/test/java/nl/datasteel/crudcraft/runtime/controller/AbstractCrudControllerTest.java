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

package nl.datasteel.crudcraft.runtime.controller;

import java.util.List;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.service.BulkResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AbstractCrudControllerTest {

    private AbstractCrudService<TestEntity, TestRequest, TestResponse, TestRef, Long> service;
    private TestController controller;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        service =
                (AbstractCrudService<TestEntity, TestRequest, TestResponse, TestRef, Long>)
                        org.mockito.Mockito.mock(AbstractCrudService.class);
        controller = new TestController(service);
    }

    @Test
    void delegatesCrudEndpointsAndWrapsStatuses() {
        TestRequest request = new TestRequest("value");
        TestResponse response = new TestResponse(1L, "value");
        when(service.findById(1L)).thenReturn(response);
        when(service.create(request)).thenReturn(response);
        when(service.update(1L, request)).thenReturn(response);
        when(service.patch(1L, request)).thenReturn(response);
        when(service.createAllResult(List.of(request)))
                .thenReturn(new BulkResult<>(List.of(response), List.of()));
        when(service.updateAllResult(any()))
                .thenReturn(new BulkResult<>(List.of(response), List.of()));
        when(service.patchAllResult(any()))
                .thenReturn(new BulkResult<>(List.of(response), List.of()));
        when(service.upsertAllResult(List.of(request)))
                .thenReturn(new BulkResult<>(List.of(response), List.of()));
        when(service.deleteAllByIdsResult(List.of(1L, 2L)))
                .thenReturn(new BulkResult<>(List.of(1L, 2L), List.of()));
        when(service.findByIds(List.of(1L, 2L))).thenReturn(List.of(response));
        when(service.count()).thenReturn(42L);
        when(service.existsById(1L)).thenReturn(true);
        when(service.existsById(2L)).thenReturn(false);

        assertEquals(response, controller.getById(1L).getBody());
        assertEquals(HttpStatus.CREATED, controller.create(request).getStatusCode());
        assertEquals(response, controller.update(1L, request).getBody());
        assertEquals(response, controller.patch(1L, request).getBody());
        assertEquals(HttpStatus.NO_CONTENT, controller.delete(1L).getStatusCode());
        assertEquals(HttpStatus.CREATED, controller.createAll(List.of(request)).getStatusCode());
        assertEquals(
                1,
                controller.updateAll(List.of(new Identified<>(1L, request)))
                        .getBody()
                        .succeeded()
                        .size());
        assertEquals(
                1,
                controller.patchAll(List.of(new Identified<>(1L, request)))
                        .getBody()
                        .succeeded()
                        .size());
        assertEquals(1, controller.upsertAll(List.of(request)).getBody().succeeded().size());
        assertEquals(HttpStatus.OK, controller.deleteAllByIds(List.of(1L, 2L)).getStatusCode());
        assertEquals(1, controller.findByIds(List.of(1L, 2L)).getBody().size());
        assertEquals(42L, controller.count().getBody().get("count"));
        assertEquals(HttpStatus.OK, controller.exists(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.exists(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.validate(request).getStatusCode());

        verify(service).delete(1L);
        verify(service).deleteAllByIdsResult(List.of(1L, 2L));
    }

    @Test
    void bulkEndpointsReturnMultiStatusWhenAnyItemFails() {
        TestRequest request = new TestRequest("value");
        BulkResult.Failure failure = new BulkResult.Failure(0, "invalid");
        when(service.createAllResult(List.of(request)))
                .thenReturn(new BulkResult<>(List.of(), List.of(failure)));
        when(service.updateAllResult(any()))
                .thenReturn(new BulkResult<>(List.of(), List.of(failure)));
        when(service.patchAllResult(any()))
                .thenReturn(new BulkResult<>(List.of(), List.of(failure)));
        when(service.upsertAllResult(List.of(request)))
                .thenReturn(new BulkResult<>(List.of(), List.of(failure)));
        when(service.deleteAllByIdsResult(List.of(1L)))
                .thenReturn(new BulkResult<>(List.of(), List.of(failure)));

        ResponseEntity<BulkResult<TestResponse>> createResponse = controller.createAll(List.of(request));

        assertEquals(HttpStatus.MULTI_STATUS, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertEquals(List.of(failure), createResponse.getBody().failed());
        assertEquals(
                HttpStatus.MULTI_STATUS,
                controller.updateAll(List.of(new Identified<>(1L, request))).getStatusCode());
        assertEquals(
                HttpStatus.MULTI_STATUS,
                controller.patchAll(List.of(new Identified<>(1L, request))).getStatusCode());
        assertEquals(HttpStatus.MULTI_STATUS, controller.upsertAll(List.of(request)).getStatusCode());
        assertEquals(HttpStatus.MULTI_STATUS, controller.deleteAllByIds(List.of(1L)).getStatusCode());
    }

    @Test
    void mapsPagesAndClampsRequestedPageSize() {
        PageRequest requested = PageRequest.of(2, 500);
        when(service.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new TestResponse(1L, "a")), requested, 11));
        when(service.findAllRef(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new TestRef(1L, "r")), requested, 11));

        ResponseEntity<PaginatedResponse<TestResponse>> full = controller.getAll(requested);
        ResponseEntity<PaginatedResponse<TestRef>> ref = controller.getAllRef(requested);

        ArgumentCaptor<Pageable> fullCaptor = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<Pageable> refCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(fullCaptor.capture());
        verify(service).findAllRef(refCaptor.capture());
        assertEquals(2, fullCaptor.getValue().getPageNumber());
        assertEquals(50, fullCaptor.getValue().getPageSize());
        assertEquals(50, refCaptor.getValue().getPageSize());

        assertNotNull(full.getBody());
        assertEquals(1, full.getBody().content().size());
        assertEquals(2, full.getBody().page());
        assertEquals(500, full.getBody().size());
        assertEquals(3, full.getBody().totalPages());
        assertEquals(1001, full.getBody().totalElements());
        assertEquals(false, full.getBody().first());
        assertEquals(true, full.getBody().last());

        assertNotNull(ref.getBody());
        assertEquals(1, ref.getBody().content().size());
    }

    @Test
    void defaultsPageableWhenInputIsNull() {
        when(service.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new TestResponse(1L, "a"))));

        controller.getAll(null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(50, captor.getValue().getPageSize());
    }

    @Test
    void rejectsNonPositiveConfiguredMaximumPageSize() {
        controller.maxPageSize = 0;

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, controller::validateConfiguration);
        assertEquals("crudcraft.api.max-page-size must be positive; got 0", exception.getMessage());
    }

    @Test
    void acceptsPositiveConfiguredMaximumPageSize() {
        controller.maxPageSize = 25;

        controller.validateConfiguration();
    }

    private static final class TestController
            extends AbstractCrudController<TestEntity, TestRequest, TestResponse, TestRef, Long> {

        private TestController(
                AbstractCrudService<TestEntity, TestRequest, TestResponse, TestRef, Long> service) {
            this(service, 50);
        }

        private TestController(
                AbstractCrudService<TestEntity, TestRequest, TestResponse, TestRef, Long> service,
                int maxPageSize) {
            super(service, maxPageSize);
        }
    }

    private record TestEntity(Long id, String value) {}

    private record TestRequest(String value) {}

    private record TestResponse(Long id, String value) {}

    private record TestRef(Long id, String value) {}
}
