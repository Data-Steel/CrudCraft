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

import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.sample.audit.AuditLog;
import nl.datasteel.crudcraft.sample.user.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Security rules for AuditLog: read-only for admins and auditors. Illustrates
 * how to deny write operations entirely.
 */
@Component
public class AuditLogSecurityPolicy implements CrudSecurityPolicy, RowSecurityHandler<AuditLog> {

    /** Repository used to look up the currently authenticated user. */
    private final UserRepository userRepository;

    public AuditLogSecurityPolicy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Only administrators and auditors may read audit logs; all mutating
     * operations are denied.
     */
    @Override
    public String getSecurityExpression(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case GET_ALL, GET_ONE, SEARCH -> "hasAnyRole('ADMIN','AUDITOR')";
            default -> "denyAll()";
        };
    }

    /**
     * Row-level filter placeholder. Real implementations could scope logs by
     * tenant.
     */
    @Override
    public Specification<AuditLog> rowFilter() {
        String username = SecurityUtil.currentUsername();
        UUID userId = userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElse(null);
        return (root, query, cb) -> userId == null
                ? cb.disjunction()
                : cb.equal(root.get("actorUserId"), userId);
    }
}
