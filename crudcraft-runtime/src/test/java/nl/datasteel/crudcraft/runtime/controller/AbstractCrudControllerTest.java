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

package nl.datasteel.crudcraft.runtime.controller;

import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.search.SearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AbstractCrudControllerTest {

    static class Entity {}
    record Req(String name){}
    record Resp(Integer id,String name){}
    record Ref(Integer id){}
    static class SR implements SearchRequest<Entity>{
        @Override public org.springframework.data.jpa.domain.Specification<Entity> toSpecification(){return null;}
    }
    static class Ctrl extends AbstractCrudController<Entity,Req,Resp,Ref,SR,Integer>{
        Ctrl(AbstractCrudService<Entity,Req,Resp,Ref,Integer> s){super(s);}
    }

    AbstractCrudService<Entity,Req,Resp,Ref,Integer> service;
    Ctrl controller;
    SR searchRequest;

    @BeforeEach
    void setup(){
        service = mock(AbstractCrudService.class);
        controller = new Ctrl(service);
        controller.maxPageSize = 50;
        controller.maxCsvRows = 1000;
        controller.maxJsonRows = 500;
        controller.maxXlsxRows = 250;
        searchRequest = new SR();
    }

    @Test
    void clampPageableHandlesNullAndClampsSize(){
        Pageable clamped = controller.clampPageable(null);
        assertEquals(0, clamped.getPageNumber());
        assertEquals(50, clamped.getPageSize());

        Pageable over = PageRequest.of(1,100);
        Pageable result = controller.clampPageable(over);
        assertEquals(1, result.getPageNumber());
        assertEquals(50, result.getPageSize());
    }

    @Test
    void getAllDelegatesToServiceAndWrapsPage(){
        Page<Resp> page = new PageImpl<>(List.of(new Resp(1,"a")), PageRequest.of(0,10), 1);
        when(service.search(any(), any())).thenReturn(page);

        ResponseEntity<PaginatedResponse<Resp>> entity = controller.getAll(null, searchRequest);
        assertEquals(200, entity.getStatusCode().value());
        verify(service).search(eq(searchRequest), any());
        assertNotNull(entity.getBody());
        assertEquals(1, entity.getBody().content().size());
    }

    @Test
    void getAllRefDelegatesToService(){
        Page<Ref> page = new PageImpl<>(List.of(new Ref(1)), PageRequest.of(0,10),1);
        when(service.searchRef(any(), any())).thenReturn(page);
        ResponseEntity<PaginatedResponse<Ref>> entity = controller.getAllRef(null, searchRequest);
        assertEquals(200, entity.getStatusCode().value());
        verify(service).searchRef(eq(searchRequest), any());
        assertEquals(1, entity.getBody().content().size());
    }

    @Test
    void getByIdReturnsDto(){
        Resp resp = new Resp(1,"a");
        when(service.findById(1)).thenReturn(resp);
        ResponseEntity<Resp> entity = controller.getById(1);
        assertEquals(resp, entity.getBody());
    }

    @Test
    void createReturnsCreatedStatus(){
        Resp resp = new Resp(1,"a");
        when(service.create(any())).thenReturn(resp);
        ResponseEntity<Resp> entity = controller.create(new Req("a"));
        assertEquals(201, entity.getStatusCode().value());
        assertEquals(resp, entity.getBody());
    }

    @Test
    void updateReturnsOk(){
        Resp resp = new Resp(1,"b");
        when(service.update(eq(1), any())).thenReturn(resp);
        ResponseEntity<Resp> entity = controller.update(1, new Req("b"));
        assertEquals(200, entity.getStatusCode().value());
        assertEquals(resp, entity.getBody());
    }

    @Test
    void patchReturnsOk(){
        Resp resp = new Resp(1,"c");
        when(service.patch(eq(1), any())).thenReturn(resp);
        ResponseEntity<Resp> entity = controller.patch(1, new Req("c"));
        assertEquals(200, entity.getStatusCode().value());
        assertEquals(resp, entity.getBody());
    }

    @Test
    void deleteReturnsNoContent(){
        ResponseEntity<Void> entity = controller.delete(1);
        verify(service).delete(1);
        assertEquals(204, entity.getStatusCode().value());
    }

    @Test
    void createAllReturnsCreated(){
        List<Req> reqs = List.of(new Req("a"));
        when(service.createAll(any())).thenReturn(List.of(new Resp(1,"a")));
        ResponseEntity<List<Resp>> entity = controller.createAll(reqs);
        assertEquals(201, entity.getStatusCode().value());
        assertEquals(1, entity.getBody().size());
    }

    @Test
    void updateAllReturnsOk(){
        List<Identified<Integer,Req>> reqs = List.of(new Identified<>(1,new Req("a")));
        when(service.updateAll(any())).thenReturn(List.of(new Resp(1,"a")));
        ResponseEntity<List<Resp>> entity = controller.updateAll(reqs);
        assertEquals(200, entity.getStatusCode().value());
    }

    @Test
    void patchAllReturnsOk(){
        List<Identified<Integer,Req>> reqs = List.of(new Identified<>(1,new Req("a")));
        when(service.patchAll(any())).thenReturn(List.of(new Resp(1,"a")));
        ResponseEntity<List<Resp>> entity = controller.patchAll(reqs);
        assertEquals(200, entity.getStatusCode().value());
    }

    @Test
    void upsertAllReturnsOk(){
        List<Req> reqs = List.of(new Req("a"));
        when(service.upsertAll(any())).thenReturn(List.of(new Resp(1,"a")));
        ResponseEntity<List<Resp>> entity = controller.upsertAll(reqs);
        assertEquals(200, entity.getStatusCode().value());
    }

    @Test
    void deleteAllByIdsReturnsNoContent(){
        ResponseEntity<Void> entity = controller.deleteAllByIds(List.of(1,2));
        verify(service).deleteAllByIds(List.of(1,2));
        assertEquals(204, entity.getStatusCode().value());
    }

    @Test
    void existsReturnsOkOrNotFound(){
        when(service.existsById(1)).thenReturn(true);
        when(service.existsById(2)).thenReturn(false);
        assertEquals(200, controller.exists(1).getStatusCode().value());
        assertEquals(404, controller.exists(2).getStatusCode().value());
    }

    @Test
    void countReturnsTotal(){
        when(service.count()).thenReturn(5L);
        ResponseEntity<Map<String,Long>> entity = controller.count();
        assertEquals(5L, entity.getBody().get("count"));
    }

    @Test
    void findByIdsReturnsResults(){
        when(service.findByIds(any())).thenReturn(List.of(new Resp(1,"a")));
        ResponseEntity<List<Resp>> entity = controller.findByIds(List.of(1));
        assertEquals(1, entity.getBody().size());
    }

    @Test
    void searchValidatesLimit(){
        assertEquals(400, controller.search(searchRequest, null).getStatusCode().value());
        assertEquals(400, controller.search(searchRequest, 0).getStatusCode().value());
    }

    @Test
    void searchClampsLimitAndResults(){
        controller.maxPageSize = 50;
        List<Resp> list = new ArrayList<>();
        for(int i=0;i<60;i++) list.add(new Resp(i,"n"+i));
        Page<Resp> page = new PageImpl<>(list, PageRequest.of(0,60),60);
        when(service.search(any(), any())).thenReturn(page);
        ResponseEntity<PaginatedResponse<Resp>> entity = controller.search(searchRequest, 60);
        assertEquals(50, entity.getBody().content().size());
        verify(service).search(eq(searchRequest), argThat(p -> p.getPageSize()==50));
    }

    @Test
    void validateReturnsOk(){
        assertEquals(200, controller.validate(new Req("a")).getStatusCode().value());
    }

    @Test
    void exportInvalidFormatReturnsBadRequest(){
        assertEquals(400, controller.export(searchRequest, 10, "xml").getStatusCode().value());
    }

    @Test
    void exportCsvStreamsData() throws Exception {
        when(service.search(any(), any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0,10),0));
        ResponseEntity<StreamingResponseBody> resp = controller.export(searchRequest, 5, "csv");
        assertEquals("text/csv", resp.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        StreamingResponseBody body = resp.getBody();
        assertNotNull(body);
        body.writeTo(new ByteArrayOutputStream());
    }

    @Test
    void exportJsonAndXlsxHaveCorrectHeaders() {
        when(service.search(any(), any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0,10),0));
        ResponseEntity<StreamingResponseBody> json = controller.export(searchRequest, 5, "json");
        assertEquals("application/json", json.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        ResponseEntity<StreamingResponseBody> xlsx = controller.export(searchRequest, 5, "xlsx");
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                xlsx.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void exportClampsLimitToFormatMax() throws Exception {
        controller.maxCsvRows = 80;
        controller.maxPageSize = 100;
        when(service.search(any(), any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0,10),0));

        ResponseEntity<StreamingResponseBody> resp = controller.export(searchRequest, 1000, "csv");
        StreamingResponseBody body = resp.getBody();
        assertNotNull(body);
        body.writeTo(new ByteArrayOutputStream());

        verify(service).search(eq(searchRequest), argThat(p -> p.getPageSize() == 80));
    }

    @Test
    void exportDefaultsLimitWhenNull() throws Exception {
        controller.maxCsvRows = 20;
        controller.maxPageSize = 100;
        when(service.search(any(), any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0,10),0));

        ResponseEntity<StreamingResponseBody> resp = controller.export(searchRequest, null, "csv");
        StreamingResponseBody body = resp.getBody();
        assertNotNull(body);
        body.writeTo(new ByteArrayOutputStream());

        verify(service).search(eq(searchRequest), argThat(p -> p.getPageSize() == 20));
    }

    @Test
    void exportNegativeLimitThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.export(searchRequest, -1, "csv"));
    }
}
