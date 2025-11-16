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
package nl.datasteel.crudcraft.annotations.classes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.security.policy.PermitAllSecurityPolicy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CrudCraftedTest {

    @Test
    void defaultsAreAsExpected() throws Exception {
        assertEquals(false, CrudCrafted.class.getMethod("editable").getDefaultValue());
        assertEquals("", CrudCrafted.class.getMethod("basePackage").getDefaultValue());
        assertEquals(CrudTemplate.FULL, CrudCrafted.class.getMethod("template").getDefaultValue());
        assertArrayEquals(new CrudEndpoint[0],
                (Object[]) CrudCrafted.class.getMethod("omitEndpoints").getDefaultValue());
        assertArrayEquals(new CrudEndpoint[0],
                (Object[]) CrudCrafted.class.getMethod("includeEndpoints").getDefaultValue());
        assertEquals(CrudTemplate.class,
                CrudCrafted.class.getMethod("endpointPolicy").getDefaultValue());
        assertEquals(true, CrudCrafted.class.getMethod("secure").getDefaultValue());
        assertEquals(PermitAllSecurityPolicy.class,
                CrudCrafted.class.getMethod("securityPolicy").getDefaultValue());
    }

    @Test
    void metaAnnotationsConfiguredProperly() {
        Target target = CrudCrafted.class.getAnnotation(Target.class);
        assertArrayEquals(new ElementType[]{ElementType.TYPE}, target.value());
        Retention retention = CrudCrafted.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.SOURCE, retention.value());
    }
}

