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

import java.lang.reflect.Method;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.AccessDeniedException;
import nl.datasteel.crudcraft.sample.branch.Branch;
import nl.datasteel.crudcraft.sample.user.User;
import nl.datasteel.crudcraft.sample.user.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Generic row security policy restricting access to the caller's branch.
 */
@Component
public class OwnBranchRowSecurityHandler implements RowSecurityHandler<Object> {

    private final UserRepository userRepository;

    public OwnBranchRowSecurityHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Specification<Object> rowFilter() {
        String username = SecurityUtil.currentUsername();
        UUID branchId = userRepository.findByUsername(username)
                .map(u -> u.getBranch() == null ? null : u.getBranch().getId())
                .orElse(null);
        return (root, query, cb) -> {
            if (branchId == null) {
                return cb.disjunction();
            }
            try {
                return cb.equal(root.get("branch").get("id"), branchId);
            } catch (IllegalArgumentException ex) {
                try {
                    return cb.equal(root.get("account").get("branch").get("id"), branchId);
                } catch (IllegalArgumentException ex2) {
                    return cb.disjunction();
                }
            }
        };
    }

    @Override
    public void apply(Object entity) {
        String username = SecurityUtil.currentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("No authenticated user"));
        Branch branch = user.getBranch();
        if (branch == null) {
            return;
        }
        Object current = resolveBranch(entity);
        if (current == null) {
            setProperty(entity, "branch", branch);
        } else if (current instanceof Branch b && !b.getId().equals(branch.getId())) {
            throw new AccessDeniedException("Cross-branch access denied");
        }
    }

    private Object resolveBranch(Object entity) {
        Object direct = getProperty(entity, "branch");
        if (direct != null) {
            return direct;
        }
        Object account = getProperty(entity, "account");
        if (account != null) {
            Object accBranch = getProperty(account, "branch");
            if (accBranch != null) {
                return accBranch;
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
            // ignore
        }
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

