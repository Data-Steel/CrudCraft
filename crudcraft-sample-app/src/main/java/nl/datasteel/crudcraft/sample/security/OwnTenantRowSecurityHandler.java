/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.sample.security;

import java.lang.reflect.Method;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.AccessDeniedException;
import nl.datasteel.crudcraft.sample.tenant.Tenant;
import nl.datasteel.crudcraft.sample.user.User;
import nl.datasteel.crudcraft.sample.user.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Generic row security policy that restricts access to data within the
 * authenticated user's tenant.
 */
@Component
public class OwnTenantRowSecurityHandler implements RowSecurityHandler<Object> {

    private final UserRepository userRepository;

    public OwnTenantRowSecurityHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Specification<Object> rowFilter() {
        String username = SecurityUtil.currentUsername();
        UUID tenantId = userRepository.findByUsername(username)
                .map(u -> u.getTenant().getId())
                .orElse(null);
        return (root, query, cb) -> {
            if (tenantId == null) {
                return cb.disjunction();
            }
            try {
                return cb.equal(root.get("tenant").get("id"), tenantId);
            } catch (IllegalArgumentException ex) {
                try {
                    return cb.equal(root.get("account").get("tenant").get("id"), tenantId);
                } catch (IllegalArgumentException ex2) {
                    try {
                        return cb.equal(root.get("customer").get("tenant").get("id"), tenantId);
                    } catch (IllegalArgumentException ex3) {
                        return cb.equal(root.get("id"), tenantId);
                    }
                }
            }
        };
    }

    @Override
    public void apply(Object entity) {
        String username = SecurityUtil.currentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("No authenticated user"));
        Tenant tenant = user.getTenant();
        if (tenant == null) {
            return;
        }
        Object current = resolveTenant(entity);
        if (entity instanceof Tenant t) {
            if (!t.getId().equals(tenant.getId())) {
                throw new AccessDeniedException("Cross-tenant access denied");
            }
        } else if (current == null) {
            setProperty(entity, "tenant", tenant);
        } else if (current instanceof Tenant t && !t.getId().equals(tenant.getId())) {
            throw new AccessDeniedException("Cross-tenant access denied");
        }
    }

    private Object resolveTenant(Object entity) {
        Object direct = getProperty(entity, "tenant");
        if (direct != null) {
            return direct;
        }
        Object account = getProperty(entity, "account");
        if (account != null) {
            Object accTenant = getProperty(account, "tenant");
            if (accTenant != null) {
                return accTenant;
            }
        }
        Object customer = getProperty(entity, "customer");
        if (customer != null) {
            Object custTenant = getProperty(customer, "tenant");
            if (custTenant != null) {
                return custTenant;
            }
        }
        return null;
    }

    private Object getProperty(Object obj, String name) {
        try {
            Method m = obj.getClass().getMethod("get" + capitalize(name));
            return m.invoke(obj);
        } catch (Exception ex) {
            return null;
        }
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private void setProperty(Object obj, String name, Object value) {
        try {
            Method m = obj.getClass().getMethod("set" + capitalize(name), value.getClass());
            m.invoke(obj, value);
        } catch (Exception ex) {
            // ignore if property not present
        }
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

