/**
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
package nl.datasteel.crudcraft.runtime.util.meta;

import nl.datasteel.crudcraft.runtime.util.RelationshipUtilsTest.PlainEntity;

public final class SampleEntityRelationshipMeta {
    public static int fixCount = 0;
    public static int clearCount = 0;
    private SampleEntityRelationshipMeta() {}
    public static void fix(PlainEntity entity) { fixCount++; }
    public static void clear(PlainEntity entity) { clearCount++; }
}
