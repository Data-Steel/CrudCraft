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
package nl.datasteel.crudcraft.codegen.writer.controller.endpoints;

import com.squareup.javapoet.AnnotationSpec;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.TestModelDescriptorFactory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EndpointSpecProviderTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();

    private void assertEndpoint(EndpointSpecProvider provider,
                                CrudEndpoint endpoint,
                                String methodName,
                                com.squareup.javapoet.ClassName mapping,
                                String path) {
        assertEquals(endpoint, provider.endpoint());
        EndpointSpec spec = provider.create(model);
        assertEquals(methodName, spec.methodName());
        AnnotationSpec ann = spec.mapping().apply(model);
        assertEquals(mapping, ann.type);
        if (path == null) {
            assertFalse(ann.members.containsKey("value"));
        } else {
            assertEquals("\"" + path + "\"", ann.members.get("value").get(0).toString());
        }
    }

    @Test void countEndpoint() {
        assertEndpoint(new CountEndpoint(), CrudEndpoint.COUNT, "count", EndpointSupport.GET_MAPPING, "/count");
    }

    @Test void bulkCreateEndpoint() {
        assertEndpoint(new BulkCreateEndpoint(), CrudEndpoint.BULK_CREATE, "createAll", EndpointSupport.POST_MAPPING, "/batch");
    }

    @Test void bulkUpdateEndpoint() {
        assertEndpoint(new BulkUpdateEndpoint(), CrudEndpoint.BULK_UPDATE, "updateAll", EndpointSupport.PUT_MAPPING, "/batch");
    }

    @Test void exportEndpoint() {
        assertEndpoint(new ExportEndpoint(), CrudEndpoint.EXPORT, "export", EndpointSupport.GET_MAPPING, "/export");
    }

    @Test void deleteEndpoint() {
        assertEndpoint(new DeleteEndpoint(), CrudEndpoint.DELETE, "delete", EndpointSupport.DELETE_MAPPING, "/{id}");
    }

    @Test void bulkPatchEndpoint() {
        assertEndpoint(new BulkPatchEndpoint(), CrudEndpoint.BULK_PATCH, "patchAll", EndpointSupport.PATCH_MAPPING, "/batch");
    }

    @Test void existsEndpoint() {
        assertEndpoint(new ExistsEndpoint(), CrudEndpoint.EXISTS, "exists", EndpointSupport.REQUEST_MAPPING, "/exists/{id}");
    }

    @Test void bulkDeleteEndpoint() {
        assertEndpoint(new BulkDeleteEndpoint(), CrudEndpoint.BULK_DELETE, "deleteAllByIds", EndpointSupport.DELETE_MAPPING, "/batch/delete");
    }

    @Test void getOneEndpoint() {
        assertEndpoint(new GetOneEndpoint(), CrudEndpoint.GET_ONE, "getById", EndpointSupport.GET_MAPPING, "/{id}");
    }

    @Test void getAllEndpoint() {
        assertEndpoint(new GetAllEndpoint(), CrudEndpoint.GET_ALL, "getAll", EndpointSupport.GET_MAPPING, null);
    }

    @Test void getAllRefEndpoint() {
        assertEndpoint(new GetAllRefEndpoint(), CrudEndpoint.GET_ALL_REF, "getAllRef", EndpointSupport.GET_MAPPING, "/ref");
    }

    @Test void validateEndpoint() {
        assertEndpoint(new ValidateEndpoint(), CrudEndpoint.VALIDATE, "validate", EndpointSupport.POST_MAPPING, "/validate");
    }

    @Test void patchEndpoint() {
        assertEndpoint(new PatchEndpoint(), CrudEndpoint.PATCH, "patch", EndpointSupport.PATCH_MAPPING, "/{id}");
    }

    @Test void searchEndpoint() {
        assertEndpoint(new SearchEndpoint(), CrudEndpoint.SEARCH, "search", EndpointSupport.GET_MAPPING, "/search");
    }

    @Test void createEndpoint() {
        assertEndpoint(new CreateEndpoint(), CrudEndpoint.POST, "create", EndpointSupport.POST_MAPPING, null);
    }

    @Test void bulkUpsertEndpoint() {
        assertEndpoint(new BulkUpsertEndpoint(), CrudEndpoint.BULK_UPSERT, "upsertAll", EndpointSupport.POST_MAPPING, "/batch/upsert");
    }

    @Test void bulkFindByIdsEndpoint() {
        assertEndpoint(new FindByIdsEndpoint(), CrudEndpoint.FIND_BY_IDS, "findByIds", EndpointSupport.POST_MAPPING, "/batch/ids");
    }

    @Test void updateEndpoint() {
        assertEndpoint(new UpdateEndpoint(), CrudEndpoint.PUT, "update", EndpointSupport.PUT_MAPPING, "/{id}");
    }

    @Test
    void allProvidersRejectNullModelDescriptor() {
        var providers = java.util.List.of(
                new CountEndpoint(),
                new BulkCreateEndpoint(),
                new BulkUpdateEndpoint(),
                new ExportEndpoint(),
                new DeleteEndpoint(),
                new BulkPatchEndpoint(),
                new ExistsEndpoint(),
                new BulkDeleteEndpoint(),
                new GetOneEndpoint(),
                new GetAllEndpoint(),
                new GetAllRefEndpoint(),
                new ValidateEndpoint(),
                new PatchEndpoint(),
                new SearchEndpoint(),
                new CreateEndpoint(),
                new BulkUpsertEndpoint(),
                new FindByIdsEndpoint(),
                new UpdateEndpoint());
        providers.forEach(p -> assertThrows(NullPointerException.class, () -> p.create(null)));
    }
}
