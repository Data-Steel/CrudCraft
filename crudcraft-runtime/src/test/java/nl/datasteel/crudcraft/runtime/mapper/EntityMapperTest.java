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

package nl.datasteel.crudcraft.runtime.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityMapperTest {

    static class Entity { Integer id; String name; }
    static class Request { Integer id; String name; Request(Integer id,String name){this.id=id;this.name=name;} }
    record Response(Integer id,String name){}
    record Ref(Integer id){}

    static class SimpleMapper implements EntityMapper<Entity, Request, Response, Ref, Integer> {
        public Entity fromRequest(Request request) {
            Entity e = new Entity();
            e.id = request.id;
            e.name = request.name;
            return e;
        }
        public Entity update(Entity entity, Request request) {
            entity.id = request.id;
            entity.name = request.name;
            return entity;
        }
        public Entity patch(Entity entity, Request request) {
            if(request.id!=null) entity.id = request.id;
            if(request.name!=null) entity.name = request.name;
            return entity;
        }
        public Response toResponse(Entity entity) { return new Response(entity.id, entity.name); }
        public Ref toRef(Entity entity) { return new Ref(entity.id); }
        public Integer getIdFromRequest(Request request) { return request.id; }
    }

    EntityMapper<Entity,Request,Response,Ref,Integer> mapper = new SimpleMapper();

    @Test
    void fromRequestCopiesFields() {
        Entity e = mapper.fromRequest(new Request(1,"a"));
        assertEquals(1, e.id);
        assertEquals("a", e.name);
    }

    @Test
    void updateOverwritesFields() {
        Entity e = new Entity(); e.id=1; e.name="a";
        mapper.update(e, new Request(2,"b"));
        assertEquals(2, e.id);
        assertEquals("b", e.name);
    }

    @Test
    void patchOnlyUpdatesNonNull() {
        Entity e = new Entity(); e.id=1; e.name="a";
        mapper.patch(e, new Request(null,"c"));
        assertEquals(1, e.id);
        assertEquals("c", e.name);
    }

    @Test
    void toResponseMapsEntity() {
        Entity e = new Entity(); e.id=1; e.name="a";
        Response r = mapper.toResponse(e);
        assertEquals(1, r.id());
        assertEquals("a", r.name());
    }

    @Test
    void toRefMapsId() {
        Entity e = new Entity(); e.id=1;
        Ref ref = mapper.toRef(e);
        assertEquals(1, ref.id());
    }

    @Test
    void getIdFromRequestReturnsId() {
        assertEquals(5, mapper.getIdFromRequest(new Request(5, "x")));
    }
}
