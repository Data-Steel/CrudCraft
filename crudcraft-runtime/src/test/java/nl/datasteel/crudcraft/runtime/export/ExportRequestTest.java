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
package nl.datasteel.crudcraft.runtime.export;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ExportRequestTest {

    @Test
    void shouldIncludeFieldWhenNoFiltersSpecified() {
        ExportRequest request = new ExportRequest();
        assertTrue(request.shouldIncludeField("name"));
        assertTrue(request.shouldIncludeField("author.name"));
    }

    @Test
    void shouldExcludeFieldWhenInExcludeList() {
        ExportRequest request = new ExportRequest();
        request.setExcludeFields(Set.of("passwordHash", "author.email"));
        
        assertFalse(request.shouldIncludeField("passwordHash"));
        assertFalse(request.shouldIncludeField("author.email"));
        assertTrue(request.shouldIncludeField("name"));
        assertTrue(request.shouldIncludeField("author.name"));
    }

    @Test
    void shouldIncludeOnlyFieldsInIncludeList() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("name", "author"));
        
        assertTrue(request.shouldIncludeField("name"));
        assertFalse(request.shouldIncludeField("description"));
    }

    @Test
    void shouldIncludeNestedFieldWhenParentIncluded() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("author"));
        
        assertTrue(request.shouldIncludeField("author.name"));
        assertTrue(request.shouldIncludeField("author.email"));
        assertFalse(request.shouldIncludeField("title"));
    }

    @Test
    void exclusionTakesPrecedenceOverInclusion() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("author"));
        request.setExcludeFields(Set.of("author.email"));
        
        assertTrue(request.shouldIncludeField("author.name"));
        assertFalse(request.shouldIncludeField("author.email"));
    }

    @Test
    void getEffectiveMaxDepthReturnsDefaultWhenNotSet() {
        ExportRequest request = new ExportRequest();
        assertEquals(1, request.getEffectiveMaxDepth());
    }

    @Test
    void getEffectiveMaxDepthReturnsSetValue() {
        ExportRequest request = new ExportRequest();
        request.setMaxDepth(3);
        assertEquals(3, request.getEffectiveMaxDepth());
    }

    @Test
    void isEntityModeEnabledReturnsFalseByDefault() {
        ExportRequest request = new ExportRequest();
        assertFalse(request.isEntityModeEnabled());
    }

    @Test
    void isEntityModeEnabledReturnsTrueWhenSet() {
        ExportRequest request = new ExportRequest();
        request.setExportMode(ExportRequest.ExportMode.ENTITY);
        assertTrue(request.isEntityModeEnabled());
    }
    
    @Test
    void hasIncludedDescendantsReturnsTrueWhenDescendantIncluded() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("author.name", "author.email"));
        
        assertTrue(request.hasIncludedDescendants("author"));
        assertFalse(request.hasIncludedDescendants("author.name"));
        assertFalse(request.hasIncludedDescendants("title"));
    }
    
    @Test
    void hasIncludedDescendantsReturnsFalseWhenNoIncludeFields() {
        ExportRequest request = new ExportRequest();
        
        assertFalse(request.hasIncludedDescendants("author"));
        assertFalse(request.hasIncludedDescendants("title"));
    }
    
    @Test
    void hasIncludedDescendantsReturnsFalseWhenNoDescendantsIncluded() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("title", "author"));
        
        assertFalse(request.hasIncludedDescendants("author"));
        assertFalse(request.hasIncludedDescendants("title"));
    }
}
