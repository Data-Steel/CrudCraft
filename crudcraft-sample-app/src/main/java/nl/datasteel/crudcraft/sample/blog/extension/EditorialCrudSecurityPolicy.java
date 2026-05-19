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

package nl.datasteel.crudcraft.sample.blog.extension;

import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;


/** Sample endpoint policy for blog-editor style generated controllers. */
public final class EditorialCrudSecurityPolicy implements CrudSecurityPolicy {

    @Override
    public String getSecurityExpression(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case GET_ALL, GET_ALL_REF, GET_ONE, FIND_BY_IDS, SEARCH, COUNT, EXISTS ->
                "permitAll()";
            case POST, PUT, PATCH, BULK_CREATE, BULK_UPDATE, BULK_PATCH, BULK_UPSERT, VALIDATE ->
                "hasAnyRole('EDITOR','ADMIN')";
            case DELETE -> "hasRole('ADMIN')";
            case BULK_DELETE -> "hasRole('ADMIN')";
            case EXPORT -> "hasRole('REPORTING')";
        };
    }
}
