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

package nl.datasteel.crudcraft.runtime.security.policy;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class SecurityPolicyTest {

    @Test
    void constantPoliciesReturnExpectedExpressions() {
        assertEquals(
                "hasRole('ADMIN')",
                new AdminOnlySecurityPolicy().getSecurityExpression(CrudEndpoint.GET_ONE));
        assertEquals(
                "isAuthenticated()",
                new AuthenticatedSecurityPolicy().getSecurityExpression(CrudEndpoint.GET_ONE));
        assertEquals("denyAll()", new DenyAllSecurityPolicy().getSecurityExpression(CrudEndpoint.GET_ONE));
        assertEquals(
                "permitAll()",
                new PermitAllSecurityPolicy().getSecurityExpression(CrudEndpoint.GET_ONE));
    }

    @Test
    void readPublicWriteAdminPolicySeparatesPublicAndRestrictedEndpoints() {
        CrudSecurityPolicy policy = new ReadPublicWriteAdminPolicy();

        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.GET_ALL));
        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.SEARCH));
        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.EXPORT));
        assertEquals("hasRole('ADMIN')", policy.getSecurityExpression(CrudEndpoint.PUT));
    }

    @Test
    void writeOnlyPolicyAllowsWritesAndDeniesReads() {
        CrudSecurityPolicy policy = new WriteOnlySecurityPolicy();

        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.POST));
        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.BULK_DELETE));
        assertEquals("denyAll()", policy.getSecurityExpression(CrudEndpoint.GET_ALL));
    }

    @Test
    void roleBasedPolicyUsesConfiguredRolesAndDeniesMissingEndpoints() {
        RoleBasedCrudSecurityPolicy policy =
                new RoleBasedCrudSecurityPolicy(
                        Map.of(CrudEndpoint.GET_ONE, "VIEWER", CrudEndpoint.DELETE, "ADMIN"));

        assertEquals("hasRole('VIEWER')", policy.getSecurityExpression(CrudEndpoint.GET_ONE));
        assertEquals("hasRole('ADMIN')", policy.getSecurityExpression(CrudEndpoint.DELETE));
        assertEquals("denyAll()", policy.getSecurityExpression(CrudEndpoint.GET_ALL));
    }

    @Test
    void roleBasedPolicyWithoutRolesDeniesAll() {
        CrudSecurityPolicy policy = new RoleBasedCrudSecurityPolicy();

        assertEquals("denyAll()", policy.getSecurityExpression(CrudEndpoint.POST));
    }

    @Test
    void roleBasedPolicyRejectsNullConfiguration() {
        assertThrows(NullPointerException.class, () -> new RoleBasedCrudSecurityPolicy(null));
    }

    @Test
    void endpointSwitchPoliciesRejectNullEndpoint() {
        assertThrows(
                NullPointerException.class,
                () -> new ReadPublicWriteAdminPolicy().getSecurityExpression(null));
        assertThrows(
                NullPointerException.class,
                () -> new WriteOnlySecurityPolicy().getSecurityExpression(null));

        assertEquals("denyAll()", new RoleBasedCrudSecurityPolicy().getSecurityExpression(null));
    }

    @Test
    void roleBasedPolicyIsolatedFromLaterMapMutationAndTreatsNullValueAsDenied() {
        EnumMap<CrudEndpoint, String> roles = new EnumMap<>(CrudEndpoint.class);
        roles.put(CrudEndpoint.GET_ONE, "VIEWER");
        roles.put(CrudEndpoint.PUT, null);

        RoleBasedCrudSecurityPolicy policy = new RoleBasedCrudSecurityPolicy(roles);
        roles.put(CrudEndpoint.GET_ONE, "ADMIN");

        assertEquals("hasRole('VIEWER')", policy.getSecurityExpression(CrudEndpoint.GET_ONE));
        assertEquals("denyAll()", policy.getSecurityExpression(CrudEndpoint.PUT));
    }

    @Test
    void roleBasedPolicyRejectsNullEndpointKey() {
        Map<CrudEndpoint, String> roles = new HashMap<>();
        roles.put(null, "ADMIN");

        assertThrows(NullPointerException.class, () -> new RoleBasedCrudSecurityPolicy(roles));
    }
}
