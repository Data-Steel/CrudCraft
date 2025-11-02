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
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ProjectionFieldTest {

    static class Sample {
        @ProjectionField("user.name")
        String name;
    }

    @Test
    void annotationValueReadCorrectly() throws Exception {
        Field f = Sample.class.getDeclaredField("name");
        ProjectionField pf = f.getAnnotation(ProjectionField.class);
        assertNotNull(pf);
        assertEquals("user.name", pf.value());
    }

    @Test
    void metaAnnotationsConfiguredProperly() {
        Target target = ProjectionField.class.getAnnotation(Target.class);
        assertArrayEquals(new ElementType[]{ElementType.FIELD}, target.value());
        Retention retention = ProjectionField.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }
}

