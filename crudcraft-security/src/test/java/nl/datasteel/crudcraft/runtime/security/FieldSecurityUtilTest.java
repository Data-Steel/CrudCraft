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
package nl.datasteel.crudcraft.runtime.security;

import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class FieldSecurityUtilTest {

    static class Dto {
        @FieldSecurity(readRoles = {})
        private String secret = "hash";

        public String getSecret() {
            return secret;
        }
    }

    static class DtoProxy extends Dto { }

    enum Status { ACTIVE }

    static class EnumDto {
        private Status status = Status.ACTIVE;

        public Status getStatus() {
            return status;
        }
    }

    static class EnumCollectionDto {
        @FieldSecurity(readRoles = {})
        private String secret = "hash";
        private Set<Status> statuses = Set.of(Status.ACTIVE);

        public String getSecret() {
            return secret;
        }

        public Set<Status> getStatuses() {
            return statuses;
        }
    }

    @Test
    void filterReadRedactsFieldsOnSubclass() {
        DtoProxy dto = new DtoProxy();
        FieldSecurityUtil.filterRead(dto);
        assertNull(dto.getSecret());
    }

    @Test
    void filterReadIgnoresEnums() {
        EnumDto dto = new EnumDto();
        assertDoesNotThrow(() -> FieldSecurityUtil.filterRead(dto));
        assertEquals(Status.ACTIVE, dto.getStatus());
    }

    @Test
    void filterReadProcessesCollectionsWithEnums() {
        EnumCollectionDto dto = new EnumCollectionDto();
        assertDoesNotThrow(() -> FieldSecurityUtil.filterRead(dto));
        assertNull(dto.getSecret());
        assertEquals(Set.of(Status.ACTIVE), dto.getStatuses());
    }

    @Test
    void canReadReturnsFalseWhenNoAuthentication() {
        assertFalse(FieldSecurityUtil.canRead(new String[]{"USER"}));
    }

    @Test
    void canReadReturnsTrueWhenRolePresent() {
        var auth = new UsernamePasswordAuthenticationToken("u", "p",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            assertTrue(FieldSecurityUtil.canRead(new String[]{"USER"}));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void canWriteHandlesAllRole() {
        assertTrue(FieldSecurityUtil.canWrite(new String[]{"ALL"}));
    }

    @Test
    void canWriteReturnsFalseForNullOrEmptyRoles() {
        assertFalse(FieldSecurityUtil.canWrite(null));
        assertFalse(FieldSecurityUtil.canWrite(new String[0]));
    }

    @Test
    void filterReadHandlesNullInput() {
        assertNull(FieldSecurityUtil.filterRead(null));
    }

    @Test
    void filterReadRedactsArrayFields() {
        class ArrayDto {
            @FieldSecurity(readRoles = {})
            private String[] secrets = {"a", "b"};
            String[] getSecrets() { return secrets; }
        }
        ArrayDto dto = new ArrayDto();
        FieldSecurityUtil.filterRead(dto);
        assertNull(dto.getSecrets());
    }

    @Test
    void filterWriteRestoresOriginalValueWhenDenied() {
        class WriteDto {
            @FieldSecurity(writeRoles = {})
            private String secret = "new";
            String getSecret() { return secret; }
        }
        class Existing {
            private String secret = "old";
            String getSecret() { return secret; }
        }
        WriteDto dto = new WriteDto();
        WriteDto result = FieldSecurityUtil.filterWrite(dto, new Existing());
        assertEquals("old", result.getSecret());
    }

    @Test
    void filterWriteThrowsWhenPolicyFailOnDenied() {
        class FailDto {
            @FieldSecurity(writeRoles = {}, writePolicy = WritePolicy.FAIL_ON_DENIED)
            private String secret = "new";
        }
        FailDto dto = new FailDto();
        assertThrows(AccessDeniedException.class, () -> FieldSecurityUtil.filterWrite(dto));
    }
}
