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

// CHECKSTYLE.SUPPRESS: VariableDeclarationUsageDistance for +1000 lines

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


/** Utility methods for exporting DTO lists to various formats. */
public final class ExportUtil {

    private static final int XLSX_MAX_COLUMNS = 16_384;
    private static final int XLSX_MAX_COLUMN_NAME_LENGTH = 3;
    private static final int EXPORT_STREAM_BUFFER_SIZE_BYTES = 64 * 1024;

    /** The ObjectMapper instance used for JSON serialization. */
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Private constructor to prevent instantiation. This class provides static utility methods
     * only.
     */
    private ExportUtil() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    /**
     * Flattens a nested map structure into a single-level map with dot-separated keys. Nested
     * objects are flattened recursively, collections are converted to comma-separated strings, and
     * null values are preserved.
     *
     * @param prefix the prefix to prepend to keys (empty string for top level)
     * @param value the value to flatten
     * @param result the map to store flattened key-value pairs
     * @param exportRequest the export request for field filtering (null for no filtering)
     * @param depth the current nesting depth
     */
    @SuppressWarnings("unchecked")
    private static void flattenMap(
            String prefix,
            Object value,
            Map<String, Object> result,
            ExportRequest exportRequest,
            int depth) {
        if (value == null) {
            if (exportRequest == null || exportRequest.shouldIncludeField(prefix)) {
                result.put(prefix, null);
            }
            return;
        }

        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            if (map.isEmpty()) {
                if (exportRequest == null || exportRequest.shouldIncludeField(prefix)) {
                    result.put(prefix, null);
                }
                return;
            }
            // Check max depth
            if (exportRequest != null && depth >= exportRequest.getEffectiveMaxDepth()) {
                // Convert to JSON-like string representation for better readability
                if (exportRequest.shouldIncludeField(prefix)) {
                    try {
                        result.put(prefix, objectMapper.writeValueAsString(map));
                    } catch (JsonProcessingException e) {
                        result.put(prefix, safeRepresentation(map));
                    }
                }
                return;
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                flattenMap(newPrefix, entry.getValue(), result, exportRequest, depth + 1);
            }
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                if (exportRequest == null || exportRequest.shouldIncludeField(prefix)) {
                    result.put(prefix, null);
                }
            } else {
                // Check max depth
                if (exportRequest != null && depth >= exportRequest.getEffectiveMaxDepth()) {
                    // Convert to JSON-like string representation for better readability
                    if (exportRequest.shouldIncludeField(prefix)) {
                        try {
                            result.put(prefix, objectMapper.writeValueAsString(collection));
                        } catch (JsonProcessingException e) {
                            result.put(prefix, safeRepresentation(collection));
                        }
                    }
                    return;
                }
                // Convert collection to comma-separated string
                String collectionStr =
                        collection.stream()
                                .map(
                                        item -> {
                                            if (item == null) {
                                                return "null";
                                            } else if (item instanceof Map) {
                                                // For nested objects in collections, convert to
                                                // JSON-like string
                                                Map<String, Object> itemMap =
                                                        (Map<String, Object>) item;
                                                return itemMap.entrySet().stream()
                                                        .map(e -> e.getKey() + "=" + e.getValue())
                                                        .collect(
                                                                Collectors.joining(", ", "{", "}"));
                                            } else {
                                                return String.valueOf(item);
                                            }
                                        })
                                .collect(Collectors.joining(", "));
                if (exportRequest == null || exportRequest.shouldIncludeField(prefix)) {
                    result.put(prefix, collectionStr);
                }
            }
        } else {
            if (exportRequest == null || exportRequest.shouldIncludeField(prefix)) {
                result.put(prefix, value);
            }
        }
    }

    /**
     * Converts a DTO to a flattened map structure suitable for CSV/XLSX export. Nested objects are
     * flattened with dot-separated keys (e.g., "author.name", "author.email").
     *
     * @param dto the DTO to convert
     * @param exportRequest the export request for field filtering (null for no filtering)
     * @return a flattened map representation of the DTO
     */
    private static <R> Map<String, Object> toFlatMap(R dto, ExportRequest exportRequest) {
        Map<String, Object> originalMap =
                objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> flatMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
            flattenMap(entry.getKey(), entry.getValue(), flatMap, exportRequest, 0);
        }
        return flatMap;
    }

    private static <R> List<Map<String, Object>> flatRows(
            Iterator<R> dtos, @Nullable ExportRequest exportRequest) {
        List<Map<String, Object>> rows = new ArrayList<>();
        while (dtos.hasNext()) {
            rows.add(toFlatMap(dtos.next(), exportRequest));
        }
        return rows;
    }

    private static String[] headersFor(List<Map<String, Object>> rows) {
        LinkedHashSet<String> headers = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            headers.addAll(row.keySet());
        }
        return headers.toArray(String[]::new);
    }

    private static @Nullable String[] declaredHeadersFor(@Nullable ExportRequest exportRequest) {
        if (exportRequest == null || exportRequest.getIncludeFields().isEmpty()) {
            return null;
        }
        LinkedHashSet<String> headers = new LinkedHashSet<>();
        for (String field : exportRequest.getIncludeFields()) {
            if (exportRequest.shouldIncludeField(field)) {
                headers.add(field);
            }
        }
        return headers.toArray(String[]::new);
    }

    private static String safeRepresentation(Map<String, Object> map) {
        return map.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + safeScalar(entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String safeRepresentation(Collection<?> collection) {
        return collection.stream()
                .map(ExportUtil::safeScalar)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String safeScalar(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + safeScalar(entry.getValue()))
                    .collect(Collectors.joining(", ", "{", "}"));
        }
        if (value instanceof Collection<?> collection) {
            return safeRepresentation(collection);
        }
        Class<?> type = value.getClass();
        if (value instanceof Number
                || value instanceof Boolean
                || value instanceof CharSequence
                || value instanceof Enum<?>) {
            return Objects.toString(value);
        }
        return type.getName();
    }

    /**
     * Exports a list of DTOs to a CSV file.
     *
     * @param dtos the list of DTOs to export
     * @param <R> DTO type
     * @return a byte array containing the CSV data
     */
    public static <R> @NonNull byte[] toCsv(@NonNull List<R> dtos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (dtos.isEmpty()) {
                return out.toByteArray();
            }
            List<Map<String, Object>> maps = flatRows(dtos.iterator(), null);
            String[] headers = headersFor(maps);
            try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                    CSVPrinter printer = new CSVPrinter(writer, csvFormat(headers))) {
                for (Map<String, Object> row : maps) {
                    writeCsvRow(printer, headers, row);
                }
            }
            return out.toByteArray();
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to export CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Exports a list of DTOs to a JSON array.
     *
     * @param dtos the list of DTOs to export
     * @param <R> DTO type
     * @return a byte array containing the JSON data
     */
    public static <R> @NonNull byte[] toJson(@NonNull List<R> dtos) {
        try {
            return objectMapper.writeValueAsBytes(dtos);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to export JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Exports a list of DTOs to an XLSX file.
     *
     * @param dtos the list of DTOs to export
     * @param <R> DTO type
     * @return a byte array containing the XLSX file data
     */
    public static <R> @NonNull byte[] toXlsx(@NonNull List<R> dtos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            String[] headers = new String[0];
            if (!dtos.isEmpty()) {
                rows = flatRows(dtos.iterator(), null);
                headers = headersFor(rows);
            }
            writeXlsxDocument(rows, headers, out);
            return out.toByteArray();
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to export XLSX: " + e.getMessage(), e);
        }
    }

    /**
     * Streams DTOs to a CSV file.
     *
     * <p>When {@link ExportRequest#getIncludeFields()} declares a field schema, rows are flattened
     * and written incrementally. Without a declared schema, this method buffers flattened rows
     * first so it can preserve the historic union-header behavior.
     *
     * @param dtos the iterator of DTOs to export
     * @param out the output stream to write the CSV to
     * @param exportRequest the export request for field filtering (null for no filtering)
     * @param <R> DTO type
     */
    public static <R> void streamCsv(
            @NonNull Iterator<R> dtos,
            @NonNull OutputStream out,
            @Nullable ExportRequest exportRequest) {
        try {
            String[] declaredHeaders = declaredHeadersFor(exportRequest);
            if (declaredHeaders != null) {
                streamCsvWithHeaders(dtos, out, exportRequest, declaredHeaders);
                return;
            }
            List<Map<String, Object>> rows = flatRows(dtos, exportRequest);
            if (rows.isEmpty()) {
                return;
            }
            String[] headers = headersFor(rows);
            try (OutputStreamWriter writer =
                            new OutputStreamWriter(buffered(out), StandardCharsets.UTF_8);
                    CSVPrinter printer = new CSVPrinter(writer, csvFormat(headers))) {
                for (Map<String, Object> row : rows) {
                    writeCsvRow(printer, headers, row);
                }
                printer.flush();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to stream CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Streams a list of DTOs to a CSV file.
     *
     * @param dtos the iterator of DTOs to export
     * @param out the output stream to write the CSV to
     * @param <R> DTO type
     */
    public static <R> void streamCsv(@NonNull Iterator<R> dtos, @NonNull OutputStream out) {
        streamCsv(dtos, out, null);
    }

    private static <R> void streamCsvWithHeaders(
            Iterator<R> dtos,
            OutputStream out,
            @Nullable ExportRequest exportRequest,
            String[] headers)
            throws IOException {
        try (OutputStreamWriter writer =
                        new OutputStreamWriter(buffered(out), StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, csvFormat(headers))) {
            while (dtos.hasNext()) {
                writeCsvRow(printer, headers, toFlatMap(dtos.next(), exportRequest));
            }
            printer.flush();
        }
    }

    /**
     * Writes a single row to the CSV printer.
     *
     * @param printer the CSVPrinter to write to
     * @param headers the headers for the columns
     * @param row the data for the row
     */
    private static void writeCsvRow(CSVPrinter printer, String[] headers, Map<String, Object> row)
            throws IOException {
        for (String h : headers) {
            printer.print(csvSafeValue(row.get(h)));
        }
        printer.println();
    }

    private static Object csvSafeValue(Object value) {
        if (!(value instanceof String text) || text.isEmpty()) {
            return value;
        }
        char first = text.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t') {
            return "'" + text;
        }
        return text;
    }

    private static CSVFormat csvFormat(String[] headers) {
        return CSVFormat.DEFAULT.builder().setHeader(headers).get();
    }

    /**
     * Streams a list of DTOs to a JSON array. If exportRequest is provided, filters fields
     * according to include/exclude rules. Unlike CSV/XLSX, JSON exports keep the nested object
     * structure (no flattening).
     *
     * @param dtos the iterator of DTOs to export
     * @param out the output stream to write the JSON to
     * @param exportRequest the export request for field filtering (null for no filtering)
     * @param <R> DTO type
     */
    public static <R> void streamJson(
            @NonNull Iterator<R> dtos,
            @NonNull OutputStream out,
            @Nullable ExportRequest exportRequest) {
        try (JsonGenerator gen = objectMapper.getFactory().createGenerator(buffered(out))) {
            gen.writeStartArray();
            while (dtos.hasNext()) {
                R dto = dtos.next();
                if (exportRequest == null) {
                    objectMapper.writeValue(gen, dto);
                } else {
                    // Convert to map and filter fields
                    Map<String, Object> map =
                            objectMapper.convertValue(
                                    dto, new TypeReference<Map<String, Object>>() {});
                    Map<String, Object> filtered = filterJsonMap(map, exportRequest, "", 0);
                    objectMapper.writeValue(gen, filtered);
                }
            }
            gen.writeEndArray();
            gen.flush();
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to stream JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Streams a list of DTOs to a JSON array (without field filtering).
     *
     * @param dtos the iterator of DTOs to export
     * @param out the output stream to write the JSON to
     * @param <R> DTO type
     */
    public static <R> void streamJson(@NonNull Iterator<R> dtos, @NonNull OutputStream out) {
        streamJson(dtos, out, null);
    }

    /**
     * Filters a map structure based on ExportRequest rules, keeping nested structure intact. Unlike
     * flattenMap, this preserves the hierarchical structure for JSON exports.
     *
     * @param map the map to filter
     * @param exportRequest the export request with filtering rules
     * @param prefix the current path prefix for nested fields
     * @return filtered map
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> filterJsonMap(
            Map<String, Object> map, ExportRequest exportRequest, String prefix, int depth) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String fullPath = prefix.isEmpty() ? key : prefix + "." + key;
            Object value = entry.getValue();

            // Check if this field or any of its descendants should be included
            boolean shouldInclude = exportRequest.shouldIncludeField(fullPath);
            boolean hasIncludedDescendants = exportRequest.hasIncludedDescendants(fullPath);

            // Skip if this field is not included and has no included descendants
            if (!shouldInclude && !hasIncludedDescendants) {
                continue;
            }

            // Process the value
            if (value == null) {
                // Only include null if the field itself is included (not just descendants)
                if (shouldInclude) {
                    result.put(key, null);
                }
            } else if (value instanceof Map) {
                if (depth >= exportRequest.getEffectiveMaxDepth()) {
                    continue;
                }
                // Recursively filter nested maps
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                Map<String, Object> filteredNested =
                        filterJsonMap(nestedMap, exportRequest, fullPath, depth + 1);
                if (!filteredNested.isEmpty()) {
                    result.put(key, filteredNested);
                }
            } else if (value instanceof Collection) {
                if (depth >= exportRequest.getEffectiveMaxDepth()) {
                    continue;
                }
                // Process collections
                Collection<?> collection = (Collection<?>) value;
                if (!collection.isEmpty()) {
                    List<Object> filteredList = new ArrayList<>();
                    for (Object item : collection) {
                        if (item instanceof Map) {
                            Map<String, Object> itemMap = (Map<String, Object>) item;
                            Map<String, Object> filteredItem =
                                    filterJsonMap(itemMap, exportRequest, fullPath, depth + 1);
                            if (!filteredItem.isEmpty()) {
                                filteredList.add(filteredItem);
                            }
                        } else {
                            // Scalar items in collection: only include if parent is included
                            if (shouldInclude) {
                                filteredList.add(item);
                            }
                        }
                    }
                    if (!filteredList.isEmpty()) {
                        result.put(key, filteredList);
                    }
                } else {
                    // Empty collection: only include if the field itself is included
                    if (shouldInclude) {
                        result.put(key, collection);
                    }
                }
            } else {
                // Scalar value: only include if the field itself is included
                if (shouldInclude) {
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    /**
     * Streams DTOs to an XLSX file.
     *
     * <p>When {@link ExportRequest#getIncludeFields()} declares a field schema, worksheet rows are
     * flattened and written incrementally into the ZIP package. Without a declared schema, this
     * method buffers flattened rows first so it can preserve the historic union-header behavior.
     *
     * @param dtos the iterator of DTOs to export
     * @param out the output stream to write the XLSX file to
     * @param exportRequest the export request for field filtering (null for no filtering)
     * @param <R> DTO type
     */
    public static <R> void streamXlsx(
            @NonNull Iterator<R> dtos,
            @NonNull OutputStream out,
            @Nullable ExportRequest exportRequest) {
        try {
            OutputStream bufferedOut = buffered(out);
            String[] declaredHeaders = declaredHeadersFor(exportRequest);
            if (declaredHeaders != null) {
                writeXlsxStreaming(
                        declaredHeaders, streamingFlatRows(dtos, exportRequest), bufferedOut);
                bufferedOut.flush();
                return;
            }
            List<Map<String, Object>> rows = flatRows(dtos, exportRequest);
            if (rows.isEmpty()) {
                writeXlsxDocument(List.of(), new String[0], bufferedOut);
                bufferedOut.flush();
                return;
            }
            String[] headers = headersFor(rows);
            writeXlsxStreaming(headers, rows.iterator(), bufferedOut);
            bufferedOut.flush();
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to stream XLSX: " + e.getMessage(), e);
        }
    }

    /**
     * Streams a list of DTOs to an XLSX file.
     *
     * @param dtos the iterator of DTOs to export
     * @param out the output stream to write the XLSX file to
     * @param <R> DTO type
     */
    public static <R> void streamXlsx(@NonNull Iterator<R> dtos, @NonNull OutputStream out) {
        streamXlsx(dtos, out, null);
    }

    private static <R> Iterator<Map<String, Object>> streamingFlatRows(
            Iterator<R> dtos, @Nullable ExportRequest exportRequest) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return dtos.hasNext();
            }

            @Override
            public Map<String, Object> next() {
                return toFlatMap(dtos.next(), exportRequest);
            }
        };
    }

    /**
     * Writes a minimal XLSX workbook containing one worksheet with inline string cells.
     *
     * @param rows flattened row data
     * @param headers column headers
     * @param out target output stream
     * @throws IOException if the ZIP package cannot be written
     */
    // CHECKSTYLE.SUPPRESS: LineLength for +70 lines
    private static void writeXlsxDocument(
            List<Map<String, Object>> rows, String[] headers, OutputStream out) throws IOException {
        if (headers.length > XLSX_MAX_COLUMNS) {
            throw new IllegalArgumentException(
                    "XLSX export supports at most "
                            + XLSX_MAX_COLUMNS
                            + " columns; reduce included fields before retrying");
        }
        ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
        // CHECKSTYLE.SUPPRESS: LineLength for +50 lines
        putZipEntry(
                zip,
                "[Content_Types].xml",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels"
                           ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>
                """);
        putZipEntry(
                zip,
                "_rels/.rels",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                </Relationships>
                """);
        putZipEntry(
                zip,
                "xl/workbook.xml",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <sheets>
                    <sheet name="export" sheetId="1" r:id="rId1"/>
                  </sheets>
                </workbook>
                """);
        putZipEntry(
                zip,
                "xl/_rels/workbook.xml.rels",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                </Relationships>
                """);
        putZipEntry(zip, "xl/worksheets/sheet1.xml", worksheetXml(rows, headers));
        zip.finish();
    }

    private static void writeXlsxStreaming(
            String[] headers, Iterator<Map<String, Object>> rows, OutputStream out)
            throws IOException {
        if (headers.length > XLSX_MAX_COLUMNS) {
            throw new IllegalArgumentException(
                    "XLSX export supports at most "
                            + XLSX_MAX_COLUMNS
                            + " columns; reduce included fields before retrying");
        }
        ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
        // CHECKSTYLE.SUPPRESS: LineLength for +50 lines
        putZipEntry(
                zip,
                "[Content_Types].xml",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels"
                           ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/xl/workbook.xml"
                            ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                  <Override PartName="/xl/worksheets/sheet1.xml"
                            ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>
                """);
        putZipEntry(
                zip,
                "_rels/.rels",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships" Target="xl/workbook.xml"/>
                </Relationships>
                """);
        putZipEntry(
                zip,
                "xl/workbook.xml",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <sheets>
                    <sheet name="export" sheetId="1" r:id="rId1"/>
                  </sheets>
                </workbook>
                """);
        putZipEntry(
                zip,
                "xl/_rels/workbook.xml.rels",
                """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                </Relationships>
                """);

        zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
        zip.write(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        .getBytes(StandardCharsets.UTF_8));
        zip.write(
                "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                        .getBytes(StandardCharsets.UTF_8));
        zip.write("<sheetData>".getBytes(StandardCharsets.UTF_8));

        if (headers.length > 0) {
            zip.write(worksheetRowXml(1, headers, null).getBytes(StandardCharsets.UTF_8));
            int rowNumber = 2;
            while (rows.hasNext()) {
                String rowXml = worksheetRowXml(rowNumber++, headers, rows.next());
                zip.write(rowXml.getBytes(StandardCharsets.UTF_8));
            }
        }

        zip.write("</sheetData></worksheet>".getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
        zip.finish();
    }

    private static void putZipEntry(ZipOutputStream zip, String name, String content)
            throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String worksheetXml(List<Map<String, Object>> rows, String[] headers) {
        StringBuilder sheet = new StringBuilder(512);
        sheet.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sheet.append(
                "<worksheet xmlns=\"http://schemas.openxmlformats.org/"
                        + "spreadsheetml/2006/main\">");
        sheet.append("<sheetData>");
        if (headers.length > 0) {
            appendXlsxRow(sheet, 1, headers, null);
            int rowNumber = 2;
            for (Map<String, Object> row : rows) {
                appendXlsxRow(sheet, rowNumber++, headers, row);
            }
        }
        sheet.append("</sheetData></worksheet>");
        return sheet.toString();
    }

    private static void appendXlsxRow(
            StringBuilder sheet, int rowNumber, String[] headers, Map<String, Object> row) {
        sheet.append("<row r=\"").append(rowNumber).append("\">");
        for (int c = 0; c < headers.length; c++) {
            Object value = row == null ? headers[c] : row.get(headers[c]);
            appendXlsxCell(sheet, rowNumber, c, value);
        }
        sheet.append("</row>");
    }

    private static String worksheetRowXml(
            int rowNumber, String[] headers, Map<String, Object> row) {
        StringBuilder buffer = new StringBuilder(128);
        appendXlsxRow(buffer, rowNumber, headers, row);
        return buffer.toString();
    }

    private static void appendXlsxCell(
            StringBuilder sheet, int rowNumber, int column, Object value) {
        sheet.append("<c r=\"")
                .append(columnName(column))
                .append(rowNumber)
                .append("\" t=\"inlineStr\"><is><t>");
        if (value != null) {
            sheet.append(escapeXml(value.toString()));
        }
        sheet.append("</t></is></c>");
    }

    private static String columnName(int zeroBasedColumn) {
        if (zeroBasedColumn < 0 || zeroBasedColumn >= XLSX_MAX_COLUMNS) {
            throw new IllegalArgumentException("Invalid XLSX column index: " + zeroBasedColumn);
        }
        StringBuilder name = new StringBuilder(XLSX_MAX_COLUMN_NAME_LENGTH);
        int value = zeroBasedColumn + 1;
        for (int iterations = 0;
                iterations < XLSX_MAX_COLUMN_NAME_LENGTH && value > 0;
                iterations++) {
            int remainder = (value - 1) % 26;
            name.insert(0, (char) ('A' + remainder));
            value = (value - 1) / 26;
        }
        if (value > 0) {
            throw new IllegalArgumentException("Invalid XLSX column index: " + zeroBasedColumn);
        }
        return name.toString();
    }

    private static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static OutputStream buffered(OutputStream out) {
        return new BufferedOutputStream(out, EXPORT_STREAM_BUFFER_SIZE_BYTES);
    }
}
