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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.CollectionTypes;


/** Builds multipart LOB parameters and request-body conversion code for generated endpoints. */
final class LobProcessor {

    private static final String JAKARTA_NOT_NULL = "jakarta.validation.constraints.NotNull";
    private static final String JAVAX_NOT_NULL = "javax.validation.constraints.NotNull";

    private LobProcessor() {}

    static boolean isCollectionLobField(FieldDescriptor fd) {
        if (!(fd.getType() instanceof DeclaredType dt)) {
            return false;
        }
        String qualifiedName = ((TypeElement) dt.asElement()).getQualifiedName().toString();
        return CollectionTypes.isQualifiedCollection(qualifiedName);
    }

    static boolean isSetLobField(FieldDescriptor fd) {
        if (!(fd.getType() instanceof DeclaredType dt)) {
            return false;
        }
        return ((TypeElement) dt.asElement())
                .getQualifiedName()
                .toString()
                .equals(CollectionTypes.JAVA_UTIL_SET);
    }

    static boolean isRequiredLobField(FieldDescriptor fd) {
        return fd.getValidations().stream()
                .anyMatch(
                        as ->
                                as.type().toString().equals(JAKARTA_NOT_NULL)
                                        || as.type().toString().equals(JAVAX_NOT_NULL));
    }

    static void addFileToRequestCode(MethodSpec.Builder mb, ModelDescriptor md) {
        for (FieldDescriptor lf : md.getRequestLobFields()) {
            String fieldName = lf.getName();
            String wither =
                    "with" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            if (isCollectionLobField(lf)) {
                addCollectionFileToRequestCode(mb, lf, fieldName, wither);
            } else {
                addScalarFileToRequestCode(mb, fieldName, wither);
            }
        }
    }

    static List<Function<ModelDescriptor, ParameterSpec>> lobParams(
            ClassName requestDtoClass, ModelDescriptor modelDescriptor) {
        return lobParams(requestDtoClass, modelDescriptor, true);
    }

    static List<Function<ModelDescriptor, ParameterSpec>> lobParams(
            ClassName requestDtoClass, ModelDescriptor modelDescriptor, boolean validateRequest) {
        List<Function<ModelDescriptor, ParameterSpec>> params = new ArrayList<>();
        params.add(
                md ->
                        addRequestPartAnnotations(
                                ParameterSpec.builder(requestDtoClass, "request"),
                                validateRequest));
        for (FieldDescriptor lf : modelDescriptor.getRequestLobFields()) {
            String fieldName = lf.getName();
            boolean required = isRequiredLobField(lf);
            if (isCollectionLobField(lf)) {
                params.add(lobCollectionParam(fieldName, required));
            } else {
                params.add(lobScalarParam(fieldName, required));
            }
        }
        return params;
    }

    private static ParameterSpec addRequestPartAnnotations(
            ParameterSpec.Builder builder, boolean validateRequest) {
        if (validateRequest) {
            builder.addAnnotation(EndpointSupport.VALID);
        }
        return builder.addAnnotation(EndpointSupport.NOT_NULL)
                .addAnnotation(
                        AnnotationSpec.builder(EndpointSupport.REQUEST_PART)
                                .addMember("value", "$S", "data")
                                .build())
                .build();
    }

    private static void addCollectionFileToRequestCode(
            MethodSpec.Builder mb, FieldDescriptor lf, String fieldName, String wither) {
        boolean isSet = isSetLobField(lf);
        ClassName iface = isSet ? EndpointSupport.SET : EndpointSupport.LIST;
        ClassName impl = isSet ? EndpointSupport.HASH_SET : EndpointSupport.ARRAY_LIST;
        mb.beginControlFlow("if ($L != null)", fieldName);
        mb.addStatement("$T<byte[]> $LBytes = new $T<>()", iface, fieldName, impl);
        mb.beginControlFlow("for ($T _file : $L)", EndpointSupport.MULTIPART_FILE, fieldName);
        mb.beginControlFlow("if (!_file.isEmpty())");
        mb.beginControlFlow("try");
        mb.addStatement("$LBytes.add(_file.getBytes())", fieldName);
        mb.nextControlFlow("catch ($T e)", EndpointSupport.IO_EXCEPTION);
        addFileReadFailure(mb, fieldName);
        mb.endControlFlow();
        mb.endControlFlow();
        mb.endControlFlow();
        mb.addStatement(
                "request = request.$L($LBytes.isEmpty() ? null : $LBytes)",
                wither,
                fieldName,
                fieldName);
        mb.endControlFlow();
    }

    private static void addScalarFileToRequestCode(
            MethodSpec.Builder mb, String fieldName, String wither) {
        mb.beginControlFlow("if ($L != null)", fieldName);
        mb.beginControlFlow("if ($L.isEmpty())", fieldName);
        mb.addStatement("request = request.$L(null)", wither);
        mb.nextControlFlow("else");
        mb.beginControlFlow("try");
        mb.addStatement("request = request.$L($L.getBytes())", wither, fieldName);
        mb.nextControlFlow("catch ($T e)", EndpointSupport.IO_EXCEPTION);
        addFileReadFailure(mb, fieldName);
        mb.endControlFlow();
        mb.endControlFlow();
        mb.endControlFlow();
    }

    private static void addFileReadFailure(MethodSpec.Builder mb, String fieldName) {
        mb.addStatement(
                "throw new $T($T.BAD_REQUEST, $S + $S, e)",
                ClassName.get("org.springframework.web.server", "ResponseStatusException"),
                ClassName.get("org.springframework.http", "HttpStatus"),
                "Failed to read uploaded file for field: ",
                fieldName);
    }

    private static Function<ModelDescriptor, ParameterSpec> lobCollectionParam(
            String fieldName, boolean required) {
        return md ->
                ParameterSpec.builder(
                                ParameterizedTypeName.get(
                                        EndpointSupport.LIST, EndpointSupport.MULTIPART_FILE),
                                fieldName)
                        .addAnnotation(
                                AnnotationSpec.builder(EndpointSupport.REQUEST_PART)
                                        .addMember("value", "$S", fieldName)
                                        .addMember("required", "$L", required)
                                        .build())
                        .build();
    }

    private static Function<ModelDescriptor, ParameterSpec> lobScalarParam(
            String fieldName, boolean required) {
        return md ->
                ParameterSpec.builder(EndpointSupport.MULTIPART_FILE, fieldName)
                        .addAnnotation(
                                AnnotationSpec.builder(EndpointSupport.REQUEST_PART)
                                        .addMember("value", "$S", fieldName)
                                        .addMember("required", "$L", required)
                                        .build())
                        .build();
    }
}
