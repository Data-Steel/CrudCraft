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

package nl.datasteel.crudcraft.codegen.writer.controller.endpoints;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.List;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;

public class ExportEndpoint implements EndpointSpecProvider {

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.EXPORT;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        String dtoRespPkg = modelDescriptor.getPackageName() + ".dto.response";
        String dtoFull = modelDescriptor.getName() + "ResponseDto";
        String searchReq = modelDescriptor.getName() + "SearchRequest";
        return new EndpointSpec(
                CrudEndpoint.EXPORT,
                "export",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                        .addMember("value", "$S", "/export").build(),
                md -> ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, EndpointSupport.STREAMING_BODY),
                List.of(
                        md -> ParameterSpec.builder(
                                        ClassName.get(modelDescriptor.getPackageName() + ".search", searchReq),
                                        "searchRequest")
                                .addAnnotation(EndpointSupport.MODEL_ATTR)
                                .build(),
                        md -> ParameterSpec.builder(ClassName.get(Integer.class), "limit")
                                .addAnnotation(AnnotationSpec.builder(EndpointSupport.REQUEST_PARAM)
                                        .addMember("value", "$S", "limit")
                                        .addMember("required", "$L", false)
                                        .build())
                                .build(),
                        md -> ParameterSpec.builder(ClassName.get(String.class), "format")
                                .addAnnotation(AnnotationSpec.builder(EndpointSupport.REQUEST_PARAM)
                                        .addMember("value", "$S", "format")
                                        .addMember("required", "$L", true)
                                        .build())
                                .build()),
                (mb, md) -> mb.addCode(
                        "final int effectiveLimit = limit != null ? limit : 1000;\n" +
                                "String lower = format == null ? \"\" : format.toLowerCase();\n" +
                                "int max;\n" +
                                "String contentType;\n" +
                                "String extension;\n" +
                                "$T<$T<$T>, $T> exporter;\n" +
                                "switch (lower) {\n" +
                                "    case \"csv\" -> {\n" +
                                "        max = maxCsvRows;\n" +
                                "        contentType = \"text/csv\";\n" +
                                "        extension = \"csv\";\n" +
                                "        exporter = $T::streamCsv;\n" +
                                "    }\n" +
                                "    case \"json\" -> {\n" +
                                "        max = maxJsonRows;\n" +
                                "        contentType = \"application/json\";\n" +
                                "        extension = \"json\";\n" +
                                "        exporter = $T::streamJson;\n" +
                                "    }\n" +
                                "    case \"xlsx\" -> {\n" +
                                "        max = maxXlsxRows;\n" +
                                "        contentType = \"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\";\n" +
                                "        extension = \"xlsx\";\n" +
                                "        exporter = $T::streamXlsx;\n" +
                                "    }\n" +
                                "    default -> {\n" +
                                "        return $T.badRequest().build();\n" +
                                "    }\n" +
                                "}\n" +
                                "int clamped = Math.min(effectiveLimit, max);\n" +
                                "int pageSize = Math.min(maxPageSize, clamped);\n" +
                                "$T body = out -> {\n" +
                                "    $T iterator = new $T<>() {\n" +
                                "        int page = 0;\n" +
                                "        int index = 0;\n" +
                                "        $T current = $T.emptyList();\n" +
                                "        int fetched = 0;\n" +
                                "        private void fetch() {\n" +
                                "            if (fetched >= clamped) {\n" +
                                "                current = $T.emptyList();\n" +
                                "                return;\n" +
                                "            }\n" +
                                "            $T p = service.search(searchRequest, $T.of(page++, pageSize));\n" +
                                "            $T dtos = p.getContent().stream()\n" +
                                "                    .map($T::filterRead)\n" +
                                "                    .toList();\n" +
                                "            if (dtos.isEmpty()) {\n" +
                                "                current = $T.emptyList();\n" +
                                "                fetched = clamped;\n" +
                                "                return;\n" +
                                "            }\n" +
                                "            if (fetched + dtos.size() > clamped) {\n" +
                                "                dtos = dtos.subList(0, clamped - fetched);\n" +
                                "                fetched = clamped;\n" +
                                "            } else {\n" +
                                "                fetched += dtos.size();\n" +
                                "            }\n" +
                                "            current = dtos;\n" +
                                "            index = 0;\n" +
                                "        }\n" +
                                "        @Override\n" +
                                "        public boolean hasNext() {\n" +
                                "            if (index >= current.size()) {\n" +
                                "                fetch();\n" +
                                "            }\n" +
                                "            return index < current.size();\n" +
                                "        }\n" +
                                "        @Override\n" +
                                "        public $T next() {\n" +
                                "            if (!hasNext()) {\n" +
                                "                throw new $T();\n" +
                                "            }\n" +
                                "            return current.get(index++);\n" +
                                "        }\n" +
                                "    };\n" +
                                "    exporter.accept(iterator, out);\n" +
                                "};\n" +
                                "String filename = \"export-\" + System.currentTimeMillis() + \".\" + extension;\n" +
                                "return $T.ok()\n" +
                                "        .header($T.CONTENT_TYPE, contentType)\n" +
                                "        .header($T.CONTENT_DISPOSITION, \"attachment; filename=\" + filename)\n" +
                                "        .body(body);\n",
                        EndpointSupport.BI_CONSUMER, EndpointSupport.ITERATOR, ClassName.get(dtoRespPkg, dtoFull), EndpointSupport.OUTPUT_STREAM,
                        EndpointSupport.EXPORT_UTIL, EndpointSupport.EXPORT_UTIL, EndpointSupport.EXPORT_UTIL, EndpointSupport.RESP_ENTITY,
                        EndpointSupport.STREAMING_BODY, EndpointSupport.ITERATOR, EndpointSupport.ITERATOR,
                        ParameterizedTypeName.get(EndpointSupport.LIST, ClassName.get(dtoRespPkg, dtoFull)), EndpointSupport.COLLECTIONS,
                        EndpointSupport.COLLECTIONS, ParameterizedTypeName.get(EndpointSupport.PAGE, ClassName.get(dtoRespPkg, dtoFull)), EndpointSupport.PAGE_REQUEST,
                        ParameterizedTypeName.get(EndpointSupport.LIST, ClassName.get(dtoRespPkg, dtoFull)), EndpointSupport.FIELD_SECURITY_UTIL, EndpointSupport.COLLECTIONS,
                        ClassName.get(dtoRespPkg, dtoFull), EndpointSupport.NO_SUCH_ELEMENT,
                        EndpointSupport.RESP_ENTITY, EndpointSupport.HTTP_HEADERS, EndpointSupport.HTTP_HEADERS)
        );
    }
}