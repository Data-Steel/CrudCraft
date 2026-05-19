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
import com.palantir.javapoet.TypeName;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class EndpointSupportTest {

    private record TestName(String value) implements Name {
        @Override
        public boolean contentEquals(CharSequence cs) {
            return value.contentEquals(cs);
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(int index) {
            return value.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return value.subSequence(start, end);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Test
    void privateConstructorIsCoveredForUtilityClass() throws Exception {
        Constructor<EndpointSupport> constructor = EndpointSupport.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertTrue(constructor.newInstance() instanceof EndpointSupport);
    }

    @Test
    void processorPrivateConstructorsAreCovered() throws Exception {
        Constructor<FieldProcessor> fieldConstructor = FieldProcessor.class.getDeclaredConstructor();
        fieldConstructor.setAccessible(true);
        Constructor<SecurityFieldFilter> securityConstructor =
                SecurityFieldFilter.class.getDeclaredConstructor();
        securityConstructor.setAccessible(true);

        assertTrue(fieldConstructor.newInstance() instanceof FieldProcessor);
        assertTrue(securityConstructor.newInstance() instanceof SecurityFieldFilter);
    }

    @Test
    void hasSearchFieldsHandlesNullAndNonSearchableFields() {
        ModelDescriptor model = mock(ModelDescriptor.class);
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(model.getFields()).thenReturn(List.of(field));
        when(field.isSearchable()).thenReturn(false);

        assertFalse(EndpointSupport.hasSearchFields(null));
        assertFalse(FieldProcessor.hasSearchFields(null));
        assertFalse(EndpointSupport.hasSearchFields(model));
        assertFalse(FieldProcessor.hasSearchFields(model));
    }

    @Test
    void hasSearchFieldsFindsSearchableField() {
        ModelDescriptor model = mock(ModelDescriptor.class);
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(model.getFields()).thenReturn(List.of(field));
        when(field.isSearchable()).thenReturn(true);

        assertTrue(EndpointSupport.hasSearchFields(model));
        assertTrue(FieldProcessor.hasSearchFields(model));
    }

    @Test
    void hasFieldSecurityHandlesNullPlainAndSecuredFields() {
        ModelDescriptor model = mock(ModelDescriptor.class);
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(model.getFields()).thenReturn(List.of(field));
        when(field.hasFieldSecurity()).thenReturn(false);

        assertFalse(EndpointSupport.hasFieldSecurity(null));
        assertFalse(SecurityFieldFilter.hasFieldSecurity(null));
        assertFalse(EndpointSupport.hasFieldSecurity(model));
        assertFalse(SecurityFieldFilter.hasFieldSecurity(model));

        when(field.hasFieldSecurity()).thenReturn(true);
        assertTrue(EndpointSupport.hasFieldSecurity(model));
        assertTrue(SecurityFieldFilter.hasFieldSecurity(model));
    }

    @Test
    void endpointParameterTemplatesBuildCommonParameters() {
        ClassName requestDto = ClassName.get("example", "WidgetRequestDto");

        ParameterSpec pathId = EndpointParameterTemplates.PATH_ID.create(requestDto).apply(null);
        ParameterSpec request =
                EndpointParameterTemplates.REQUEST_BODY.create(requestDto).apply(null);
        ParameterSpec requestList =
                EndpointParameterTemplates.VALID_REQUEST_LIST.create(requestDto).apply(null);
        ParameterSpec identifiedList =
                EndpointParameterTemplates.VALID_IDENTIFIED_REQUEST_LIST.create(requestDto)
                        .apply(null);
        ParameterSpec pageable = EndpointParameterTemplates.PAGEABLE.create(requestDto).apply(null);

        assertTrue(pathId.toString().contains("PathVariable"));
        assertTrue(pathId.toString().contains("\"id\""));
        assertTrue(request.toString().contains("RequestBody"));
        assertTrue(requestList.toString().contains("Valid"));
        assertTrue(requestList.toString().contains("java.util.List<example.WidgetRequestDto>"));
        assertTrue(identifiedList.toString().contains("Identified<java.util.UUID"));
        assertTrue(pageable.toString().contains("Pageable"));
        assertTrue(pageable.toString().contains("pageable"));
    }

    @Test
    void endpointResponseTemplatesWrapCommonResponses() {
        ClassName responseDto = ClassName.get("example", "WidgetResponseDto");

        TypeName response = EndpointResponseTemplates.RESPONSE_ENTITY.wrap(responseDto);
        TypeName list = EndpointResponseTemplates.LIST_RESPONSE_ENTITY.wrap(responseDto);
        TypeName paginated = EndpointResponseTemplates.PAGINATED_RESPONSE_ENTITY.wrap(responseDto);

        assertTrue(response.toString().contains("ResponseEntity<example.WidgetResponseDto>"));
        assertTrue(list.toString().contains("ResponseEntity<java.util.List"));
        assertTrue(paginated.toString().contains("PaginatedResponse<example.WidgetResponseDto>"));
    }

    @Test
    void lobHelpersHandleScalarListSetAndRequiredValidation() {
        FieldDescriptor scalar = lobField("avatar", mock(TypeMirror.class), true);
        FieldDescriptor map = lobField("metadata", declared("java.util.Map"), false);
        FieldDescriptor list = lobField("photos", declared("java.util.List"), false);
        FieldDescriptor set = lobField("documents", declared("java.util.Set"), false);
        FieldDescriptor collection = lobField("files", declared("java.util.Collection"), false);
        ModelDescriptor model = mock(ModelDescriptor.class);
        when(model.getRequestLobFields()).thenReturn(List.of(scalar, list, set, collection));

        assertFalse(EndpointSupport.isCollectionLobField(scalar));
        assertFalse(EndpointSupport.isCollectionLobField(map));
        assertTrue(EndpointSupport.isCollectionLobField(list));
        assertTrue(EndpointSupport.isCollectionLobField(set));
        assertTrue(EndpointSupport.isCollectionLobField(collection));

        List<java.util.function.Function<ModelDescriptor, com.palantir.javapoet.ParameterSpec>>
                params =
                        EndpointSupport.lobParams(
                                com.palantir.javapoet.ClassName.get("t", "RequestDto"), model);
        String paramsCode =
                params.stream().map(fn -> fn.apply(model).toString()).toList().toString();
        assertTrue(paramsCode.contains("required = true"));
        assertTrue(paramsCode.contains("required = false"));
        assertTrue(
                paramsCode.contains(
                        "java.util.List<org.springframework.web.multipart.MultipartFile> photos"));
        assertTrue(paramsCode.contains("org.springframework.web.multipart.MultipartFile avatar"));

        MethodSpec.Builder builder = MethodSpec.methodBuilder("apply");
        EndpointSupport.addFileToRequestCode(builder, model);
        String code = builder.build().code().toString();
        assertTrue(code.contains("request = request.withAvatar(null)"));
        assertTrue(
                code.contains("java.util.List<byte[]> photosBytes = new java.util.ArrayList<>()"));
        assertTrue(
                code.contains("java.util.Set<byte[]> documentsBytes = new java.util.HashSet<>()"));
    }

    @Test
    void setLobHelperReturnsFalseForScalarType() throws Exception {
        Method method =
                LobProcessor.class.getDeclaredMethod("isSetLobField", FieldDescriptor.class);
        method.setAccessible(true);

        assertFalse(
                (boolean) method.invoke(null, lobField("avatar", mock(TypeMirror.class), false)));
    }

    @Test
    void requiredLobFieldAcceptsJavaxNotNullToo() {
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.getValidations())
                .thenReturn(
                        List.of(
                                AnnotationSpec.builder(
                                                ClassName.get(
                                                        "javax.validation.constraints", "NotNull"))
                                        .build()));

        assertTrue(EndpointSupport.isRequiredLobField(field));
    }

    @Test
    void requiredLobFieldRejectsEmptyAndUnrelatedValidations() {
        FieldDescriptor empty = mock(FieldDescriptor.class);
        when(empty.getValidations()).thenReturn(List.of());
        FieldDescriptor unrelated = mock(FieldDescriptor.class);
        when(unrelated.getValidations())
                .thenReturn(
                        List.of(
                                AnnotationSpec.builder(
                                                ClassName.get(
                                                        "jakarta.validation.constraints", "Size"))
                                        .build()));

        assertFalse(EndpointSupport.isRequiredLobField(empty));
        assertFalse(EndpointSupport.isRequiredLobField(unrelated));
    }

    private static FieldDescriptor lobField(String name, TypeMirror type, boolean required) {
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.getName()).thenReturn(name);
        when(field.getType()).thenReturn(type);
        when(field.getValidations())
                .thenReturn(
                        required
                                ? List.of(
                                        AnnotationSpec.builder(
                                                        com.palantir.javapoet.ClassName.get(
                                                                "jakarta.validation.constraints",
                                                                "NotNull"))
                                                .build())
                                : List.of());
        return field;
    }

    private static DeclaredType declared(String qualifiedName) {
        DeclaredType type = mock(DeclaredType.class);
        TypeElement element = mock(TypeElement.class);
        when(type.asElement()).thenReturn(element);
        when(element.getQualifiedName()).thenReturn(new TestName(qualifiedName));
        return type;
    }
}
