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
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.RowScope;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ScopeKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ModelDescriptorTest {

    static class P implements CrudSecurityPolicy {
        @Override
        public String getSecurityExpression(CrudEndpoint endpoint) {
            return "";
        }
    }

    private FieldDescriptor field() {
        return new FieldDescriptor(
                new Identity("f", null, null, SchemaMetadata.empty()),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private ModelDescriptor sample() {
        ModelIdentity id = new ModelIdentity("M", "pkg", List.of(field()), "base");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions ep =
                new EndpointOptions(
                        CrudTemplate.FULL,
                        new CrudEndpoint[0],
                        new CrudEndpoint[0],
                        CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(true, P.class, List.of());
        return new ModelDescriptor(id, flags, ep, sec);
    }

    @Test
    void gettersDelegate() {
        ModelDescriptor md = sample();
        assertEquals("M", md.getName());
        assertEquals("pkg", md.getPackageName());
        assertEquals("base", md.getBasePackage());
        assertEquals(1, md.getFields().size());
        assertTrue(md.isEditable());
        assertTrue(md.isCrudCraftEntity());
        assertFalse(md.isEmbeddable());
        assertFalse(md.isAbstract());
        assertEquals(CrudTemplate.FULL, md.getTemplate());
        assertEquals(0, md.getOmitEndpoints().length);
        assertEquals(0, md.getIncludeEndpoints().length);
        assertEquals(CrudTemplate.class, md.getEndpointPolicy());
        assertTrue(md.isSecure());
        assertEquals(P.class, md.getSecurityPolicy());
        assertTrue(md.getRowSecurityHandlers().isEmpty());
        assertTrue(md.getRowScopes().isEmpty());
        assertTrue(md.getEndpointExpressions().isEmpty());
        assertFalse(md.hasEndpointExpressions());
    }

    @Test
    void securityDelegatesExposeScopesAndEndpointExpressions() {
        RowScope scope = new RowScope(ScopeKind.TENANT, "tenantId", "tenant_id");
        ModelDescriptor md =
                new ModelDescriptor(
                        new ModelIdentity("M", "pkg", List.of(field()), "base"),
                        new ModelFlags(true, true, false, false),
                        new EndpointOptions(
                                CrudTemplate.FULL,
                                new CrudEndpoint[0],
                                new CrudEndpoint[0],
                                CrudTemplate.class),
                        new ModelSecurity(
                                true,
                                P.class,
                                List.of("Handler"),
                                List.of(scope),
                                Map.of(CrudEndpoint.GET_ONE, "hasRole('USER')")));

        assertEquals(List.of("Handler"), md.getRowSecurityHandlers());
        assertEquals(List.of(scope), md.getRowScopes());
        assertEquals("hasRole('USER')", md.getEndpointExpressions().get(CrudEndpoint.GET_ONE));
        assertTrue(md.hasEndpointExpressions());
    }

    @Test
    void flagDelegatesExposeNegativeAndPositiveBranches() {
        ModelDescriptor md =
                new ModelDescriptor(
                        new ModelIdentity("M", "pkg", List.of(field()), "base"),
                        new ModelFlags(false, false, true, true),
                        new EndpointOptions(
                                CrudTemplate.FULL,
                                new CrudEndpoint[0],
                                new CrudEndpoint[0],
                                CrudTemplate.class),
                        new ModelSecurity(false, P.class, List.of()));

        assertFalse(md.isEditable());
        assertFalse(md.isCrudCraftEntity());
        assertTrue(md.isEmbeddable());
        assertTrue(md.isAbstract());
        assertFalse(md.isSecure());
    }

    @Test
    void equalsHashCodeAndToString() {
        ModelDescriptor a = sample();
        ModelDescriptor b = sample();
        assertEquals(a, b);
        assertEquals(a, a);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(0, a.hashCode());
        assertTrue(a.toString().contains("M"));
    }

    @Test
    void notEqualWhenNameDiffers() {
        ModelDescriptor a = sample();
        ModelIdentity id2 = new ModelIdentity("N", "pkg", List.of(field()), "base");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions ep =
                new EndpointOptions(
                        CrudTemplate.FULL,
                        new CrudEndpoint[0],
                        new CrudEndpoint[0],
                        CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(true, P.class, List.of());
        ModelDescriptor b = new ModelDescriptor(id2, flags, ep, sec);
        assertNotEquals(a, b);
    }

    @Test
    void equalsBranchesForFlagsEndpointsAndSecurity() {
        ModelDescriptor base = sample();
        ModelIdentity id = new ModelIdentity("M", "pkg", List.of(field()), "base");
        ModelSecurity sec = new ModelSecurity(true, P.class, List.of());

        ModelDescriptor diffFlags =
                new ModelDescriptor(
                        id,
                        new ModelFlags(false, true, false, false),
                        new EndpointOptions(
                                CrudTemplate.FULL,
                                new CrudEndpoint[0],
                                new CrudEndpoint[0],
                                CrudTemplate.class),
                        sec);
        ModelDescriptor diffEndpoints =
                new ModelDescriptor(
                        id,
                        new ModelFlags(true, true, false, false),
                        new EndpointOptions(
                                CrudTemplate.READ_ONLY,
                                new CrudEndpoint[0],
                                new CrudEndpoint[0],
                                CrudTemplate.class),
                        sec);
        ModelDescriptor diffSecurity =
                new ModelDescriptor(
                        id,
                        new ModelFlags(true, true, false, false),
                        new EndpointOptions(
                                CrudTemplate.FULL,
                                new CrudEndpoint[0],
                                new CrudEndpoint[0],
                                CrudTemplate.class),
                        new ModelSecurity(false, P.class, List.of()));

        assertNotEquals(base, null);
        assertNotEquals(base, "x");
        assertNotEquals(base, diffFlags);
        assertNotEquals(base, diffEndpoints);
        assertNotEquals(base, diffSecurity);
    }

    @Test
    void lobFieldConvenienceAccessors() {
        FieldDescriptor requestLob =
                new FieldDescriptor(
                        new Identity("blob1", null, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], true),
                        null,
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        null,
                        null,
                        new Security(false, null, null));
        FieldDescriptor responseLobOnly =
                new FieldDescriptor(
                        new Identity("blob2", null, null, SchemaMetadata.empty()),
                        new DtoOptions(true, false, true, new String[0], true),
                        null,
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        null,
                        null,
                        new Security(false, null, null));
        ModelDescriptor md =
                new ModelDescriptor(
                        new ModelIdentity("M", "pkg", List.of(requestLob, responseLobOnly), "base"),
                        new ModelFlags(true, true, false, false),
                        new EndpointOptions(
                                CrudTemplate.FULL,
                                new CrudEndpoint[0],
                                new CrudEndpoint[0],
                                CrudTemplate.class),
                        new ModelSecurity(false, P.class, List.of()));

        assertTrue(md.hasLobFields());
        assertEquals(2, md.getLobFields().size());
        assertEquals(1, md.getRequestLobFields().size());
        assertEquals(2, md.getResponseLobFields().size());
    }

    @Test
    void lobConvenienceAccessorsReturnFalseAndEmptyWhenNoRequestLobExists() {
        FieldDescriptor responseLobOnly =
                new FieldDescriptor(
                        new Identity("blob", null, null, SchemaMetadata.empty()),
                        new DtoOptions(true, false, true, new String[0], true),
                        null,
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        null,
                        null,
                        new Security(false, null, null));
        FieldDescriptor plainRequest =
                new FieldDescriptor(
                        new Identity("name", null, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        null,
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        null,
                        null,
                        new Security(false, null, null));
        ModelDescriptor md =
                new ModelDescriptor(
                        new ModelIdentity(
                                "M", "pkg", List.of(responseLobOnly, plainRequest), "base"),
                        new ModelFlags(true, true, false, false),
                        new EndpointOptions(
                                CrudTemplate.FULL,
                                new CrudEndpoint[0],
                                new CrudEndpoint[0],
                                CrudTemplate.class),
                        new ModelSecurity(false, P.class, List.of()));

        assertFalse(md.hasLobFields());
        assertEquals(List.of(responseLobOnly), md.getLobFields());
        assertTrue(md.getRequestLobFields().isEmpty());
        assertEquals(List.of(responseLobOnly), md.getResponseLobFields());
    }
}
