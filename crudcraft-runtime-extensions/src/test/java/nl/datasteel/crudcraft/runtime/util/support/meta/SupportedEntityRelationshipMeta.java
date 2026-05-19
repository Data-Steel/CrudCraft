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

package nl.datasteel.crudcraft.runtime.util.support.meta;

import nl.datasteel.crudcraft.runtime.util.support.SupportedEntity;


public final class SupportedEntityRelationshipMeta {
    public static boolean fixCalled;
    public static boolean clearCalled;
    public static SupportedEntity lastFixedEntity;
    public static SupportedEntity lastClearedEntity;

    private SupportedEntityRelationshipMeta() {}

    public static void fix(SupportedEntity entity) {
        fixCalled = true;
        lastFixedEntity = entity;
    }

    public static void clear(SupportedEntity entity) {
        clearCalled = true;
        lastClearedEntity = entity;
    }

    public static void reset() {
        fixCalled = false;
        clearCalled = false;
        lastFixedEntity = null;
        lastClearedEntity = null;
    }
}
