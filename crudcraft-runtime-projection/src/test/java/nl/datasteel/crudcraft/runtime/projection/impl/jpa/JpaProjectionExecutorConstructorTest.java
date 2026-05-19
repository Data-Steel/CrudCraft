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

package nl.datasteel.crudcraft.runtime.projection.impl.jpa;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;


class JpaProjectionExecutorConstructorTest {

    @Test
    void threeArgConstructorUsesNoopFieldSecurityAdapter() throws Exception {
        JpaProjectionExecutor executor =
                new JpaProjectionExecutor(
                        mock(EntityManager.class),
                        mock(CriteriaProjectionBuilder.class),
                        mock(ProjectionMetadataRegistry.class));

        assertSame(FieldSecurityAdapter.NOOP, readSecurityAdapter(executor));
    }

    @Test
    void fourArgConstructorReplacesNullFieldSecurityAdapterWithNoop() throws Exception {
        JpaProjectionExecutor executor =
                new JpaProjectionExecutor(
                        mock(EntityManager.class),
                        mock(CriteriaProjectionBuilder.class),
                        mock(ProjectionMetadataRegistry.class),
                        null);

        assertSame(FieldSecurityAdapter.NOOP, readSecurityAdapter(executor));
    }

    @Test
    void fourArgConstructorKeepsCustomFieldSecurityAdapter() throws Exception {
        FieldSecurityAdapter adapter = new FieldSecurityAdapter() {};
        JpaProjectionExecutor executor =
                new JpaProjectionExecutor(
                        mock(EntityManager.class),
                        mock(CriteriaProjectionBuilder.class),
                        mock(ProjectionMetadataRegistry.class),
                        adapter);

        assertSame(adapter, readSecurityAdapter(executor));
    }

    private static FieldSecurityAdapter readSecurityAdapter(JpaProjectionExecutor executor)
            throws Exception {
        Field field = JpaProjectionExecutor.class.getDeclaredField("fieldSecurityAdapter");
        field.setAccessible(true);
        return (FieldSecurityAdapter) field.get(executor);
    }
}
