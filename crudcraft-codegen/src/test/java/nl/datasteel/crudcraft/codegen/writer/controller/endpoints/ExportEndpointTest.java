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

import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import java.util.List;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ExportEndpointTest {

    @Test
    void plainExportUsesFindAllAndNoSearchRequestParameter() {
        ModelDescriptor model = model(false);
        ExportEndpoint provider = new ExportEndpoint();
        assertEquals(CrudEndpoint.EXPORT, provider.endpoint());
        EndpointSpec spec = provider.create(model);

        assertEquals(EndpointSupport.GET_MAPPING, spec.mapping().apply(model).type());
        assertEquals(
                ParameterizedTypeName.get(
                        EndpointSupport.RESP_ENTITY, EndpointSupport.STREAMING_BODY),
                spec.returnType().apply(model));

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(3, params.size());
        assertEquals("limit", params.get(0).name());
        assertEquals("format", params.get(1).name());
        assertEquals("exportRequest", params.get(2).name());

        String body = body(spec, model);
        assertTrue(body.contains("null,"));
        assertTrue(body.contains("pageable -> service.findAll(pageable)"));
    }

    @Test
    void searchAwareExportAddsSearchRequestAndUsesSearchOperations() {
        ModelDescriptor model = model(true);
        EndpointSpec spec = new ExportEndpoint().create(model);

        List<ParameterSpec> params = spec.params().stream().map(p -> p.apply(model)).toList();
        assertEquals(4, params.size());
        assertEquals("searchRequest", params.get(0).name());
        assertEquals("limit", params.get(1).name());

        String body = body(spec, model);
        assertTrue(body.contains("searchRequest,"));
        assertTrue(body.contains("SearchOperations.search(service, searchRequest, pageable"));
        assertTrue(body.contains("SampleResponseDto.class"));
    }

    private static String body(EndpointSpec spec, ModelDescriptor model) {
        MethodSpec.Builder mb = MethodSpec.methodBuilder(spec.methodName());
        spec.body().accept(mb, model);
        return mb.build().code().toString();
    }

    private static ModelDescriptor model(boolean searchable) {
        ModelDescriptor model = mock(ModelDescriptor.class);
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(model.getPackageName()).thenReturn("com.example");
        when(model.getName()).thenReturn("Sample");
        when(model.getFields()).thenReturn(List.of(field));
        when(field.isSearchable()).thenReturn(searchable);
        return model;
    }
}
