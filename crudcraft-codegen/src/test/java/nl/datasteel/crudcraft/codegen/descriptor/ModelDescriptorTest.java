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

package nl.datasteel.crudcraft.codegen.descriptor;

import java.util.List;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ModelDescriptorTest {

    static class P implements CrudSecurityPolicy {
        @Override public String getSecurityExpression(CrudEndpoint endpoint) { return ""; }
    }

    private FieldDescriptor field() {
        return new FieldDescriptor(new Identity("f", null), null, null, null, null, null, null);
    }

    private ModelDescriptor sample() {
        ModelIdentity id = new ModelIdentity("M", "pkg", List.of(field()), "base");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions ep = new EndpointOptions(CrudTemplate.FULL, new CrudEndpoint[0], new CrudEndpoint[0], CrudTemplate.class);
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
        assertEquals(CrudTemplate.FULL, md.getTemplate());
        assertEquals(0, md.getOmitEndpoints().length);
        assertEquals(0, md.getIncludeEndpoints().length);
        assertTrue(md.isSecure());
        assertEquals(P.class, md.getSecurityPolicy());
        assertTrue(md.getRowSecurityHandlers().isEmpty());
    }

    @Test
    void equalsHashCodeAndToString() {
        ModelDescriptor a = sample();
        ModelDescriptor b = sample();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.toString().contains("M"));
    }

    @Test
    void notEqualWhenNameDiffers() {
        ModelDescriptor a = sample();
        ModelIdentity id2 = new ModelIdentity("N", "pkg", List.of(field()), "base");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions ep = new EndpointOptions(CrudTemplate.FULL, new CrudEndpoint[0], new CrudEndpoint[0], CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(true, P.class, List.of());
        ModelDescriptor b = new ModelDescriptor(id2, flags, ep, sec);
        assertNotEquals(a, b);
    }
}
