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

package nl.datasteel.crudcraft.runtime.extensions.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import nl.datasteel.crudcraft.runtime.util.clearmissing.ClearMissingMethodEntity;
import nl.datasteel.crudcraft.runtime.util.fixthrowing.FixThrowingEntity;
import nl.datasteel.crudcraft.runtime.util.hiddenaccess.HiddenAccessEntity;
import nl.datasteel.crudcraft.runtime.util.hiddenaccess.meta.HiddenAccessMetaFixtures;
import nl.datasteel.crudcraft.runtime.util.missingmethod.MissingMethodEntity;
import nl.datasteel.crudcraft.runtime.util.support.SupportedEntity;
import nl.datasteel.crudcraft.runtime.util.support.meta.SupportedEntityRelationshipMeta;
import nl.datasteel.crudcraft.runtime.util.throwing.ThrowingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SuppressWarnings({"deprecation", "removal"})
class RelationshipUtilsTest {

    @AfterEach
    void resetMetaFlags() {
        SupportedEntityRelationshipMeta.reset();
    }

    @Test
    void utilityConstructorThrows() throws Exception {
        Constructor<RelationshipUtils> constructor =
                RelationshipUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown =
                assertThrows(InvocationTargetException.class, constructor::newInstance);
        IllegalStateException cause =
                assertInstanceOf(IllegalStateException.class, thrown.getCause());
        assertEquals("Utility class should not be instantiated", cause.getMessage());
    }

    @Test
    void fixBidirectionalThrowsWhenMetaClassIsMissing() {
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> RelationshipUtils.fixBidirectional(new NoMetaEntity()));

        assertTrue(thrown.getMessage().contains("Generated relationship metadata not found"));
        assertInstanceOf(ClassNotFoundException.class, thrown.getCause());
    }

    @Test
    void clearBidirectionalThrowsWhenMetaClassIsMissing() {
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> RelationshipUtils.clearBidirectional(new NoMetaEntity()));

        assertTrue(thrown.getMessage().contains("Generated relationship metadata not found"));
        assertInstanceOf(ClassNotFoundException.class, thrown.getCause());
    }

    @Test
    void fixBidirectionalThrowsWhenEntityIsNull() {
        NullPointerException thrown =
                assertThrows(
                        NullPointerException.class, () -> RelationshipUtils.fixBidirectional(null));
        assertTrue(thrown.getMessage().contains("Entity must not be null"));
    }

    @Test
    void clearBidirectionalThrowsWhenEntityIsNull() {
        NullPointerException thrown =
                assertThrows(
                        NullPointerException.class,
                        () -> RelationshipUtils.clearBidirectional(null));
        assertTrue(thrown.getMessage().contains("Entity must not be null"));
    }

    @Test
    void fixBidirectionalInvokesGeneratedMetaClass() {
        SupportedEntity entity = new SupportedEntity();

        RelationshipUtils.fixBidirectional(entity);

        assertTrue(SupportedEntityRelationshipMeta.fixCalled);
        assertSame(entity, SupportedEntityRelationshipMeta.lastFixedEntity);
    }

    @Test
    void clearBidirectionalInvokesGeneratedMetaClass() {
        SupportedEntity entity = new SupportedEntity();

        RelationshipUtils.clearBidirectional(entity);

        assertTrue(SupportedEntityRelationshipMeta.clearCalled);
        assertSame(entity, SupportedEntityRelationshipMeta.lastClearedEntity);
    }

    @Test
    void fixBidirectionalThrowsWhenFixMethodIsMissing() {
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> RelationshipUtils.fixBidirectional(new MissingMethodEntity()));

        assertTrue(thrown.getMessage().contains("Failed to fix bidirectional relationship"));
        assertInstanceOf(NoSuchMethodException.class, thrown.getCause());
    }

    @Test
    void clearBidirectionalThrowsWhenMetaInvocationFails() {
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> RelationshipUtils.clearBidirectional(new ThrowingEntity()));

        assertTrue(thrown.getMessage().contains("Failed to clear bidirectional relationship"));
        assertInstanceOf(InvocationTargetException.class, thrown.getCause());
    }

    @Test
    void clearBidirectionalThrowsWhenClearMethodIsMissing() {
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> RelationshipUtils.clearBidirectional(new ClearMissingMethodEntity()));

        assertTrue(thrown.getMessage().contains("Failed to clear bidirectional relationship"));
        assertInstanceOf(NoSuchMethodException.class, thrown.getCause());
    }

    @Test
    void fixBidirectionalThrowsWhenMetaInvocationFails() {
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> RelationshipUtils.fixBidirectional(new FixThrowingEntity()));

        assertTrue(thrown.getMessage().contains("Failed to fix bidirectional relationship"));
        assertInstanceOf(InvocationTargetException.class, thrown.getCause());
    }

    @Test
    void fixBidirectionalThrowsWhenMetaClassIsNotAccessible() {
        assertEquals(
                "nl.datasteel.crudcraft.runtime.util.hiddenaccess.meta"
                        + ".HiddenAccessEntityRelationshipMeta",
                HiddenAccessMetaFixtures.hiddenAccessEntityRelationshipMetaType().getName());
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> RelationshipUtils.fixBidirectional(new HiddenAccessEntity()));

        assertTrue(thrown.getMessage().contains("Failed to fix bidirectional relationship"));
        assertInstanceOf(IllegalAccessException.class, thrown.getCause());
    }

    @Test
    void clearBidirectionalThrowsWhenMetaClassIsNotAccessible() {
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> RelationshipUtils.clearBidirectional(new HiddenAccessEntity()));

        assertTrue(thrown.getMessage().contains("Failed to clear bidirectional relationship"));
        assertInstanceOf(IllegalAccessException.class, thrown.getCause());
    }

    private static final class NoMetaEntity {}
}
