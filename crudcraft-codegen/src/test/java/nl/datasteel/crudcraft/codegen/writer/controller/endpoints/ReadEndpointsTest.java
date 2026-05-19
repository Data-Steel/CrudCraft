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


/** Tests for read-oriented controller endpoints. */
class ReadEndpointsTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();

    private static String body(EndpointSpec spec, ModelDescriptor model) {
        MethodSpec.Builder mb = MethodSpec.methodBuilder(spec.methodName());
        spec.body().accept(mb, model);
        return mb.build().code().toString();
    }

    @Test
    void getOneEndpointProducesProperSpec() {
        GetOneEndpoint provider = new GetOneEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.GET_ONE, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.GET_MAPPING, mapping.type());
        assertEquals("\"/{id}\"", mapping.members().get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, respDto);
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec p = params.get(0);
        assertEquals("id", p.name());
        assertEquals(EndpointSupport.UUID_CLASS, p.type());
        assertTrue(p.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.PATH_VAR)));

        String body = body(spec, model);
        assertTrue(body.contains("service.findById(id)"));
        assertTrue(body.contains("ok(dto)"));
    }

    @Test
    void getAllEndpointProducesProperSpec() {
        GetAllEndpoint provider = new GetAllEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.GET_ALL, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.GET_MAPPING, mapping.type());
        assertFalse(mapping.members().containsKey("value"));

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp =
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, respDto));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        assertEquals("pageable", params.get(0).name());
        assertEquals(EndpointSupport.PAGEABLE, params.get(0).type());

        String body = body(spec, model);
        assertTrue(body.contains("service.findAll(clampPageable(pageable))"));
        assertTrue(body.contains("new PaginatedResponse"));
    }

    @Test
    void bulkFindByIdsEndpointProducesProperSpec() {
        FindByIdsEndpoint provider = new FindByIdsEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.FIND_BY_IDS, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.POST_MAPPING, mapping.type());
        assertEquals("\"/batch/ids\"", mapping.members().get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp =
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, respDto));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec ids = params.get(0);
        assertEquals("ids", ids.name());
        assertTrue(
                ids.annotations().stream()
                        .anyMatch(a -> a.type().equals(EndpointSupport.REQUEST_BODY)));

        String body = body(spec, model);
        assertTrue(body.contains("service.findByIds(ids)"));
        assertTrue(body.contains("var dtos = service.findByIds(ids)"));
    }

    @Test
    void getAllRefEndpointProducesProperSpec() {
        GetAllRefEndpoint provider = new GetAllRefEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.GET_ALL_REF, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.GET_MAPPING, mapping.type());
        assertEquals("\"/ref\"", mapping.members().get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.ref", name + "Ref");
        TypeName resp =
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, respDto));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        assertEquals("pageable", params.get(0).name());

        String body = body(spec, model);
        assertTrue(body.contains("service.findAllRef(clampPageable(pageable))"));
        assertTrue(body.contains("new PaginatedResponse"));
    }

    @Test
    void searchEndpointProducesProperSpec() {
        SearchEndpoint provider = new SearchEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.SEARCH, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.GET_MAPPING, mapping.type());
        assertEquals("\"/search\"", mapping.members().get("value").get(0).toString());

        String pkg = model.getPackageName();
        String name = model.getName();
        TypeName respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        TypeName resp =
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, respDto));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(2, params.size());
        ParameterSpec search = params.get(0);
        assertEquals("searchRequest", search.name());
        assertTrue(
                search.annotations().stream()
                        .anyMatch(a -> a.type().equals(EndpointSupport.MODEL_ATTR)));
        ParameterSpec pageable = params.get(1);
        assertEquals("pageable", pageable.name());
        assertEquals(EndpointSupport.PAGEABLE, pageable.type());

        String body = body(spec, model);
        assertTrue(body.contains("Pageable clamped = clampPageable(pageable)"));
        assertTrue(body.contains("SearchOperations.search"));
        assertTrue(body.contains("new PaginatedResponse"));
    }

    @Test
    void getAllEndpointFallsBackToPlainListWhenSearchIsDisabled() {
        ModelDescriptor noSearchModel = mock(ModelDescriptor.class);
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.isSearchable()).thenReturn(false);
        when(noSearchModel.getPackageName()).thenReturn("com.example");
        when(noSearchModel.getName()).thenReturn("Sample");
        when(noSearchModel.getFields()).thenReturn(List.of(field));

        GetAllEndpoint provider = new GetAllEndpoint();
        EndpointSpec spec = provider.create(noSearchModel);

        List<ParameterSpec> params =
                spec.params().stream().map(p -> p.apply(noSearchModel)).toList();
        assertEquals(1, params.size());
        assertEquals("pageable", params.get(0).name());
        String body = body(spec, noSearchModel);
        assertTrue(body.contains("service.findAll(clampPageable(pageable))"));
    }

    @Test
    void countEndpointProducesProperSpec() {
        CountEndpoint provider = new CountEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.COUNT, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.GET_MAPPING, mapping.type());
        assertEquals("\"/count\"", mapping.members().get("value").get(0).toString());

        TypeName resp =
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(
                                EndpointSupport.MAP,
                                ClassName.get(String.class),
                                ClassName.get(Long.class)));
        assertEquals(resp, spec.returnType().apply(model));

        assertTrue(spec.params().isEmpty());

        String body = body(spec, model);
        assertTrue(body.contains("service.count()"));
        assertTrue(body.contains("Map.of(\"count\", total)"));
    }

    @Test
    void existsEndpointProducesProperSpec() {
        ExistsEndpoint provider = new ExistsEndpoint();
        EndpointSpec spec = provider.create(model);
        assertEquals(CrudEndpoint.EXISTS, spec.endpoint());

        AnnotationSpec mapping = spec.mapping().apply(model);
        assertEquals(EndpointSupport.REQUEST_MAPPING, mapping.type());
        assertEquals("\"/exists/{id}\"", mapping.members().get("value").get(0).toString());

        String methodMembers = mapping.members().get("method").get(0).toString();
        assertTrue(methodMembers.contains("RequestMethod.HEAD"));
        assertTrue(methodMembers.contains("RequestMethod.GET"));

        TypeName resp =
                ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, ClassName.get(Void.class));
        assertEquals(resp, spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(1, params.size());
        ParameterSpec id = params.get(0);
        assertEquals("id", id.name());
        assertTrue(id.annotations().stream().anyMatch(a -> a.type().equals(EndpointSupport.PATH_VAR)));

        String body = body(spec, model);
        assertTrue(body.contains("service.existsById(id)"));
        assertTrue(body.contains("ok().build()"));
        assertTrue(body.contains("notFound().build()"));
    }
}
