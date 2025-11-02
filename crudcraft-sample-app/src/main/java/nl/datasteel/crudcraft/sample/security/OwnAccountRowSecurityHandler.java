/*
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

import jakarta.persistence.criteria.From;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.AccessDeniedException;
import nl.datasteel.crudcraft.sample.account.Account;
import nl.datasteel.crudcraft.sample.customer.AccountHolder;
import nl.datasteel.crudcraft.sample.branch.Branch;
import nl.datasteel.crudcraft.sample.enums.RoleType;
import nl.datasteel.crudcraft.sample.tenant.Tenant;
import nl.datasteel.crudcraft.sample.user.User;
import nl.datasteel.crudcraft.sample.user.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Row security policy that restricts access to entities related to an account
 * owned by the authenticated tenant and branch.
 */
@Component
public class OwnAccountRowSecurityHandler implements RowSecurityHandler<Object> {

    private final UserRepository userRepository;

    public OwnAccountRowSecurityHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Specification<Object> rowFilter() {
        String username = SecurityUtil.currentUsername();
        User user = userRepository.findByUsername(username).orElse(null);

        UUID tenantId = user == null ? null : user.getTenant().getId();
        UUID branchId = (user == null || user.getBranch() == null) ? null : user.getBranch().getId();
        Set<RoleType> roles = user == null ? Set.of() : user.getRoles();

        return (root, query, cb) -> {
            if (tenantId == null) {
                return cb.disjunction();
            }

            From<?, Account> accountFrom;
            if (Account.class.isAssignableFrom(root.getJavaType())) {
                accountFrom = cb.treat(root, Account.class);
            } else {
                accountFrom = cb.treat(root.join("account", JoinType.LEFT), Account.class);
            }

            var predicate = cb.equal(accountFrom.get("tenant").get("id"), tenantId);

            if (branchId != null) {
                predicate = cb.and(predicate, cb.equal(accountFrom.get("branch").get("id"), branchId));
            }

            if (roles.contains(RoleType.CUSTOMER)) {
                Join<Account, AccountHolder> holderJoin = accountFrom.join("holders", JoinType.LEFT);
                predicate = cb.and(predicate,
                        cb.equal(holderJoin.get("customer").get("email"), username));
                query.distinct(true);
            }

            return predicate;
        };
    }

    @Override
    public void apply(Object entity) {
        String username = SecurityUtil.currentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("No authenticated user"));
        Account account = entity instanceof Account ? (Account) entity : (Account) getProperty(entity, "account");
        if (account == null) {
            return;
        }
        Tenant tenant = user.getTenant();
        if (tenant != null && (account.getTenant() == null || !account.getTenant().getId().equals(tenant.getId()))) {
            throw new AccessDeniedException("Cross-tenant account access denied");
        }
        Branch branch = user.getBranch();
        if (branch != null && account.getBranch() != null && !account.getBranch().getId().equals(branch.getId())) {
            throw new AccessDeniedException("Cross-branch account access denied");
        }
        if (user.getRoles().contains(RoleType.CUSTOMER)) {
            boolean owns = account.getHolders().stream()
                    .map(AccountHolder::getCustomer)
                    .anyMatch(c -> c != null && username.equals(c.getEmail()));
            if (!owns) {
                throw new AccessDeniedException("Account access denied");
            }
        }
    }

    private Object getProperty(Object obj, String name) {
        try {
            Method m = obj.getClass().getMethod("get" + capitalize(name));
            return m.invoke(obj);
        } catch (Exception ex) {
            return null;
        }
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

