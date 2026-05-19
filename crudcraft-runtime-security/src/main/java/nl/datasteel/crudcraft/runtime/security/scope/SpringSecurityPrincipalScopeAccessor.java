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

package nl.datasteel.crudcraft.runtime.security.scope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


/** Default principal scope accessor based on Spring Security context. */
public class SpringSecurityPrincipalScopeAccessor implements PrincipalScopeAccessor {

    /** Creates a Spring Security based principal scope accessor. */
    public SpringSecurityPrincipalScopeAccessor() {
        // Constructor without any parameters stays empty
    }

    @Override
    public Optional<Object> claim(String claimName) {
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        return claimFrom(authentication.getPrincipal(), claimName)
                .or(() -> claimFrom(authentication.getDetails(), claimName))
                .or(() -> claimFrom(authentication.getCredentials(), claimName))
                .or(
                        () ->
                                "sub".equals(claimName)
                                        ? Optional.ofNullable(authentication.getName())
                                        : Optional.empty());
    }

    @Override
    public Set<String> roles() {
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
            return Set.of();
        }
        if (authentication.getAuthorities() == null) {
            return Set.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String value = authority.getAuthority();
            if (value == null) {
                continue;
            }
            if (value.startsWith("ROLE_")) {
                normalized.add(value.substring("ROLE_".length()));
            } else {
                normalized.add(value);
            }
        }
        return Set.copyOf(normalized);
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = currentAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    private Authentication currentAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        return context.getAuthentication();
    }

    private Optional<Object> claimFrom(Object source, String claimName) {
        if (source == null || claimName == null || claimName.isBlank()) {
            return Optional.empty();
        }
        if (source instanceof Map<?, ?> map) {
            return Optional.ofNullable(map.get(claimName));
        }
        if (source instanceof UserDetails userDetails && "sub".equals(claimName)) {
            return Optional.ofNullable(userDetails.getUsername());
        }
        if (source instanceof Principal principal && "sub".equals(claimName)) {
            return Optional.ofNullable(principal.getName());
        }
        return invokeClaimMethod(source, "getClaim", String.class, claimName)
                .or(() -> invokeClaimMethod(source, "getClaimAsString", String.class, claimName))
                .or(() -> invokeClaimsMap(source, claimName));
    }

    private Optional<Object> invokeClaimsMap(Object source, String claimName) {
        Optional<Object> claims = invokeClaimMethod(source, "getClaims");
        if (claims.isEmpty() || !(claims.get() instanceof Map<?, ?> map)) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(claimName));
    }

    private Optional<Object> invokeClaimMethod(
            Object source, String methodName, Class<?> argType, Object argValue) {
        try {
            Method method = source.getClass().getMethod(methodName, argType);
            return Optional.ofNullable(method.invoke(source, argValue));
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            return Optional.empty();
        } catch (InvocationTargetException ex) {
            throw invocationFailure(methodName, ex);
        }
    }

    private Optional<Object> invokeClaimMethod(Object source, String methodName) {
        try {
            Method method = source.getClass().getMethod(methodName);
            return Optional.ofNullable(method.invoke(source));
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            return Optional.empty();
        } catch (InvocationTargetException ex) {
            throw invocationFailure(methodName, ex);
        }
    }

    private IllegalStateException invocationFailure(
            String methodName, InvocationTargetException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        return new IllegalStateException("Failed to read claim via " + methodName, cause);
    }
}
