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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/** Detailed tests for endpoints that create resources. */
class CreateEndpointsTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();

    private static String body(EndpointSpec spec, ModelDescriptor model) {
        MethodSpec.Builder mb = MethodSpec.methodBuilder(spec.methodName());
        spec.body().accept(mb, model);
        return mb.build().code().toString();
    }

    @Test
    void createEndpointProducesProperSpec() {
        CreateEndpoint provider = new CreateEndpoint();
        assertEquals(CrudEndpoint.POST, provider.endpoint());
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.POST, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.POST_MAPPING, mapping.type());
        assertFalse(mapping.members().containsKey("value"));

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName resp =
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY,
                        ClassName.get(pkg + ".dto.response", name + "ResponseDto"));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec p = params.get(0);
        assertEquals("request", p.name());
        assertEquals(ClassName.get(pkg + ".dto.request", name + "RequestDto"), p.type());
        assertTrue(
                p.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));

        String body = body(spec, model);
        assertTrue(body.contains("service.create(request)"));
        assertTrue(body.contains("status(201)"));
        assertTrue(body.contains("body(created)"));
    }

    @Test
    void createEndpointUsesMultipartAndCopiesLobFilesIntoRequest() {
        ModelDescriptor lobModel = lobModel();
        CreateEndpoint provider = new CreateEndpoint();
        EndpointSpec spec = provider.create(lobModel);

        AnnotationSpec mapping = spec.mapping().apply(lobModel);
        assertEquals(EndpointSupport.POST_MAPPING, mapping.type());
        assertTrue(mapping.members().get("consumes").toString().contains("MULTIPART_FORM_DATA"));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(lobModel)).toList();
        assertEquals(2, params.size());
        assertEquals("request", params.get(0).name());
        assertEquals("avatar", params.get(1).name());
        assertTrue(
                params.get(1).annotations().stream()
                        .anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_PART)));

        String body = body(spec, lobModel);
        assertTrue(body.contains("request = request.withAvatar(null)"));
        assertTrue(body.contains("avatar.getBytes()"));
        assertTrue(body.contains("service.create(request)"));
    }

    @Test
    void bulkCreateEndpointProducesProperSpec() {
        BulkCreateEndpoint provider = new BulkCreateEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.BULK_CREATE, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.POST_MAPPING, mapping.type());
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
        ParameterSpec p = params.get(0);
        assertEquals("requests", p.name());
        TypeName reqDto = ClassName.get(pkg + ".dto.request", name + "RequestDto");
        assertEquals(ParameterizedTypeName.get(EndpointSupport.LIST, reqDto), p.type());
        assertTrue(
                p.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));
        assertTrue(p.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.VALID)));

        String body = body(spec, model);
        assertTrue(body.contains("service.createAllResult(requests)"));
        assertTrue(body.contains("HttpStatus.MULTI_STATUS"));
        assertTrue(body.contains("HttpStatus.CREATED"));
        assertTrue(body.contains("body(result)"));
    }

    @Test
    void bulkUpsertEndpointProducesProperSpec() {
        BulkUpsertEndpoint provider = new BulkUpsertEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.BULK_UPSERT, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.POST_MAPPING, mapping.type());
        assertEquals("\"/batch/upsert\"", mapping.members().get("value").get(0).toString());

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
        ParameterSpec p = params.get(0);
        assertEquals("requests", p.name());
        TypeName reqDto = ClassName.get(pkg + ".dto.request", name + "RequestDto");
        assertEquals(ParameterizedTypeName.get(EndpointSupport.COLLECTION, reqDto), p.type());
        assertTrue(
                p.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));

        String body = body(spec, model);
        assertTrue(body.contains("service.upsertAllResult(requests)"));
        assertTrue(body.contains("HttpStatus.MULTI_STATUS"));
        assertTrue(body.contains("HttpStatus.CREATED"));
        assertTrue(body.contains("body(result)"));
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
