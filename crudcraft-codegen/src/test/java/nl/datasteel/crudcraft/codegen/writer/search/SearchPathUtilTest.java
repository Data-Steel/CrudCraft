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

package nl.datasteel.crudcraft.codegen.writer.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class SearchPathUtilTest {

    @Test
    void toPropertyConvertsDottedPaths() {
        assertEquals("name", SearchFieldCollector.toProperty("name"));
        assertEquals("parentChild", SearchFieldCollector.toProperty("parent.child"));
        assertEquals("aBCEf", SearchFieldCollector.toProperty("a.b.c.ef"));
    }

    @Test
    void buildPathBuildsJoinAndGetChain() {
        assertEquals("root.get(\"id\")", SearchFieldCollector.buildCriteriaPath("id"));
        assertEquals(
                "root.join(\"parent\").get(\"child\")",
                SearchFieldCollector.buildCriteriaPath("parent.child"));
        assertEquals(
                "root.join(\"a\").join(\"b\").get(\"c\")",
                SearchFieldCollector.buildCriteriaPath("a.b.c"));
    }
}
