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

package nl.datasteel.crudcraft.codegen.annotations.fields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class SearchableTest {

    @Test
    void defaultsAreEmptyAndDepthOne() throws Exception {
        Method ops = Searchable.class.getMethod("operators");
        assertEquals(0, ((Object[]) ops.getDefaultValue()).length);
        Method depth = Searchable.class.getMethod("depth");
        assertEquals(1, depth.getDefaultValue());
    }

    @Test
    void metaAnnotationsConfiguredProperly() {
        Target target = Searchable.class.getAnnotation(Target.class);
        assertArrayEquals(new ElementType[] {ElementType.FIELD, ElementType.TYPE}, target.value());
        Retention retention = Searchable.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.CLASS, retention.value());
    }
}
