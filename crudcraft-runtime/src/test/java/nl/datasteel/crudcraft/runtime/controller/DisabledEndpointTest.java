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
package nl.datasteel.crudcraft.runtime.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DisabledEndpointTest {

    static class Demo {
        @DisabledEndpoint(reason = "maintenance")
        void disabled() {}
    }

    @Test
    void annotationHasRuntimeRetentionAndMethodTarget() throws NoSuchMethodException {
        Retention retention = DisabledEndpoint.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());

        java.lang.annotation.Target target = DisabledEndpoint.class.getAnnotation(java.lang.annotation.Target.class);
        assertNotNull(target);
        assertArrayEquals(new ElementType[]{ElementType.METHOD}, target.value());
    }

    @Test
    void reasonValueIsRetrievedViaReflection() throws NoSuchMethodException {
        Method m = Demo.class.getDeclaredMethod("disabled");
        DisabledEndpoint annotation = m.getAnnotation(DisabledEndpoint.class);
        assertNotNull(annotation);
        assertEquals("maintenance", annotation.reason());
    }
}
