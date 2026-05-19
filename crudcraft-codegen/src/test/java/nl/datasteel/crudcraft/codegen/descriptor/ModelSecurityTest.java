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

package nl.datasteel.crudcraft.codegen.descriptor;

import java.util.List;
import java.util.Map;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.RowScope;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ScopeKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ModelSecurityTest {

    static class P implements CrudSecurityPolicy {
        @Override
        public String getSecurityExpression(
                nl.datasteel.crudcraft.annotations.CrudEndpoint endpoint) {
            return "";
        }
    }

    @Test
    void gettersReturnValues() {
        ModelSecurity sec =
                new ModelSecurity(
                        true,
                        P.class,
                        List.of("Handler"),
                        List.of(new RowScope(ScopeKind.OWNER, "ownerId", "sub")),
                        Map.of(CrudEndpoint.GET_ALL, "hasRole('USER')"));
        assertTrue(sec.isSecure());
        assertEquals(P.class, sec.getSecurityPolicy());
        assertEquals(List.of("Handler"), sec.getRowSecurityHandlers());
        assertTrue(sec.secure());
        assertEquals(P.class, sec.securityPolicy());
        assertEquals(List.of("Handler"), sec.rowSecurityHandlers());
        assertEquals(1, sec.rowScopes().size());
        assertEquals(Map.of(CrudEndpoint.GET_ALL, "hasRole('USER')"), sec.endpointExpressions());
        assertEquals(1, sec.getRowScopes().size());
        assertEquals(
                Map.of(CrudEndpoint.GET_ALL, "hasRole('USER')"), sec.getEndpointExpressions());
        assertTrue(sec.hasEndpointExpressions());
    }

    @Test
    void booleanAccessorsReflectFalseAndEmptyEndpointExpressionState() {
        ModelSecurity sec = new ModelSecurity(false, P.class, List.of(), List.of(), Map.of());

        assertFalse(sec.isSecure());
        assertFalse(sec.secure());
        assertFalse(sec.hasEndpointExpressions());
    }

    @Test
    void rowHandlersListDefensivelyCopied() {
        java.util.ArrayList<String> handlers = new java.util.ArrayList<>();
        handlers.add("A");
        ModelSecurity sec = new ModelSecurity(true, P.class, handlers);
        handlers.add("B");
        assertEquals(1, sec.getRowSecurityHandlers().size());
    }

    @Test
    void equalsHashCodeAndToStringUseValueSemantics() {
        ModelSecurity a =
                new ModelSecurity(
                        true,
                        P.class,
                        List.of("Handler"),
                        List.of(new RowScope(ScopeKind.OWNER, "ownerId", "sub")),
                        Map.of(CrudEndpoint.GET_ONE, "hasRole('ADMIN')"));
        ModelSecurity b =
                new ModelSecurity(
                        true,
                        P.class,
                        List.of("Handler"),
                        List.of(new RowScope(ScopeKind.OWNER, "ownerId", "sub")),
                        Map.of(CrudEndpoint.GET_ONE, "hasRole('ADMIN')"));

        assertEquals(a, b);
        assertEquals(
                java.util.Objects.hash(
                        true,
                        P.class,
                        List.of("Handler"),
                        List.of(new RowScope(ScopeKind.OWNER, "ownerId", "sub")),
                        Map.of(CrudEndpoint.GET_ONE, "hasRole('ADMIN')")),
                a.hashCode());
        assertEquals(
                "ModelSecurity{secure=true, securityPolicy=class "
                        + "nl.datasteel.crudcraft.codegen.descriptor.ModelSecurityTest$P,"
                        + " rowSecurityHandlers=[Handler], rowScopes=[RowScope[kind=OWNER,"
                        + " field=ownerId, claim=sub]], endpointExpressions={GET_ONE=hasRole('ADMIN')}}",
                a.toString());
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
    }

    @Test
    void constructorNormalizesNullCollections() {
        ModelSecurity sec = new ModelSecurity(false, P.class, null, null, null);
        assertTrue(sec.getRowSecurityHandlers().isEmpty());
        assertTrue(sec.getRowScopes().isEmpty());
        assertTrue(sec.getEndpointExpressions().isEmpty());
    }

    @Test
    void equalsDetectsDifferentFields() {
        ModelSecurity base =
                new ModelSecurity(
                        true,
                        P.class,
                        List.of("H1"),
                        List.of(new RowScope(ScopeKind.TENANT, "tenantId", "tenant")),
                        Map.of(CrudEndpoint.GET_ALL, "hasRole('USER')"));

        assertNotEquals(base, new ModelSecurity(false, P.class, List.of("H1")));
        assertNotEquals(base, new ModelSecurity(true, null, List.of("H1")));
        assertNotEquals(base, new ModelSecurity(true, P.class, List.of("H2")));
        assertNotEquals(
                base,
                new ModelSecurity(
                        true,
                        P.class,
                        List.of("H1"),
                        List.of(new RowScope(ScopeKind.CLIENT, "clientId", "client")),
                        Map.of(CrudEndpoint.GET_ALL, "hasRole('USER')")));
        assertNotEquals(
                base,
                new ModelSecurity(
                        true,
                        P.class,
                        List.of("H1"),
                        List.of(new RowScope(ScopeKind.TENANT, "tenantId", "tenant")),
                        Map.of(CrudEndpoint.GET_ALL, "denyAll()")));
    }
}
