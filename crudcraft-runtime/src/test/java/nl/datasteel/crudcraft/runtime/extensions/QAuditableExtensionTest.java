/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.runtime.extensions;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QAuditableExtensionTest {

    @Test
    void constructorsInitializeMetadata() {
        QAuditableExtension q = new QAuditableExtension("a");
        assertEquals("a", q.getMetadata().getName());
        assertNotNull(q.createdAt);
        assertNotNull(q.updatedAt);

        QAuditableExtension fromPath = new QAuditableExtension(q);
        assertEquals(q.getType(), fromPath.getType());

        PathMetadata md = PathMetadataFactory.forVariable("x");
        QAuditableExtension fromMd = new QAuditableExtension(md);
        assertEquals("x", fromMd.getMetadata().getName());
    }

    @Test
    void staticInstanceAccessible() {
        assertNotNull(QAuditableExtension.auditableExtension);
    }
}
