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

package nl.datasteel.crudcraft.codegen.writer.controller;

import com.palantir.javapoet.MethodSpec;
import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ControllerEndpointsTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();

    @Test
    void defaultsReturnAllEndpoints() {
        Map<CrudEndpoint, EndpointSpec> map = ControllerEndpoints.defaults(model);
        EnumSet<CrudEndpoint> expected = EnumSet.allOf(CrudEndpoint.class);
        expected.remove(CrudEndpoint.SEARCH);
        assertEquals(expected, map.keySet());
    }

    @Test
    void defaultsThrowsOnNullDescriptor() {
        assertThrows(NullPointerException.class, () -> ControllerEndpoints.defaults(null));
    }

    @Test
    void privateConstructorIsCovered() throws Exception {
        Constructor<ControllerEndpoints> constructor =
                ControllerEndpoints.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        constructor.newInstance();
    }

    @Test
    void specFunctionsProduceNonNull() {
        Map<CrudEndpoint, EndpointSpec> map = ControllerEndpoints.defaults(model);
        map.values()
                .forEach(
                        spec -> {
                            assertNotNull(spec.mapping().apply(model));
                            assertNotNull(spec.returnType().apply(model));
                            spec.params().forEach(p -> assertNotNull(p.apply(model)));
                            assertDoesNotThrow(
                                    () ->
                                            spec.body()
                                                    .accept(
                                                            MethodSpec.methodBuilder(
                                                                    spec.methodName()),
                                                            model));
                        });
    }

    @Test
    void defaultsExcludeSearchWhenNoSearchableFieldsExist() {
        ModelDescriptor noSearchModel = mock(ModelDescriptor.class);
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.isSearchable()).thenReturn(false);
        when(noSearchModel.getFields()).thenReturn(List.of(field));
        when(noSearchModel.getPackageName()).thenReturn("com.example");
        when(noSearchModel.getName()).thenReturn("Sample");

        Map<CrudEndpoint, EndpointSpec> map = ControllerEndpoints.defaults(noSearchModel);
        assertFalse(map.containsKey(CrudEndpoint.SEARCH));
    }

    @Test
    void defaultsIncludeSearchWhenSearchableFieldsExist() {
        ModelDescriptor searchableModel = mock(ModelDescriptor.class);
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.isSearchable()).thenReturn(true);
        when(searchableModel.getFields()).thenReturn(List.of(field));
        when(searchableModel.getPackageName()).thenReturn("com.example");
        when(searchableModel.getName()).thenReturn("Sample");

        Map<CrudEndpoint, EndpointSpec> map = ControllerEndpoints.defaults(searchableModel);

        assertTrue(map.containsKey(CrudEndpoint.SEARCH));
    }

    @Test
    void fieldSecurityEndpointsEmitReadAndWriteFiltersOnlyWhenNeeded() {
        ModelDescriptor securedModel = securedSearchableModel();
        Map<CrudEndpoint, EndpointSpec> map = ControllerEndpoints.defaults(securedModel);

        for (CrudEndpoint endpoint :
                List.of(
                        CrudEndpoint.POST,
                        CrudEndpoint.PUT,
                        CrudEndpoint.PATCH,
                        CrudEndpoint.BULK_CREATE,
                        CrudEndpoint.BULK_UPDATE,
                        CrudEndpoint.BULK_PATCH,
                        CrudEndpoint.BULK_UPSERT,
                        CrudEndpoint.VALIDATE)) {
            assertTrue(body(map.get(endpoint), securedModel).contains("filterWrite"), endpoint.name());
        }

        for (CrudEndpoint endpoint :
                List.of(
                        CrudEndpoint.GET_ONE,
                        CrudEndpoint.GET_ALL,
                        CrudEndpoint.GET_ALL_REF,
                        CrudEndpoint.FIND_BY_IDS,
                        CrudEndpoint.SEARCH,
                        CrudEndpoint.EXPORT)) {
            assertTrue(body(map.get(endpoint), securedModel).contains("filterRead"), endpoint.name());
        }

        ModelDescriptor plainModel = TestModelDescriptorFactory.create();
        Map<CrudEndpoint, EndpointSpec> plain = ControllerEndpoints.defaults(plainModel);
        assertFalse(body(plain.get(CrudEndpoint.POST), plainModel).contains("FieldSecurityUtil"));
        assertFalse(body(plain.get(CrudEndpoint.GET_ONE), plainModel).contains("FieldSecurityUtil"));
    }

    private static String body(EndpointSpec spec, ModelDescriptor modelDescriptor) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(spec.methodName());
        spec.body().accept(builder, modelDescriptor);
        return builder.build().code().toString();
    }

    private static ModelDescriptor securedSearchableModel() {
        ModelDescriptor model = mock(ModelDescriptor.class);
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.isSearchable()).thenReturn(true);
        when(field.hasFieldSecurity()).thenReturn(true);
        when(model.getFields()).thenReturn(List.of(field));
        when(model.getPackageName()).thenReturn("com.example");
        when(model.getName()).thenReturn("Sample");
        return model;
    }
}
