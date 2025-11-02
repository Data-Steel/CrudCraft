/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen.writer.controller.endpoints;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.List;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.TestModelDescriptorFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for update, patch and delete endpoints.
 */
class UpdateDeleteEndpointsTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();

    private static String body(EndpointSpec spec, ModelDescriptor model) {
        MethodSpec.Builder mb = MethodSpec.methodBuilder(spec.methodName());
        spec.body().accept(mb, model);
        return mb.build().code.toString();
    }

    @Test
    void updateEndpointProducesProperSpec() {
        UpdateEndpoint provider = new UpdateEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.PUT, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.PUT_MAPPING, mapping.type);
        assertEquals("\"/{id}\"", mapping.members.get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, respDto);
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(2, params.size());
        ParameterSpec id = params.get(0);
        assertEquals("id", id.name);
        assertTrue(id.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.PATH_VAR)));
        ParameterSpec req = params.get(1);
        assertEquals("request", req.name);
        assertTrue(req.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.REQUEST_BODY)));

        String body = body(spec, model);
        assertTrue(body.contains("filterWrite(request)"));
        assertTrue(body.contains("service.update(id, request)"));
        assertTrue(body.contains("ok(FieldSecurityUtil.filterRead(updated))"));
    }

    @Test
    void bulkUpdateEndpointProducesProperSpec() {
        BulkUpdateEndpoint provider = new BulkUpdateEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.BULK_UPDATE, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.PUT_MAPPING, mapping.type);
        assertEquals("\"/batch\"", mapping.members.get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY,
                ParameterizedTypeName.get(EndpointSupport.LIST, respDto));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec reqs = params.get(0);
        assertEquals("requests", reqs.name);
        assertTrue(reqs.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.REQUEST_BODY)));
        assertTrue(reqs.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.VALID)));

        String body = body(spec, model);
        assertTrue(body.contains("requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()))"));
        assertTrue(body.contains("service.updateAll(requests)"));
        assertTrue(body.contains("ok(dtos)"));
    }

    @Test
    void patchEndpointProducesProperSpec() {
        PatchEndpoint provider = new PatchEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.PATCH, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.PATCH_MAPPING, mapping.type);
        assertEquals("\"/{id}\"", mapping.members.get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, respDto);
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(2, params.size());
        ParameterSpec id = params.get(0);
        assertEquals("id", id.name);
        assertTrue(id.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.PATH_VAR)));
        ParameterSpec req = params.get(1);
        assertEquals("request", req.name);
        assertTrue(req.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.REQUEST_BODY)));

        String body = body(spec, model);
        assertTrue(body.contains("filterWrite(request)"));
        assertTrue(body.contains("service.patch(id, request)"));
        assertTrue(body.contains("ok(FieldSecurityUtil.filterRead(patched))"));
    }

    @Test
    void bulkPatchEndpointProducesProperSpec() {
        BulkPatchEndpoint provider = new BulkPatchEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.BULK_PATCH, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.PATCH_MAPPING, mapping.type);
        assertEquals("\"/batch\"", mapping.members.get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY,
                ParameterizedTypeName.get(EndpointSupport.LIST, respDto));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec reqs = params.get(0);
        assertEquals("requests", reqs.name);
        assertTrue(reqs.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.REQUEST_BODY)));
        assertTrue(reqs.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.VALID)));

        String body = body(spec, model);
        assertTrue(body.contains("requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()))"));
        assertTrue(body.contains("service.patchAll(requests)"));
        assertTrue(body.contains("ok(dtos)"));
    }

    @Test
    void deleteEndpointProducesProperSpec() {
        DeleteEndpoint provider = new DeleteEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.DELETE, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.DELETE_MAPPING, mapping.type);
        assertEquals("\"/{id}\"", mapping.members.get("value").get(0).toString());

        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, ClassName.get(Void.class));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec id = params.get(0);
        assertEquals("id", id.name);
        assertTrue(id.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.PATH_VAR)));

        String body = body(spec, model);
        assertTrue(body.contains("service.delete(id)"));
        assertTrue(body.contains("noContent().build()"));
    }

    @Test
    void bulkDeleteEndpointProducesProperSpec() {
        BulkDeleteEndpoint provider = new BulkDeleteEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.BULK_DELETE, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.DELETE_MAPPING, mapping.type);
        assertEquals("\"/batch/delete\"", mapping.members.get("value").get(0).toString());

        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, ClassName.get(Void.class));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec ids = params.get(0);
        assertEquals("ids", ids.name);
        assertTrue(ids.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.REQUEST_BODY)));

        String body = body(spec, model);
        assertTrue(body.contains("service.deleteAllByIds(ids)"));
        assertTrue(body.contains("noContent().build()"));
    }

    @Test
    void validateEndpointProducesProperSpec() {
        ValidateEndpoint provider = new ValidateEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.VALIDATE, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.POST_MAPPING, mapping.type);
        assertEquals("\"/validate\"", mapping.members.get("value").get(0).toString());

        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, ClassName.get(Void.class));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec req = params.get(0);
        assertEquals("request", req.name);
        assertTrue(req.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.REQUEST_BODY)));
        assertTrue(req.annotations.stream().anyMatch(a -> a.type.equals(EndpointSupport.VALID)));

        String body = body(spec, model);
        assertTrue(body.contains("ok().build()"));
    }
}

