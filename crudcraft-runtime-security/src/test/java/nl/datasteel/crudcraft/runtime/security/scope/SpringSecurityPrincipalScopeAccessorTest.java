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

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SpringSecurityPrincipalScopeAccessorTest {

    private final SpringSecurityPrincipalScopeAccessor accessor =
            new SpringSecurityPrincipalScopeAccessor();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsClaimsAndRolesFromAuthentication() {
        var principal =
                Map.of(
                        "sub", "user-1",
                        "tenant_id", "tenant-9",
                        "client_id", "client-3");
        var auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        "n/a",
                        Set.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("SUPPORT")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(accessor.isAuthenticated());
        assertEquals("user-1", accessor.currentUserId().orElseThrow());
        assertEquals("tenant-9", accessor.claim("tenant_id").orElseThrow());
        assertEquals(Set.of("ADMIN", "SUPPORT"), accessor.roles());
    }

    @Test
    void missingAuthenticationFailsClosed() {
        assertFalse(accessor.isAuthenticated());
        assertTrue(accessor.claim("sub").isEmpty());
        assertTrue(accessor.roles().isEmpty());
    }

    @Test
    void readsSubFromUserDetailsPrincipal() {
        var principal =
                User.withUsername("subject-user").password("n/a").authorities("ROLE_USER").build();
        var auth =
                new UsernamePasswordAuthenticationToken(
                        principal, "n/a", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("subject-user", accessor.claim("sub").orElseThrow());
    }

    @Test
    void readsSubFromPrincipalDetails() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        "fallback", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        auth.setDetails((Principal) () -> "principal-sub");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("principal-sub", accessor.claim("sub").orElseThrow());
    }

    @Test
    void readsClaimsViaGetClaimMethod() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        new ClaimCarrier(), "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("tenant-44", accessor.claim("tenant_id").orElseThrow());
    }

    @Test
    void readsClaimsViaGetClaimAsStringFallback() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        new ClaimAsStringCarrier(),
                        "n/a",
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("client-2", accessor.claim("client_id").orElseThrow());
    }

    @Test
    void readsClaimsViaGetClaimsFallback() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        new ClaimsMapCarrier(),
                        "n/a",
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("region-a", accessor.claim("region").orElseThrow());
    }

    @Test
    void claimWithBlankNameReturnsEmpty() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        new ClaimCarrier(), "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(accessor.claim(" ").isEmpty());
    }

    @Test
    void claimWithNullNameReturnsEmpty() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        new ClaimCarrier(), "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(accessor.claim(null).isEmpty());
    }

    @Test
    void propagatesRuntimeErrorsFromClaimMethods() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        "principal",
                        new RuntimeThrowingCarrier(),
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(IllegalArgumentException.class, () -> accessor.claim("tenant_id"));
    }

    @Test
    void wrapsCheckedErrorsFromClaimMethods() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        "principal",
                        new CheckedThrowingCarrier(),
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> accessor.claim("tenant_id"));
        Assertions.assertTrue(ex.getMessage().contains("getClaim"));
    }

    @Test
    void rolesIgnoreNullAuthorities() {
        GrantedAuthority nullAuthority = () -> null;
        var auth =
                new UsernamePasswordAuthenticationToken(
                        "principal",
                        "n/a",
                        Set.of(
                                nullAuthority,
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("RAW")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(Set.of("ADMIN", "RAW"), accessor.roles());
    }

    @Test
    void fallsBackToAuthenticationNameForSubClaim() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        "auth-name", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("auth-name", accessor.claim("sub").orElseThrow());
    }

    @Test
    void returnsEmptyWhenClaimCannotBeResolved() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        "auth-name", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(accessor.claim("tenant_id").isEmpty());
    }

    @Test
    void userDetailsPrincipalDoesNotResolveNonSubClaim() {
        var principal =
                User.withUsername("subject-user").password("n/a").authorities("ROLE_USER").build();
        var auth =
                new UsernamePasswordAuthenticationToken(
                        principal, "n/a", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(accessor.claim("tenant_id").isEmpty());
    }

    @Test
    void principalTypeDoesNotResolveNonSubClaim() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        (Principal) () -> "subject-user",
                        "n/a",
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(accessor.claim("tenant_id").isEmpty());
    }

    @Test
    void ignoresGetClaimsResultWhenItIsNotAMap() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        new NonMapClaimsCarrier(),
                        "n/a",
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(accessor.claim("tenant_id").isEmpty());
    }

    @Test
    void propagatesRuntimeErrorsFromNoArgClaimMethod() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        new RuntimeThrowingClaimsMapCarrier(),
                        "n/a",
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(RuntimeException.class, () -> accessor.claim("tenant_id"));
    }

    @Test
    void wrapsCheckedErrorsFromNoArgClaimMethod() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        new CheckedThrowingClaimsMapCarrier(),
                        "n/a",
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> accessor.claim("tenant_id"));
        Assertions.assertTrue(ex.getMessage().contains("getClaims"));
    }

    @Test
    void reportsUnauthenticatedWhenAuthenticationFlagIsFalse() {
        var auth = new UsernamePasswordAuthenticationToken("auth-name", "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertFalse(accessor.isAuthenticated());
    }

    @Test
    void rolesReturnsEmptyWhenAuthoritiesCollectionIsNull() {
        AuthenticationWithNullAuthorities auth = new AuthenticationWithNullAuthorities();
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(accessor.roles().isEmpty());
    }

    @Test
    void userDetailsSubClaimTakesPrecedenceOverAuthenticationNameFallback() {
        var principal =
                User.withUsername("principal-sub").password("n/a").authorities("ROLE_USER").build();
        var auth = new AuthenticationWithCustomName(principal, "fallback-sub");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("principal-sub", accessor.claim("sub").orElseThrow());
    }

    @Test
    void readsClaimFromCredentialsWhenPrincipalAndDetailsDoNotContainClaim() {
        var auth =
                new UsernamePasswordAuthenticationToken(
                        "principal",
                        Map.of("tenant_id", "tenant-from-credentials"),
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        auth.setDetails(Map.of("different", "value"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("tenant-from-credentials", accessor.claim("tenant_id").orElseThrow());
    }

    private static final class ClaimCarrier {
        public Object getClaim(String name) {
            return "tenant_id".equals(name) ? "tenant-44" : null;
        }
    }

    private static final class ClaimAsStringCarrier {
        public String getClaimAsString(String name) {
            return "client_id".equals(name) ? "client-2" : null;
        }
    }

    private static final class ClaimsMapCarrier {
        public Map<String, Object> getClaims() {
            return Map.of("region", "region-a");
        }
    }

    private static final class NonMapClaimsCarrier {
        public String getClaims() {
            return "not-a-map";
        }
    }

    private static final class RuntimeThrowingClaimsMapCarrier {
        public Map<String, Object> getClaims() {
            throw new RuntimeException("boom");
        }
    }

    private static final class CheckedThrowingClaimsMapCarrier {
        public Map<String, Object> getClaims() throws Exception {
            throw new Exception("boom");
        }
    }

    private static final class RuntimeThrowingCarrier {
        public Object getClaim(String name) {
            throw new IllegalArgumentException("boom");
        }
    }

    private static final class CheckedThrowingCarrier {
        public Object getClaim(String name) throws Exception {
            throw new Exception("boom");
        }
    }

    private static final class AuthenticationWithNullAuthorities
            extends UsernamePasswordAuthenticationToken {

        AuthenticationWithNullAuthorities() {
            super("principal", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        }

        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return null;
        }
    }

    private static final class AuthenticationWithCustomName
            extends UsernamePasswordAuthenticationToken {

        private final String name;

        AuthenticationWithCustomName(Object principal, String name) {
            super(principal, "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER")));
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
