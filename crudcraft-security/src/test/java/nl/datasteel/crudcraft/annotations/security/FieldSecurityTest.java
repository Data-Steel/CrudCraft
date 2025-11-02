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
package nl.datasteel.crudcraft.annotations.security;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class FieldSecurityTest {

    static class DefaultField {
        @FieldSecurity
        String value;
    }

    static class CustomField {
        @FieldSecurity(readRoles = {"USER"}, writeRoles = {"ADMIN"}, writePolicy = WritePolicy.FAIL_ON_DENIED)
        String value;
    }

    @Test
    void defaultsAreAllAndSkipOnDenied() throws Exception {
        Field field = DefaultField.class.getDeclaredField("value");
        FieldSecurity fs = field.getAnnotation(FieldSecurity.class);
        assertArrayEquals(new String[]{"ALL"}, fs.readRoles());
        assertArrayEquals(new String[]{"ALL"}, fs.writeRoles());
        assertEquals(WritePolicy.SKIP_ON_DENIED, fs.writePolicy());
    }

    @Test
    void customValuesOverrideDefaults() throws Exception {
        Field field = CustomField.class.getDeclaredField("value");
        FieldSecurity fs = field.getAnnotation(FieldSecurity.class);
        assertArrayEquals(new String[]{"USER"}, fs.readRoles());
        assertArrayEquals(new String[]{"ADMIN"}, fs.writeRoles());
        assertEquals(WritePolicy.FAIL_ON_DENIED, fs.writePolicy());
    }

    @Test
    void metaAnnotationsConfiguredProperly() {
        Target target = FieldSecurity.class.getAnnotation(Target.class);
        assertArrayEquals(new ElementType[]{ElementType.FIELD}, target.value());
        Retention retention = FieldSecurity.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }
}
