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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class RowSecurityTest {

    static class SampleHandler implements RowSecurityHandler<String> {
        @Override
        public Specification<String> rowFilter() {
            return (root, query, cb) -> null;
        }
    }

    @RowSecurity(handlers = {SampleHandler.class})
    static class SecuredEntity {}

    @Test
    void annotationStoresHandlers() {
        RowSecurity rs = SecuredEntity.class.getAnnotation(RowSecurity.class);
        assertNotNull(rs);
        assertArrayEquals(new Class[]{SampleHandler.class}, rs.handlers());
    }

    @Test
    void metaAnnotationsConfiguredProperly() {
        Target target = RowSecurity.class.getAnnotation(Target.class);
        assertArrayEquals(new ElementType[]{ElementType.TYPE}, target.value());
        Retention retention = RowSecurity.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }
}
