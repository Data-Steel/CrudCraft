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
package nl.datasteel.crudcraft.annotations;

import java.util.EnumSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CrudEndpointTest {

    @Test
    void hasExpectedNumberOfEndpoints() {
        assertEquals(18, CrudEndpoint.values().length);
    }

    @Test
    void containsAllDefinedEndpoints() {
        Set<CrudEndpoint> expected = EnumSet.of(
                CrudEndpoint.GET_ALL,
                CrudEndpoint.GET_ALL_REF,
                CrudEndpoint.GET_ONE,
                CrudEndpoint.POST,
                CrudEndpoint.PUT,
                CrudEndpoint.PATCH,
                CrudEndpoint.DELETE,
                CrudEndpoint.BULK_CREATE,
                CrudEndpoint.BULK_UPDATE,
                CrudEndpoint.BULK_PATCH,
                CrudEndpoint.BULK_UPSERT,
                CrudEndpoint.BULK_DELETE,
                CrudEndpoint.FIND_BY_IDS,
                CrudEndpoint.EXISTS,
                CrudEndpoint.COUNT,
                CrudEndpoint.SEARCH,
                CrudEndpoint.VALIDATE,
                CrudEndpoint.EXPORT
        );
        assertEquals(expected, EnumSet.allOf(CrudEndpoint.class));
    }
}
