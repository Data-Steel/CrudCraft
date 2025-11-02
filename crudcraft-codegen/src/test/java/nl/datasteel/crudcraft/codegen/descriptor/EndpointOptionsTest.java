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
package nl.datasteel.crudcraft.codegen.descriptor;

import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class EndpointOptionsTest {

    @Test
    void arraysAreCopiedAndEqualsWorks() {
        CrudEndpoint[] omit = {CrudEndpoint.DELETE};
        CrudEndpoint[] include = {CrudEndpoint.POST};
        EndpointOptions opts = new EndpointOptions(CrudTemplate.FULL, omit, include, CrudTemplate.class);
        omit[0] = CrudEndpoint.GET_ONE;
        include[0] = CrudEndpoint.GET_ALL;
        assertArrayEquals(new CrudEndpoint[]{CrudEndpoint.DELETE}, opts.getOmitEndpoints());
        assertArrayEquals(new CrudEndpoint[]{CrudEndpoint.POST}, opts.getIncludeEndpoints());
        EndpointOptions same = new EndpointOptions(CrudTemplate.FULL,
                new CrudEndpoint[]{CrudEndpoint.DELETE}, new CrudEndpoint[]{CrudEndpoint.POST}, CrudTemplate.class);
        assertEquals(opts, same);
        assertEquals(opts.hashCode(), same.hashCode());
    }
}
