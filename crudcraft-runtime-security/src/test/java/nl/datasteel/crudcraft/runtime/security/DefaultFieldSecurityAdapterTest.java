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

package nl.datasteel.crudcraft.runtime.security;

import java.util.Set;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DefaultFieldSecurityAdapterTest {

    private final DefaultFieldSecurityAdapter adapter = new DefaultFieldSecurityAdapter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void delegatesReadFilterToUtility() {
        SecuredDto dto = new SecuredDto();
        dto.secret = "hidden";
        authenticateUserRole();

        SecuredDto result = adapter.filterRead(dto);

        assertSame(dto, result);
        assertNull(dto.secret);
    }

    @Test
    void delegatesWriteFilterToUtility() {
        SecuredDto existing = new SecuredDto();
        existing.secret = "persisted";
        SecuredDto request = new SecuredDto();
        request.secret = "changed";
        authenticateUserRole();

        SecuredDto result = adapter.filterWrite(request, existing);

        assertSame(request, result);
        assertEquals("persisted", request.secret);
    }

    @Test
    void delegatesCanReadFieldToUtility() {
        authenticateUserRole();
        assertFalse(adapter.canReadField(SecuredDto.class, "secret"));
    }

    @Test
    void delegatesCanReadFieldForUnsecuredField() {
        authenticateUserRole();
        assertTrue(adapter.canReadField(OpenDto.class, "visible"));
    }

    @Test
    void accessDeniedExceptionSupportsCause() {
        RuntimeException cause = new RuntimeException("cause");
        AccessDeniedException ex = new AccessDeniedException("denied", cause);

        assertEquals("denied", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertThrows(
                AccessDeniedException.class,
                () -> {
                    throw ex;
                });
    }

    private void authenticateUserRole() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user", "n/a", Set.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private static final class SecuredDto {
        @FieldSecurity(
                readRoles = {"ADMIN"},
                writeRoles = {"ADMIN"},
                writePolicy = WritePolicy.SKIP_ON_DENIED)
        private String secret;
    }

    private static final class OpenDto {
        private String visible;
    }
}
