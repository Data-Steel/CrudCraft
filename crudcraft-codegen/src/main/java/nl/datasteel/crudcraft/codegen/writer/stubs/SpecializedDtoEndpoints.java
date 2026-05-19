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

package nl.datasteel.crudcraft.codegen.writer.stubs;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.ModelIdTypeResolver;
import nl.datasteel.crudcraft.codegen.util.StringCase;


/** Builds optional projection-specific controller endpoints for generated stubs. */
final class SpecializedDtoEndpoints {

    private SpecializedDtoEndpoints() {}

    static List<MethodSpec> generate(
            ModelDescriptor modelDescriptor, CrudSecurityPolicy securityPolicy) {
        Set<String> specializedDtoNames =
                modelDescriptor.getFields().stream()
                        .flatMap(fd -> Arrays.stream(fd.getResponseDtos()))
                        .collect(java.util.stream.Collectors.toSet());

        List<MethodSpec> methods = new ArrayList<>();
        String pkg = modelDescriptor.getPackageName();
        String name = modelDescriptor.getName();
        ClassName responseEntity =
                JavaPoetUtils.getClassName("org.springframework.http", "ResponseEntity");
        ClassName paginatedResponse =
                JavaPoetUtils.getClassName(
                        "nl.datasteel.crudcraft.runtime.controller.response", "PaginatedResponse");
        ClassName getMapping =
                JavaPoetUtils.getClassName("org.springframework.web.bind.annotation", "GetMapping");
        ClassName operation =
                JavaPoetUtils.getClassName("io.swagger.v3.oas.annotations", "Operation");
        ClassName apiResponses =
                JavaPoetUtils.getClassName(
                        "io.swagger.v3.oas.annotations.responses", "ApiResponses");
        ClassName apiResponse =
                JavaPoetUtils.getClassName(
                        "io.swagger.v3.oas.annotations.responses", "ApiResponse");
        ClassName pageable =
                JavaPoetUtils.getClassName("org.springframework.data.domain", "Pageable");
        ClassName page = JavaPoetUtils.getClassName("org.springframework.data.domain", "Page");
        ClassName pathVariable =
                JavaPoetUtils.getClassName(
                        "org.springframework.web.bind.annotation", "PathVariable");
        TypeName idType = ModelIdTypeResolver.resolveModelIdType(modelDescriptor).box();
        ClassName preAuthorize =
                JavaPoetUtils.getClassName(
                        "org.springframework.security.access.prepost", "PreAuthorize");

        for (String dtoName : specializedDtoNames) {
            String className = name + StringCase.PASCAL.apply(dtoName) + "ResponseDto";
            ClassName specializedDto =
                    JavaPoetUtils.getClassName(pkg + ".dto.response", className);
            String pathSegment = StringCase.CAMEL.apply(dtoName).toLowerCase();
            String getAllOperation =
                    CrudEndpoint.GET_ALL.name()
                            + "_"
                            + StringCase.CAMEL.apply(dtoName).toUpperCase(Locale.ROOT);
            String getOneOperation =
                    CrudEndpoint.GET_ONE.name()
                            + "_"
                            + StringCase.CAMEL.apply(dtoName).toUpperCase(Locale.ROOT);

            MethodSpec.Builder getAllBuilder =
                    MethodSpec.methodBuilder("getAll" + StringCase.PASCAL.apply(dtoName))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(
                                    ParameterizedTypeName.get(
                                            responseEntity,
                                            ParameterizedTypeName.get(
                                                    paginatedResponse, specializedDto)))
                            .addAnnotation(
                                    AnnotationSpec.builder(getMapping)
                                            .addMember("value", "$S", "/" + pathSegment)
                                            .build())
                            .addAnnotation(
                                    AnnotationSpec.builder(operation)
                                            .addMember(
                                                    "summary",
                                                    "$S",
                                                    "Get all "
                                                            + modelDescriptor.getName()
                                                            + " entities as "
                                                            + dtoName
                                                            + " projection")
                                            .addMember(
                                                    "description",
                                                    "$S",
                                                    "Retrieves all "
                                                            + modelDescriptor.getName()
                                                            + " entities with support for"
                                                            + " pagination,"
                                                            + " projected to "
                                                            + dtoName
                                                            + " DTO.")
                                            .build())
                            .addAnnotation(
                                    AnnotationSpec.builder(apiResponses)
                                            .addMember(
                                                    "value",
                                                    "@$T(responseCode = $S, description = $S)",
                                                    apiResponse,
                                                    "200",
                                                    "Paginated list of "
                                                            + modelDescriptor.getName()
                                                            + " entities")
                                            .build())
                            .addParameter(ParameterSpec.builder(pageable, "pageable").build())
                            .addStatement("long _crudcraftStarted = $T.nanoTime()", System.class)
                            .addStatement("String _crudcraftOutcome = $S", "success")
                            .beginControlFlow("try")
                            .addCode("$T clamped = clampPageable(pageable);\n", pageable)
                            .addCode(
                                    "$T<$T> page = service.findAll(clamped, $T.class);\n",
                                    page,
                                    specializedDto,
                                    specializedDto)
                            .addCode(
                                    "$T<$T> response = new $T<>(\n"
                                            + "    page.getContent(),\n"
                                            + "    page.getNumber(),\n"
                                            + "    page.getSize(),\n"
                                            + "    page.getTotalPages(),\n"
                                            + "    page.getTotalElements(),\n"
                                            + "    page.isFirst(),\n"
                                            + "    page.isLast()\n"
                                            + ");\n",
                                    paginatedResponse,
                                    specializedDto,
                                    paginatedResponse)
                            .addCode("return $T.ok(response);\n", responseEntity)
                            .nextControlFlow("catch ($T ex)", RuntimeException.class)
                            .addStatement("_crudcraftOutcome = $S", "error")
                            .addStatement("throw ex")
                            .nextControlFlow("finally")
                            .addStatement(
                                    "recordOperation($S, _crudcraftOutcome, _crudcraftStarted)",
                                    getAllOperation)
                            .endControlFlow();
            addSecurityAnnotation(
                    getAllBuilder, preAuthorize, securityPolicy, CrudEndpoint.GET_ALL);
            MethodSpec getAllMethod = getAllBuilder.build();
            methods.add(getAllMethod);

            MethodSpec.Builder getOneBuilder =
                    MethodSpec.methodBuilder("get" + StringCase.PASCAL.apply(dtoName) + "ById")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ParameterizedTypeName.get(responseEntity, specializedDto))
                            .addAnnotation(
                                    AnnotationSpec.builder(getMapping)
                                            .addMember("value", "$S", "/" + pathSegment + "/{id}")
                                            .build())
                            .addAnnotation(
                                    AnnotationSpec.builder(operation)
                                            .addMember(
                                                    "summary",
                                                    "$S",
                                                    "Get a single "
                                                            + modelDescriptor.getName()
                                                            + " by ID as "
                                                            + dtoName
                                                            + " projection")
                                            .addMember(
                                                    "description",
                                                    "$S",
                                                    "Retrieves a single "
                                                            + modelDescriptor.getName()
                                                            + " entity by its unique identifier,"
                                                            + " projected to "
                                                            + dtoName
                                                            + " DTO.")
                                            .build())
                            .addAnnotation(
                                    AnnotationSpec.builder(apiResponses)
                                            .addMember(
                                                    "value",
                                                    "{@$T(responseCode = $S,"
                                                            + " description = $S),"
                                                            + " @$T(responseCode = $S,"
                                                            + " description = $S)}",
                                                    apiResponse,
                                                    "200",
                                                    modelDescriptor.getName()
                                                            + " retrieved successfully",
                                                    apiResponse,
                                                    "404",
                                                    modelDescriptor.getName()
                                                            + " with the specified ID"
                                                            + " was not found")
                                            .build())
                            .addParameter(
                                    ParameterSpec.builder(idType, "id")
                                            .addAnnotation(pathVariable)
                                            .build())
                            .addStatement("long _crudcraftStarted = $T.nanoTime()", System.class)
                            .addStatement("String _crudcraftOutcome = $S", "success")
                            .beginControlFlow("try")
                            .addCode(
                                    "$T dto = service.findById(id, $T.class);\n",
                                    specializedDto,
                                    specializedDto)
                            .addCode("return $T.ok(dto);\n", responseEntity)
                            .nextControlFlow("catch ($T ex)", RuntimeException.class)
                            .addStatement("_crudcraftOutcome = $S", "error")
                            .addStatement("throw ex")
                            .nextControlFlow("finally")
                            .addStatement(
                                    "recordOperation($S, _crudcraftOutcome, _crudcraftStarted)",
                                    getOneOperation)
                            .endControlFlow();
            addSecurityAnnotation(
                    getOneBuilder, preAuthorize, securityPolicy, CrudEndpoint.GET_ONE);
            MethodSpec getOneMethod = getOneBuilder.build();
            methods.add(getOneMethod);
        }

        return methods;
    }

    private static void addSecurityAnnotation(
            MethodSpec.Builder methodBuilder,
            ClassName preAuthorize,
            CrudSecurityPolicy securityPolicy,
            CrudEndpoint endpoint) {
        if (securityPolicy == null) {
            return;
        }
        String expression = securityPolicy.getSecurityExpression(endpoint);
        if (expression == null) {
            throw new IllegalStateException(
                    "Security expression for " + endpoint + " must not be null");
        }
        methodBuilder.addAnnotation(
                AnnotationSpec.builder(preAuthorize).addMember("value", "$S", expression).build());
    }
}
