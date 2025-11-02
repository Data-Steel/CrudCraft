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
package nl.datasteel.crudcraft.sample.security;

import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import org.springframework.stereotype.Component;

/**
 * Security rules for Account endpoints. Shows how to restrict CRUD operations
 * and prepare for row-level scoping.
 */
@Component
public class AccountSecurityPolicy implements CrudSecurityPolicy {

    /**
     * Defines which Spring Security expression applies per endpoint. Only
     * admins and tellers may modify accounts; all roles can read them.
     */
    @Override
    public String getSecurityExpression(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case POST, PUT, PATCH, DELETE -> "hasRole('ADMIN') or hasRole('TELLER')";
            default -> "hasAnyRole('ADMIN','AUDITOR','TELLER','CUSTOMER')";
        };
    }

}
