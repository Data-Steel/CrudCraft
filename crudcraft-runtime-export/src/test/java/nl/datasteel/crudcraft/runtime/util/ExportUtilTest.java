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

package nl.datasteel.crudcraft.runtime.export.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ExportUtilTest {
    private static final ObjectMapper TEST_MAPPER = new ObjectMapper();

    @Test
    void utilityClassConstructorThrows() throws Exception {
        Constructor<ExportUtil> constructor = ExportUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException ex =
                assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertTrue(ex.getCause() instanceof IllegalStateException);
    }

    @Test
    void toCsvToJsonAndToXlsxProduceData() throws Exception {
        List<Map<String, Object>> rows =
                List.of(Map.of("name", "alice", "age", 30), Map.of("name", "bob", "age", 25));

        byte[] csv = ExportUtil.toCsv(rows);
        byte[] json = ExportUtil.toJson(rows);
        byte[] xlsx = ExportUtil.toXlsx(rows);

        String csvText = new String(csv, StandardCharsets.UTF_8);
        String jsonText = new String(json, StandardCharsets.UTF_8);

        assertTrue(csvText.contains("name"));
        assertTrue(csvText.contains("alice"));
        assertTrue(jsonText.contains("\"name\":\"alice\""));
        assertTrue(xlsx.length > 0);

        String sheet = xlsxSheetXml(xlsx);
        assertTrue(sheet.contains("<t>name</t>"));
        assertTrue(sheet.contains("<t>age</t>"));
    }

    @Test
    void toCsvReturnsEmptyForEmptyInput() {
        assertArrayEquals(new byte[0], ExportUtil.toCsv(List.of()));
    }

    @Test
    void toCsvPrefixesSpreadsheetFormulaInputs() {
        List<Map<String, Object>> rows =
                List.of(
                        Map.of("value", "=SUM(1,2)"),
                        Map.of("value", "+1"),
                        Map.of("value", "-1"),
                        Map.of("value", "@cmd"),
                        Map.of("value", "\tvalue"));

        String csv = new String(ExportUtil.toCsv(rows), StandardCharsets.UTF_8);

        assertTrue(csv.contains("'=SUM(1,2)"));
        assertTrue(csv.contains("'+1"));
        assertTrue(csv.contains("'-1"));
        assertTrue(csv.contains("'@cmd"));
        assertTrue(csv.contains("'\tvalue"));
    }

    @Test
    void csvAndXlsxUseUnionHeadersAcrossRows() throws Exception {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("first", "a");
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("second", "b");

        String csv = new String(ExportUtil.toCsv(List.of(first, second)), StandardCharsets.UTF_8);
        String sheet = xlsxSheetXml(ExportUtil.toXlsx(List.of(first, second)));

        assertTrue(csv.startsWith("first,second"));
        assertTrue(csv.contains("a,"));
        assertTrue(csv.contains(",b"));
        assertTrue(sheet.contains("<t>first</t>"));
        assertTrue(sheet.contains("<t>second</t>"));
    }

    @Test
    void streamCsvAppliesFilteringAndDepthRules() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("name", "nested", "tags"));
        request.setMaxDepth(0);
        List<Map<String, Object>> rows =
                List.of(
                        Map.of(
                                "name",
                                "alice",
                                "nested",
                                Map.of("child", "v"),
                                "tags",
                                List.of("x", "y")),
                        Map.of("name", "bob"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(rows.iterator(), out, request);
        String csv = out.toString(StandardCharsets.UTF_8);

        assertTrue(csv.contains("name"));
        assertTrue(csv.contains("nested"));
        assertTrue(csv.contains("tags"));
        assertTrue(csv.contains("alice"));
    }

    @Test
    void streamCsvUsesUnionHeadersAcrossRows() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("first", "a");
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("second", "b");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ExportUtil.streamCsv(List.of(first, second).iterator(), out);
        String csv = out.toString(StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("first,second"));
        assertTrue(csv.contains("a,"));
        assertTrue(csv.contains(",b"));
    }

    @Test
    void streamCsvUsesDeclaredHeadersWhenFieldsAreIncluded() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(new LinkedHashSet<>(List.of("first", "second")));
        request.setExcludeFields(Set.of("third"));
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("first", "a");
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("second", "b");
        second.put("third", "hidden");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ExportUtil.streamCsv(List.of(first, second).iterator(), out, request);
        String csv = out.toString(StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("first,second"));
        assertTrue(csv.contains("a,"));
        assertTrue(csv.contains(",b"));
        assertFalse(csv.contains("third"));
        assertFalse(csv.contains("hidden"));
    }

    @Test
    void streamCsvCoversNullEmptyAndMixedCollectionBranches() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("nullable", "emptyMap", "emptyList", "mixed", "top"));
        request.setMaxDepth(0);

        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("nullable", null);
        row.put("emptyMap", Map.of());
        row.put("emptyList", List.of());
        row.put("mixed", java.util.Arrays.asList(null, "value", Map.of("k", "v")));
        row.put("top", Map.of("child", "v"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(row).iterator(), out, request);
        String csv = out.toString(StandardCharsets.UTF_8);

        assertTrue(csv.contains("nullable"));
        assertTrue(csv.contains("emptyMap"));
        assertTrue(csv.contains("emptyList"));
        assertTrue(csv.contains("mixed"));
        assertTrue(csv.contains("top"));
        assertTrue(csv.contains("null"));
        assertTrue(csv.contains("value"));
        assertTrue(csv.contains("k"));
    }

    @Test
    void streamCsvWithoutRowsKeepsOutputEmpty() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.<Map<String, Object>>of().iterator(), out);

        assertEquals(0, out.size());
    }

    @Test
    void streamJsonKeepsNestedStructureAndFiltersByRequest() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("author.name", "tags"));
        request.setExcludeFields(Set.of("author.email"));

        List<Map<String, Object>> rows =
                List.of(
                        Map.of(
                                "title", "post",
                                "author", Map.of("name", "Ada", "email", "a@example.com"),
                                "tags", List.of("java", "spring")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamJson(rows.iterator(), out, request);
        String json = out.toString(StandardCharsets.UTF_8);

        assertTrue(json.contains("\"author\""));
        assertTrue(json.contains("\"name\":\"Ada\""));
        assertTrue(json.contains("\"tags\""));
        assertFalse(json.contains("email"));
        assertFalse(json.contains("title"));
    }

    @Test
    void streamJsonCoversNestedCollectionAndNullFilteringBranches() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(
                Set.of("parent.keep", "items.keep", "emptyItems", "nullField", "dropAll.keep"));
        request.setExcludeFields(Set.of("parent.drop", "items.drop"));

        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("parent", Map.of("keep", "ok", "drop", "x"));
        row.put(
                "items",
                List.of(Map.of("keep", "v1", "drop", "x"), Map.of("drop", "only-drop"), "scalar"));
        row.put("emptyItems", List.of());
        row.put("nullField", null);
        row.put("dropAll", Map.of("drop", "value"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamJson(List.of(row).iterator(), out, request);
        String json = out.toString(StandardCharsets.UTF_8);

        assertTrue(json.contains("\"keep\":\"ok\""));
        assertTrue(json.contains("\"emptyItems\":[]"));
        assertTrue(json.contains("\"nullField\":null"));
        assertFalse(json.contains("only-drop"));
        assertFalse(json.contains("\"scalar\""));
        assertFalse(json.contains("\"dropAll\""));
    }

    @Test
    void streamXlsxSupportsEmptyAndNonEmptyIterators() throws Exception {
        ByteArrayOutputStream emptyOut = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.<Map<String, Object>>of().iterator(), emptyOut);
        assertTrue(emptyOut.size() > 0);

        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.of(Map.of("name", "alice")).iterator(), dataOut);
        String sheet = xlsxSheetXml(dataOut.toByteArray());
        assertTrue(sheet.contains("<t>name</t>"));
        assertTrue(sheet.contains("<t>alice</t>"));
    }

    @Test
    void streamXlsxUsesDeclaredHeadersWhenFieldsAreIncluded() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(new LinkedHashSet<>(List.of("first", "second")));
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("first", "a");
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("second", "b");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ExportUtil.streamXlsx(List.of(first, second).iterator(), out, request);
        String sheet = xlsxSheetXml(out.toByteArray());

        assertTrue(sheet.contains("<t>first</t>"));
        assertTrue(sheet.contains("<t>second</t>"));
        assertTrue(sheet.contains("<t>a</t>"));
        assertTrue(sheet.contains("<t>b</t>"));
    }

    @Test
    void streamXlsxCoversNullCellBranch() throws Exception {
        Map<String, Object> first = new HashMap<>();
        first.put("name", "alice");
        Map<String, Object> second = new HashMap<>();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.of(first, second).iterator(), out);

        String sheet = xlsxSheetXml(out.toByteArray());
        assertTrue(sheet.contains("<c r=\"A3\" t=\"inlineStr\"><is><t></t></is></c>"));
    }

    @Test
    void streamXlsxUsesExcelColumnNamesBeyondZ() throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < 27; i++) {
            row.put("column" + i, "value" + i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.of(row).iterator(), out);

        String sheet = xlsxSheetXml(out.toByteArray());
        assertTrue(sheet.contains("<c r=\"Z1\" t=\"inlineStr\"><is><t>column25</t></is></c>"));
        assertTrue(sheet.contains("<c r=\"AA1\" t=\"inlineStr\"><is><t>column26</t></is></c>"));
        assertTrue(sheet.contains("<c r=\"AA2\" t=\"inlineStr\"><is><t>value26</t></is></c>"));
    }

    @Test
    void streamXlsxWritesRequiredWorkbookPackageEntries() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.of(Map.of("name", "alice")).iterator(), out);

        Set<String> entries = xlsxEntries(out.toByteArray());

        assertTrue(entries.contains("[Content_Types].xml"));
        assertTrue(entries.contains("_rels/.rels"));
        assertTrue(entries.contains("xl/workbook.xml"));
        assertTrue(entries.contains("xl/_rels/workbook.xml.rels"));
        assertTrue(entries.contains("xl/worksheets/sheet1.xml"));
    }

    @Test
    void streamJsonHonorsMaxDepthForNestedMapsAndCollections() {
        ExportRequest request = new ExportRequest();
        request.setMaxDepth(0);
        request.setIncludeFields(Set.of("parent.child", "items.child"));

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("parent", Map.of("child", "value"));
        row.put("items", List.of(Map.of("child", "value")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamJson(List.of(row).iterator(), out, request);

        assertEquals("[{}]", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void streamJsonStopsGrandchildrenAtConfiguredDepth() {
        ExportRequest request = new ExportRequest();
        request.setMaxDepth(1);
        request.setIncludeFields(Set.of("parent.child.leaf", "items.child.leaf"));

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("parent", Map.of("child", Map.of("leaf", "value")));
        row.put("items", List.of(Map.of("child", Map.of("leaf", "value"))));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamJson(List.of(row).iterator(), out, request);

        assertEquals("[{}]", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void toXlsxHandlesEmptyInputAndMissingValues() throws Exception {
        assertTrue(ExportUtil.toXlsx(List.of()).length > 0);

        Map<String, Object> first = new HashMap<>();
        first.put("name", "alice");
        Map<String, Object> second = new HashMap<>();

        byte[] bytes = ExportUtil.toXlsx(List.of(first, second));
        String sheet = xlsxSheetXml(bytes);
        assertTrue(sheet.contains("<t>alice</t>"));
        assertTrue(sheet.contains("<c r=\"A3\" t=\"inlineStr\"><is><t></t></is></c>"));
    }

    @Test
    void safeScalarPrivateMethodCoversEveryScalarKind() throws Exception {
        Method safeScalar = ExportUtil.class.getDeclaredMethod("safeScalar", Object.class);
        safeScalar.setAccessible(true);

        Map<String, Object> nestedMap = new LinkedHashMap<>();
        nestedMap.put("missing", null);
        nestedMap.put("inner", Map.of("flag", true));
        List<Object> mixedCollection =
                Arrays.asList(null, 4, false, TestState.ACTIVE, new PlainObject());

        assertEquals("null", safeScalar.invoke(null, new Object[] {null}));
        assertEquals("{missing=null, inner={flag=true}}", safeScalar.invoke(null, nestedMap));
        assertEquals(
                "[null, 4, false, ACTIVE, "
                        + "nl.datasteel.crudcraft.runtime.export.util.ExportUtilTest$PlainObject]",
                safeScalar.invoke(null, mixedCollection));
        assertEquals("7", safeScalar.invoke(null, 7));
        assertEquals("false", safeScalar.invoke(null, false));
        assertEquals("text", safeScalar.invoke(null, "text"));
        assertEquals("ACTIVE", safeScalar.invoke(null, TestState.ACTIVE));
        assertEquals(
                "nl.datasteel.crudcraft.runtime.export.util.ExportUtilTest$PlainObject",
                safeScalar.invoke(null, new PlainObject()));
    }

    @Test
    void flattenMapPrivateMethodCoversRecursiveAndFallbackPaths() throws Exception {
        Method flattenMap =
                ExportUtil.class.getDeclaredMethod(
                        "flattenMap",
                        String.class,
                        Object.class,
                        Map.class,
                        ExportRequest.class,
                        int.class);
        flattenMap.setAccessible(true);

        Map<String, Object> result = new HashMap<>();
        flattenMap.invoke(null, "scalar", "value", result, null, 0);
        assertEquals("value", result.get("scalar"));
        flattenMap.invoke(null, "nullNoFilter", null, result, null, 0);
        flattenMap.invoke(null, "emptyMapNoFilter", Map.of(), result, null, 0);
        flattenMap.invoke(null, "emptyListNoFilter", List.of(), result, null, 0);
        assertTrue(result.containsKey("nullNoFilter"));
        assertTrue(result.containsKey("emptyMapNoFilter"));
        assertTrue(result.containsKey("emptyListNoFilter"));

        ExportRequest excludeAll = new ExportRequest();
        excludeAll.setIncludeFields(Set.of("elsewhere"));
        ExportRequest includeNullAndEmpty = new ExportRequest();
        includeNullAndEmpty.setIncludeFields(
                Set.of("nullIncluded", "emptyMapIncluded", "emptyListIncluded"));
        flattenMap.invoke(null, "nullExcluded", null, result, excludeAll, 0);
        flattenMap.invoke(null, "nullIncluded", null, result, includeNullAndEmpty, 0);
        flattenMap.invoke(null, "emptyMapExcluded", Map.of(), result, excludeAll, 0);
        flattenMap.invoke(null, "emptyMapIncluded", Map.of(), result, includeNullAndEmpty, 0);
        flattenMap.invoke(null, "emptyListExcluded", List.of(), result, excludeAll, 0);
        flattenMap.invoke(null, "emptyListIncluded", List.of(), result, includeNullAndEmpty, 0);
        assertFalse(result.containsKey("nullExcluded"));
        assertFalse(result.containsKey("emptyMapExcluded"));
        assertFalse(result.containsKey("emptyListExcluded"));
        assertTrue(result.containsKey("nullIncluded"));
        assertTrue(result.containsKey("emptyMapIncluded"));
        assertTrue(result.containsKey("emptyListIncluded"));

        Map<String, Object> nested = new HashMap<>();
        nested.put("inner", Map.of("leaf", "ok"));
        flattenMap.invoke(null, "", Map.of("root", "top"), result, null, 0);
        flattenMap.invoke(null, "node", nested, result, null, 0);
        assertEquals("top", result.get("root"));
        assertEquals("ok", result.get("node.inner.leaf"));

        List<Object> mixed = new ArrayList<>();
        mixed.add(null);
        mixed.add(Map.of("x", "y"));
        mixed.add(42);
        flattenMap.invoke(null, "mixed", mixed, result, null, 0);
        assertTrue(String.valueOf(result.get("mixed")).contains("null"));
        assertTrue(String.valueOf(result.get("mixed")).contains("{x=y}"));
        assertTrue(String.valueOf(result.get("mixed")).contains("42"));

        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("node", "items"));
        request.setMaxDepth(0);

        Map<String, Object> failingMap = new HashMap<>();
        failingMap.put("boom", new ExplodingBean());
        flattenMap.invoke(null, "node", failingMap, result, request, 0);
        assertTrue(String.valueOf(result.get("node")).contains("boom"));

        List<Object> failingList = new ArrayList<>();
        failingList.add(new ExplodingBean());
        flattenMap.invoke(null, "items", failingList, result, request, 0);
        assertTrue(String.valueOf(result.get("items")).contains("ExplodingBean"));

        ExportRequest depthInclude = new ExportRequest();
        depthInclude.setIncludeFields(Set.of("list"));
        depthInclude.setMaxDepth(0);
        flattenMap.invoke(null, "list", List.of("a", "b"), result, depthInclude, 0);
        assertTrue(String.valueOf(result.get("list")).contains("[\"a\",\"b\"]"));

        ExportRequest depthExclude = new ExportRequest();
        depthExclude.setIncludeFields(Set.of("other"));
        depthExclude.setMaxDepth(0);
        flattenMap.invoke(null, "listExcluded", List.of("a"), result, depthExclude, 0);
        flattenMap.invoke(null, "nodeExcluded", Map.of("a", 1), result, depthExclude, 0);
        assertFalse(result.containsKey("listExcluded"));
        assertFalse(result.containsKey("nodeExcluded"));

        ExportRequest collectionFiltered = new ExportRequest();
        collectionFiltered.setIncludeFields(Set.of("other"));
        collectionFiltered.setMaxDepth(3);
        flattenMap.invoke(
                null, "collectionNoInclude", List.of("x", "y"), result, collectionFiltered, 0);
        assertFalse(result.containsKey("collectionNoInclude"));
        ExportRequest collectionIncluded = new ExportRequest();
        collectionIncluded.setIncludeFields(Set.of("collectionIncluded"));
        collectionIncluded.setMaxDepth(3);
        flattenMap.invoke(
                null, "collectionIncluded", List.of("x", "y"), result, collectionIncluded, 0);
        assertTrue(result.containsKey("collectionIncluded"));

        ExportRequest recurseWithRequest = new ExportRequest();
        recurseWithRequest.setIncludeFields(Set.of("branch.leaf"));
        recurseWithRequest.setMaxDepth(3);
        flattenMap.invoke(null, "branch", Map.of("leaf", "v"), result, recurseWithRequest, 0);
        assertEquals("v", result.get("branch.leaf"));

        ExportRequest depthSensitive = new ExportRequest();
        depthSensitive.setIncludeFields(Set.of("root.inner", "root.inner.leaf"));
        depthSensitive.setMaxDepth(1);
        flattenMap.invoke(
                null, "root", Map.of("inner", Map.of("leaf", "v")), result, depthSensitive, 0);
        assertTrue(result.containsKey("root.inner"));
        assertFalse(result.containsKey("root.inner.leaf"));

        ExportRequest scalarInclude = new ExportRequest();
        scalarInclude.setIncludeFields(Set.of("plain"));
        flattenMap.invoke(null, "plain", "value", result, scalarInclude, 0);
        assertEquals("value", result.get("plain"));
        ExportRequest scalarExclude = new ExportRequest();
        scalarExclude.setIncludeFields(Set.of("elsewhere"));
        flattenMap.invoke(null, "plainExcluded", "value", result, scalarExclude, 0);
        assertFalse(result.containsKey("plainExcluded"));
    }

    @Test
    void streamJsonSkipsFullyFilteredAndExcludedCollections() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(Set.of("objects.keep"));

        Map<String, Object> row = new HashMap<>();
        row.put("objects", List.of(Map.of("drop", "value")));
        row.put("emptyNoInclude", List.of());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamJson(List.of(row).iterator(), out, request);
        String json = out.toString(StandardCharsets.UTF_8);

        assertFalse(json.contains("objects"));
        assertFalse(json.contains("emptyNoInclude"));
    }

    @Test
    void streamJsonDescendantOnlyPathsDoNotIncludeNullEmptyOrScalarParents() {
        ExportRequest request = new ExportRequest();
        request.setIncludeFields(
                Set.of("nullParent.child", "emptyParent.child", "scalarParent.child"));

        Map<String, Object> row = new HashMap<>();
        row.put("nullParent", null);
        row.put("emptyParent", List.of());
        row.put("scalarParent", "value");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamJson(List.of(row).iterator(), out, request);
        String json = out.toString(StandardCharsets.UTF_8);

        assertFalse(json.contains("nullParent"));
        assertFalse(json.contains("emptyParent"));
        assertFalse(json.contains("scalarParent"));
    }

    @Test
    void streamJsonProducesParsableJsonArray() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamJson(List.of(Map.of("name", "a"), Map.of("name", "b")).iterator(), out);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parsed = TEST_MAPPER.readValue(out.toByteArray(), List.class);
        assertEquals(2, parsed.size());
        assertEquals("a", parsed.get(0).get("name"));
        assertEquals("b", parsed.get(1).get("name"));
    }

    @Test
    void streamCsvWritesRowsOnSeparateLines() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(Map.of("a", 1), Map.of("a", 2)).iterator(), out);

        String csv = out.toString(StandardCharsets.UTF_8);
        String[] lines = csv.split("\\R");
        assertTrue(lines.length >= 3);
    }

    @Test
    void streamOverloadsAndEdgeIteratorsCoverRemainingBranches() throws Exception {
        ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
        ExportUtil.streamJson(List.of(Map.of("k", "v")).iterator(), jsonOut);
        assertTrue(jsonOut.toString(StandardCharsets.UTF_8).contains("\"k\":\"v\""));

        Iterator<Map<String, Object>> emptyCsvIterator =
                new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public Map<String, Object> next() {
                        throw new java.util.NoSuchElementException();
                    }
                };
        ByteArrayOutputStream csvOut = new ByteArrayOutputStream();
        ExportUtil.streamCsv(emptyCsvIterator, csvOut);
        assertEquals(0, csvOut.size());

        Iterator<Map<String, Object>> emptyXlsxIterator =
                new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public Map<String, Object> next() {
                        throw new java.util.NoSuchElementException();
                    }
                };
        ByteArrayOutputStream xlsxOut = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(emptyXlsxIterator, xlsxOut);
        assertTrue(xlsxOut.size() > 0);
    }

    @Test
    void conversionMethodsWrapSerializationFailures() {
        assertThrows(RuntimeException.class, () -> ExportUtil.toCsv(List.of("not-a-map-dto")));
        assertThrows(RuntimeException.class, () -> ExportUtil.toJson(List.of(new ExplodingBean())));
        assertThrows(RuntimeException.class, () -> ExportUtil.toXlsx(List.of("not-a-map-dto")));
    }

    @Test
    void csvSafeValuePrefixesSpreadsheetFormulaInputs() throws Exception {
        Method csvSafeValue = ExportUtil.class.getDeclaredMethod("csvSafeValue", Object.class);
        csvSafeValue.setAccessible(true);

        assertEquals("'=SUM(1,2)", csvSafeValue.invoke(null, "=SUM(1,2)"));
        assertEquals("'+1", csvSafeValue.invoke(null, "+1"));
        assertEquals("'-1", csvSafeValue.invoke(null, "-1"));
        assertEquals("'@cmd", csvSafeValue.invoke(null, "@cmd"));
        assertEquals("'\tvalue", csvSafeValue.invoke(null, "\tvalue"));

        assertEquals("plain", csvSafeValue.invoke(null, "plain"));
        assertEquals("", csvSafeValue.invoke(null, ""));
        assertEquals(42, csvSafeValue.invoke(null, 42));
        assertEquals(null, csvSafeValue.invoke(null, new Object[] {null}));
    }

    @Test
    void columnNameHandlesRangeBoundaries() throws Exception {
        Method columnName = ExportUtil.class.getDeclaredMethod("columnName", int.class);
        columnName.setAccessible(true);

        assertEquals("A", columnName.invoke(null, 0));
        assertEquals("Z", columnName.invoke(null, 25));
        assertEquals("AA", columnName.invoke(null, 26));
        assertEquals("XFD", columnName.invoke(null, 16_383));

        InvocationTargetException negative =
                assertThrows(InvocationTargetException.class, () -> columnName.invoke(null, -1));
        assertTrue(negative.getCause() instanceof IllegalArgumentException);

        InvocationTargetException overflow =
                assertThrows(
                        InvocationTargetException.class, () -> columnName.invoke(null, 16_384));
        assertTrue(overflow.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void streamMethodsWrapOutputStreamFailures() {
        OutputStream brokenOut = new ThrowingOutputStream();
        Iterator<Map<String, Object>> iterator =
                List.<Map<String, Object>>of(Map.of("name", "alice")).iterator();

        assertThrows(RuntimeException.class, () -> ExportUtil.streamCsv(iterator, brokenOut));
        assertThrows(
                RuntimeException.class,
                () ->
                        ExportUtil.streamJson(
                                List.of(Map.of("name", "alice")).iterator(), brokenOut));
        assertThrows(
                RuntimeException.class,
                () ->
                        ExportUtil.streamXlsx(
                                List.of(Map.of("name", "alice")).iterator(), brokenOut));
    }

    @Test
    void streamMethodsWrapIteratorFailures() {
        Iterator<Object> brokenIterator =
                new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Object next() {
                        throw new IllegalStateException("broken");
                    }
                };

        OutputStream out = new ByteArrayOutputStream();

        assertThrows(RuntimeException.class, () -> ExportUtil.streamCsv(brokenIterator, out));
        assertThrows(RuntimeException.class, () -> ExportUtil.streamJson(brokenIterator, out));
        assertThrows(RuntimeException.class, () -> ExportUtil.streamXlsx(brokenIterator, out));
    }

    private static String xlsxSheetXml(byte[] xlsx) throws IOException {
        try (ZipInputStream zip =
                new ZipInputStream(new java.io.ByteArrayInputStream(xlsx), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("xl/worksheets/sheet1.xml".equals(entry.getName())) {
                    return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        throw new AssertionError("Missing xl/worksheets/sheet1.xml");
    }

    private static Set<String> xlsxEntries(byte[] xlsx) throws IOException {
        Set<String> entries = new HashSet<>();
        try (ZipInputStream zip =
                new ZipInputStream(new java.io.ByteArrayInputStream(xlsx), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }
        return entries;
    }

    private static final class ThrowingOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            throw new IOException("boom");
        }
    }

    private static final class ExplodingBean {
        public String getValue() {
            throw new IllegalStateException("boom");
        }
    }

    private enum TestState {
        ACTIVE
    }

    private static final class PlainObject {}
}
