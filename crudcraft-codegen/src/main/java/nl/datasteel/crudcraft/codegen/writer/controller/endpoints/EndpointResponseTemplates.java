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

import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;


/** Reusable response wrapper shapes shared by generated endpoint methods. */
enum EndpointResponseTemplates {
    RESPONSE_ENTITY {
        @Override
        TypeName wrap(TypeName bodyType) {
            return ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, bodyType);
        }
    },

    LIST_RESPONSE_ENTITY {
        @Override
        TypeName wrap(TypeName bodyType) {
            return ParameterizedTypeName.get(
                    EndpointSupport.RESP_ENTITY,
                    ParameterizedTypeName.get(EndpointSupport.LIST, bodyType));
        }
    },

    BULK_RESPONSE_ENTITY {
        @Override
        TypeName wrap(TypeName bodyType) {
            return ParameterizedTypeName.get(
                    EndpointSupport.RESP_ENTITY,
                    ParameterizedTypeName.get(EndpointSupport.BULK_RESULT, bodyType));
        }
    },

    PAGINATED_RESPONSE_ENTITY {
        @Override
        TypeName wrap(TypeName bodyType) {
            return ParameterizedTypeName.get(
                    EndpointSupport.RESP_ENTITY,
                    ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, bodyType));
        }
    };

    abstract TypeName wrap(TypeName bodyType);
}
