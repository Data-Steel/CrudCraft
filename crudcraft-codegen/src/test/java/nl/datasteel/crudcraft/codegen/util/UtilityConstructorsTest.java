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

package nl.datasteel.crudcraft.codegen.util;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


class UtilityConstructorsTest {

    @Test
    void privateConstructorsAreCoveredForStrictMutationLineCoverage() throws Exception {
        assertConstructs(CollectionTypes.class);
        assertConstructs(FilerUtils.class);
        assertConstructs(Pluralizer.class);
        assertConstructs(StubGeneratorUtil.class);
        assertConstructs(TypeUtils.class);
    }

    private static void assertConstructs(Class<?> type) throws Exception {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}
