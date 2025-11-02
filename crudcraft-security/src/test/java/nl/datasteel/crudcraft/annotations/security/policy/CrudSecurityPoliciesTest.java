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
package nl.datasteel.crudcraft.annotations.security.policy;

import java.util.Map;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CrudSecurityPoliciesTest {

    @Test
    void denyAllPolicyAlwaysDenies() {
        DenyAllSecurityPolicy p = new DenyAllSecurityPolicy();
        assertEquals("denyAll()", p.getSecurityExpression(CrudEndpoint.GET_ONE));
    }

    @Test
    void permitAllPolicyAlwaysPermits() {
        PermitAllSecurityPolicy p = new PermitAllSecurityPolicy();
        assertEquals("permitAll()", p.getSecurityExpression(CrudEndpoint.GET_ONE));
    }

    @Test
    void readPublicWriteAdminPolicyDiffersByEndpoint() {
        ReadPublicWriteAdminPolicy p = new ReadPublicWriteAdminPolicy();
        assertEquals("permitAll()", p.getSecurityExpression(CrudEndpoint.GET_ONE));
        assertEquals("hasRole('ADMIN')", p.getSecurityExpression(CrudEndpoint.POST));
    }

    @Test
    void writeOnlySecurityPolicyAllowsOnlyWrites() {
        WriteOnlySecurityPolicy p = new WriteOnlySecurityPolicy();
        assertEquals("permitAll()", p.getSecurityExpression(CrudEndpoint.POST));
        assertEquals("denyAll()", p.getSecurityExpression(CrudEndpoint.GET_ONE));
    }

    @Test
    void adminOnlyPolicyRequiresAdminRole() {
        AdminOnlySecurityPolicy p = new AdminOnlySecurityPolicy();
        assertEquals("hasRole('ADMIN')", p.getSecurityExpression(CrudEndpoint.GET_ONE));
    }

    @Test
    void authenticatedPolicyRequiresAuthentication() {
        AuthenticatedSecurityPolicy p = new AuthenticatedSecurityPolicy();
        assertEquals("isAuthenticated()", p.getSecurityExpression(CrudEndpoint.GET_ONE));
    }

    @Test
    void roleBasedPolicyUsesMapAndDefaultsToDenyAll() {
        RoleBasedCrudSecurityPolicy p = new RoleBasedCrudSecurityPolicy(
                Map.of(CrudEndpoint.GET_ONE, "USER"));
        assertEquals("hasRole('USER')", p.getSecurityExpression(CrudEndpoint.GET_ONE));
        assertEquals("denyAll()", p.getSecurityExpression(CrudEndpoint.POST));
    }

    @Test
    void roleBasedPolicyNullMapThrows() {
        assertThrows(NullPointerException.class, () -> new RoleBasedCrudSecurityPolicy(null));
    }
}
