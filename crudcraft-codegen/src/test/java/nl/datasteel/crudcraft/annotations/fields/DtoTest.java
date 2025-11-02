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
package nl.datasteel.crudcraft.annotations.fields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class DtoTest {

    @Test
    void defaultValuesAreFalseAndEmpty() throws Exception {
        Method ref = Dto.class.getMethod("ref");
        assertEquals(false, ref.getDefaultValue());
        Method value = Dto.class.getMethod("value");
        assertArrayEquals(new String[0], (Object[]) value.getDefaultValue());
    }

    @Test
    void metaAnnotationsConfiguredProperly() {
        Target target = Dto.class.getAnnotation(Target.class);
        assertArrayEquals(new ElementType[]{ElementType.FIELD}, target.value());
        Retention retention = Dto.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.CLASS, retention.value());
    }
}

