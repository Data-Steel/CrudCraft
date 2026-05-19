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

package nl.datasteel.crudcraft.codegen.writer;

import java.util.List;
import java.util.StringJoiner;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.CollectionTypes;
import nl.datasteel.crudcraft.codegen.util.StringCase;


/** Shared payload and formatting helpers for Insomnia collection generation. */
final class InsomniaPayloadHelper {

    private InsomniaPayloadHelper() {}

    static String generateSampleRequestBody(ModelDescriptor model) {
        StringBuilder body = new StringBuilder("{");
        List<FieldDescriptor> requestFields =
                model.getFields().stream()
                        .filter(
                                fd ->
                                        fd.inRequest()
                                                || (fd.inDto()
                                                        && fd.getRelType() != RelationshipType.NONE
                                                        && !fd.isEmbedded()))
                        .toList();

        StringJoiner fields = new StringJoiner(", ");
        for (FieldDescriptor field : requestFields) {
            fields.add("\\\"" + field.getName() + "\\\": " + getSampleValue(field));
        }
        body.append(fields);
        body.append("}");
        return body.toString();
    }

    static String getSampleValue(FieldDescriptor field) {
        String typeName = field.getType().toString();
        if (field.getRelType() != null && field.getRelType() == RelationshipType.MANY_TO_ONE) {
            String relatedType = field.getType().toString();
            if (relatedType.contains(".")) {
                relatedType = relatedType.substring(relatedType.lastIndexOf('.') + 1);
            }
            String varName = StringCase.SNAKE.apply(relatedType) + "_id";
            return "\\\"{{ " + varName + " }}\\\"";
        }
        if (typeName.contains("String")) {
            return "\\\"\\\"";
        }
        if (typeName.contains("Integer")
                || typeName.contains("int")
                || typeName.contains("Long")
                || typeName.contains("long")) {
            return "0";
        }
        if (typeName.contains("Double")
                || typeName.contains("double")
                || typeName.contains("Float")
                || typeName.contains("float")
                || typeName.contains("BigDecimal")) {
            return "0.0";
        }
        if (typeName.contains("Boolean") || typeName.contains("boolean")) {
            return "false";
        }
        if (typeName.contains("Instant")
                || typeName.contains("LocalDate")
                || typeName.contains("OffsetDateTime")
                || typeName.contains("ZonedDateTime")) {
            return "\\\"2024-01-01T00:00:00Z\\\"";
        }
        if (typeName.contains(CollectionTypes.SET) || typeName.contains(CollectionTypes.LIST)) {
            return "[]";
        }
        return "null";
    }

    static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    static String jsonFormat(String template, Object... args) {
        return String.format(template.replace("\r\n", "\n").replace("\n", "%n"), args);
    }

    static String formatDisplayName(String name) {
        if (name.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder().append(name.charAt(0));
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append(" ");
            }
            result.append(c);
        }
        return result.toString();
    }
}
