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

package nl.datasteel.crudcraft.codegen.writer.controller.endpoints;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import java.util.List;
import javax.lang.model.type.TypeMirror;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.TestModelDescriptorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/** Tests for update, patch and delete endpoints. */
class UpdateDeleteEndpointsTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();

    private static String body(EndpointSpec spec, ModelDescriptor model) {
        MethodSpec.Builder mb = MethodSpec.methodBuilder(spec.methodName());
        spec.body().accept(mb, model);
        return mb.build().code().toString();
    }

    @Test
    void updateEndpointProducesProperSpec() {
        UpdateEndpoint provider = new UpdateEndpoint();
        assertEquals(CrudEndpoint.PUT, provider.endpoint());
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.PUT, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.PUT_MAPPING, mapping.type());
        assertEquals("\"/{id}\"", mapping.members().get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, respDto);
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(2, params.size());
        ParameterSpec id = params.get(0);
        assertEquals("id", id.name());
        assertTrue(id.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.PATH_VAR)));
        ParameterSpec req = params.get(1);
        assertEquals("request", req.name());
        assertTrue(
                req.annotations().stream()
                        .anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));

        String body = body(spec, model);
        assertTrue(body.contains("service.update(id, request)"));
        assertTrue(body.contains("ok(updated)"));
    }

    @Test
    void updateEndpointUsesMultipartAndCopiesLobFilesIntoRequest() {
        ModelDescriptor lobModel = lobModel();
        UpdateEndpoint provider = new UpdateEndpoint();
        EndpointSpec spec = provider.create(lobModel);

        AnnotationSpec mapping = spec.mapping().apply(lobModel);
        assertEquals(EndpointSupport.PUT_MAPPING, mapping.type());
        assertTrue(mapping.members().get("consumes").toString().contains("MULTIPART_FORM_DATA"));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(lobModel)).toList();
        assertEquals(3, params.size());
        assertEquals("id", params.get(0).name());
        assertEquals("request", params.get(1).name());
        assertEquals("avatar", params.get(2).name());

        String body = body(spec, lobModel);
        assertTrue(body.contains("request = request.withAvatar(null)"));
        assertTrue(body.contains("avatar.getBytes()"));
        assertTrue(body.contains("service.update(id, request)"));
    }

    @Test
    void bulkUpdateEndpointProducesProperSpec() {
        BulkUpdateEndpoint provider = new BulkUpdateEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.BULK_UPDATE, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.PUT_MAPPING, mapping.type());
        assertEquals("\"/batch\"", mapping.members().get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp =
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(EndpointSupport.BULK_RESULT, respDto));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec reqs = params.get(0);
        assertEquals("requests", reqs.name());
        assertTrue(
                reqs.annotations().stream()
                        .anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));
        assertTrue(reqs.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.VALID)));

        String body = body(spec, model);
        assertTrue(body.contains("service.updateAllResult(requests)"));
        assertTrue(body.contains("HttpStatus.MULTI_STATUS"));
        assertTrue(body.contains("HttpStatus.OK"));
        assertTrue(body.contains("body(result)"));
    }

    @Test
    void patchEndpointProducesProperSpec() {
        PatchEndpoint provider = new PatchEndpoint();
        assertEquals(CrudEndpoint.PATCH, provider.endpoint());
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.PATCH, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.PATCH_MAPPING, mapping.type());
        assertEquals("\"/{id}\"", mapping.members().get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, respDto);
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(2, params.size());
        ParameterSpec id = params.get(0);
        assertEquals("id", id.name());
        assertTrue(id.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.PATH_VAR)));
        ParameterSpec req = params.get(1);
        assertEquals("request", req.name());
        assertTrue(
                req.annotations().stream()
                        .anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));

        String body = body(spec, model);
        assertTrue(body.contains("service.patch(id, request)"));
        assertTrue(body.contains("ok(patched)"));
    }

    @Test
    void patchEndpointUsesMultipartAndCopiesLobFilesIntoRequest() {
        ModelDescriptor lobModel = lobModel();
        PatchEndpoint provider = new PatchEndpoint();
        EndpointSpec spec = provider.create(lobModel);

        AnnotationSpec mapping = spec.mapping().apply(lobModel);
        assertEquals(EndpointSupport.PATCH_MAPPING, mapping.type());
        assertTrue(mapping.members().get("consumes").toString().contains("MULTIPART_FORM_DATA"));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(lobModel)).toList();
        assertEquals(3, params.size());
        assertEquals("id", params.get(0).name());
        assertEquals("request", params.get(1).name());
        assertEquals("avatar", params.get(2).name());

        String body = body(spec, lobModel);
        assertTrue(body.contains("request = request.withAvatar(null)"));
        assertTrue(body.contains("avatar.getBytes()"));
        assertTrue(body.contains("service.patch(id, request)"));
    }

    @Test
    void bulkPatchEndpointProducesProperSpec() {
        BulkPatchEndpoint provider = new BulkPatchEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.BULK_PATCH, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.PATCH_MAPPING, mapping.type());
        assertEquals("\"/batch\"", mapping.members().get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp =
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(EndpointSupport.BULK_RESULT, respDto));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec reqs = params.get(0);
        assertEquals("requests", reqs.name());
        assertTrue(
                reqs.annotations().stream()
                        .anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));
        assertTrue(reqs.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.VALID)));

        String body = body(spec, model);
        assertTrue(body.contains("service.patchAllResult(requests)"));
        assertTrue(body.contains("HttpStatus.MULTI_STATUS"));
        assertTrue(body.contains("HttpStatus.OK"));
        assertTrue(body.contains("body(result)"));
    }

    @Test
    void deleteEndpointProducesProperSpec() {
        DeleteEndpoint provider = new DeleteEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.DELETE, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.DELETE_MAPPING, mapping.type());
        assertEquals("\"/{id}\"", mapping.members().get("value").get(0).toString());

        TypeName resp =
                ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, ClassName.get(Void.class));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec id = params.get(0);
        assertEquals("id", id.name());
        assertTrue(id.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.PATH_VAR)));

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
        assertEquals(EndpointSupport.DELETE_MAPPING, mapping.type());
        assertEquals("\"/batch/delete\"", mapping.members().get("value").get(0).toString());

        TypeName resp =
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(
                                EndpointSupport.BULK_RESULT,
                                EndpointSupport.resolveModelIdType(model)));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec ids = params.get(0);
        assertEquals("ids", ids.name());
        assertTrue(
                ids.annotations().stream()
                        .anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));

        String body = body(spec, model);
        assertTrue(body.contains("service.deleteAllByIdsResult(ids)"));
        assertTrue(body.contains("HttpStatus.MULTI_STATUS"));
        assertTrue(body.contains("HttpStatus.OK"));
        assertTrue(body.contains("body(result)"));
    }

    @Test
    void validateEndpointProducesProperSpec() {
        ValidateEndpoint provider = new ValidateEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.VALIDATE, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.POST_MAPPING, mapping.type());
        assertEquals("\"/validate\"", mapping.members().get("value").get(0).toString());

        TypeName resp =
                ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, ClassName.get(Void.class));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec req = params.get(0);
        assertEquals("request", req.name());
        assertTrue(
                req.annotations().stream()
                        .anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));
        assertTrue(req.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.VALID)));

        String body = body(spec, model);
        assertTrue(body.contains("ok().build()"));
    }

    private static ModelDescriptor lobModel() {
        ModelDescriptor model = mock(ModelDescriptor.class);
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(model.getPackageName()).thenReturn("com.example");
        when(model.getName()).thenReturn("Sample");
        when(model.hasLobFields()).thenReturn(true);
        when(model.getRequestLobFields()).thenReturn(List.of(field));
        when(field.getName()).thenReturn("avatar");
        when(field.getType()).thenReturn(mock(TypeMirror.class));
        when(field.getValidations()).thenReturn(List.of());
        return model;
    }
}
