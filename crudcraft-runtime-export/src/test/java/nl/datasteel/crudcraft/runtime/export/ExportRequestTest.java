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

package nl.datasteel.crudcraft.runtime.export;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ExportRequestTest {

    @Test
    void includeAndExcludeSetsAreDefensivelyCopiedAndUnmodifiable() {
        Set<String> includes = Set.of("name");
        Set<String> excludes = Set.of("secret");
        ExportRequest request =
                new ExportRequest(includes, excludes, 2, ExportRequest.ExportMode.ENTITY);

        assertEquals(includes, request.getIncludeFields());
        assertEquals(excludes, request.getExcludeFields());
        assertThrows(
                UnsupportedOperationException.class, () -> request.getIncludeFields().add("x"));
        assertThrows(
                UnsupportedOperationException.class, () -> request.getExcludeFields().add("x"));
    }

    @Test
    void settersAcceptNullAndRestoreEmptyViews() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("a"));
        request.setExcludeFields(Set.of("b"));

        request.setIncludeFields(null);
        request.setExcludeFields(null);

        assertEquals(Set.of(), request.getIncludeFields());
        assertEquals(Set.of(), request.getExcludeFields());
    }

    @Test
    void constructorAcceptsNullSets() {
        ExportRequest request = new ExportRequest(null, null, null, null);

        assertEquals(Set.of(), request.getIncludeFields());
        assertEquals(Set.of(), request.getExcludeFields());
    }

    @Test
    void requireSupportedFormatNormalizesAndRejectsNullBlankAndUnknownFormats() {
        assertEquals("json", ExportRequest.requireSupportedFormat("  JsOn  "));

        assertThrows(
                IllegalArgumentException.class, () -> ExportRequest.requireSupportedFormat(null));
        assertThrows(
                IllegalArgumentException.class, () -> ExportRequest.requireSupportedFormat(" "));
        assertThrows(
                IllegalArgumentException.class, () -> ExportRequest.requireSupportedFormat("xml"));
    }

    @Test
    void shouldIncludeFieldHonorsParentInclusionAndExclusionRules() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("author"));
        request.setExcludeFields(Set.of("author.secret"));

        assertTrue(request.shouldIncludeField("author.name"));
        assertFalse(request.shouldIncludeField("author.secret"));
        assertFalse(request.shouldIncludeField("author.secret.value"));
        assertFalse(request.shouldIncludeField("title"));
    }

    @Test
    void shouldIncludeFieldDefaultsToIncludeAllWhenNoIncludesConfigured() {
        ExportRequest request = new ExportRequest();
        request.setExcludeFields(Set.of("a"));

        assertTrue(request.shouldIncludeField("name"));
        assertFalse(request.shouldIncludeField("a"));
    }

    @Test
    void hasIncludedDescendantsDetectsNestedInclusions() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("author.name", "author.email", "title"));

        assertTrue(request.hasIncludedDescendants("author"));
        assertFalse(request.hasIncludedDescendants("title"));
        assertFalse(request.hasIncludedDescendants("missing"));
    }

    @Test
    void effectiveDepthAndModeHaveSafeDefaults() {
        ExportRequest request = new ExportRequest();

        assertNull(request.getMaxDepth());
        assertNull(request.getExportMode());
        assertEquals(5, request.getEffectiveMaxDepth());
        assertEquals(ExportRequest.ExportMode.DTO, request.getEffectiveExportMode());
        assertFalse(request.isEntityModeEnabled());

        request.setMaxDepth(-3);
        request.setExportMode(ExportRequest.ExportMode.ENTITY);

        assertEquals(ExportRequest.ExportMode.ENTITY, request.getExportMode());
        assertEquals(0, request.getEffectiveMaxDepth());
        assertTrue(request.isEntityModeEnabled());
    }

    @Test
    void parentIncludeExcludeLoopsHandleDeepPaths() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("author.profile"));
        request.setExcludeFields(Set.of("author.profile.secret"));

        assertTrue(request.shouldIncludeField("author.profile.name"));
        assertFalse(request.shouldIncludeField("author.profile.secret.token"));
    }

    @Test
    void emptyIncludeSetBehavesLikeIncludeAllAndNoDescendants() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of());

        assertTrue(request.shouldIncludeField("any.field"));
        assertFalse(request.hasIncludedDescendants("any"));
    }

    @Test
    void includeAndExcludeFieldsAreSanitizedAndNullSafe() {
        ExportRequest request = new ExportRequest();
        Set<String> includes = new HashSet<>();
        includes.add(" author.name ");
        includes.add("");
        includes.add("   ");
        includes.add(null);
        request.setIncludeFields(includes);

        Set<String> excludes = new HashSet<>();
        excludes.add(" secret ");
        excludes.add("");
        excludes.add(null);
        request.setExcludeFields(excludes);

        assertEquals(Set.of("author.name"), request.getIncludeFields());
        assertEquals(Set.of("secret"), request.getExcludeFields());
        assertFalse(request.shouldIncludeField(null));
        assertFalse(request.shouldIncludeField("   "));
        assertFalse(request.hasIncludedDescendants(null));
        assertFalse(request.hasIncludedDescendants(" "));
    }

    @Test
    void includeAndExcludeFieldsRejectMalformedPaths() {
        ExportRequest request = new ExportRequest();

        assertThrows(IllegalArgumentException.class, () -> request.setIncludeFields(Set.of("a..b")));
        assertThrows(IllegalArgumentException.class, () -> request.setIncludeFields(Set.of(".a")));
        assertThrows(IllegalArgumentException.class, () -> request.setExcludeFields(Set.of("a.")));
        assertThrows(IllegalArgumentException.class, () -> request.setExcludeFields(Set.of("a-b")));
    }

    @Test
    void parentChecksDoNotTreatExactFieldAsParentAndKeepNullInternalState() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("author.name"));
        request.setExcludeFields(Set.of("author.name"));

        Method isParentExcluded =
                ExportRequest.class.getDeclaredMethod("isParentExcluded", String.class);
        Method isParentIncluded =
                ExportRequest.class.getDeclaredMethod("isParentIncluded", String.class);
        isParentExcluded.setAccessible(true);
        isParentIncluded.setAccessible(true);

        assertFalse((Boolean) isParentExcluded.invoke(request, "author.name"));
        assertFalse((Boolean) isParentIncluded.invoke(request, "author.name"));

        request.setIncludeFields(null);
        java.lang.reflect.Field includeField =
                ExportRequest.class.getDeclaredField("includeFields");
        includeField.setAccessible(true);
        assertNull(includeField.get(request));
    }

    @Test
    void privateParentChecksReturnFalseWhenSetsAreNull() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(null);
        request.setExcludeFields(null);

        Method isParentExcluded =
                ExportRequest.class.getDeclaredMethod("isParentExcluded", String.class);
        Method isParentIncluded =
                ExportRequest.class.getDeclaredMethod("isParentIncluded", String.class);
        isParentExcluded.setAccessible(true);
        isParentIncluded.setAccessible(true);

        assertFalse((Boolean) isParentExcluded.invoke(request, "a.b"));
        assertFalse((Boolean) isParentIncluded.invoke(request, "a.b"));
    }
}
