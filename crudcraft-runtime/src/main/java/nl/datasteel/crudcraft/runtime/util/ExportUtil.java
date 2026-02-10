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
package nl.datasteel.crudcraft.runtime.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Utility methods for exporting DTO lists to various formats.
 */
public final class ExportUtil {

    /** The ObjectMapper instance used for JSON serialization. */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Private constructor to prevent instantiation.
     * This class provides static utility methods only.
     */
    private ExportUtil() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    /**
     * Flattens a nested map structure into a single-level map with dot-separated keys.
     * Nested objects are flattened recursively, collections are converted to comma-separated strings,
     * and null values are preserved.
     *
     * @param prefix the prefix to prepend to keys (empty string for top level)
     * @param value  the value to flatten
     * @param result the map to store flattened key-value pairs
     */
    @SuppressWarnings("unchecked")
    private static void flattenMap(String prefix, Object value, Map<String, Object> result) {
        if (value == null) {
            result.put(prefix, null);
            return;
        }

        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            if (map.isEmpty()) {
                result.put(prefix, null);
                return;
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                flattenMap(newPrefix, entry.getValue(), result);
            }
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                result.put(prefix, null);
            } else {
                // Convert collection to comma-separated string
                String collectionStr = collection.stream()
                        .map(item -> {
                            if (item == null) {
                                return "null";
                            } else if (item instanceof Map) {
                                // For nested objects in collections, convert to JSON-like string
                                Map<String, Object> itemMap = (Map<String, Object>) item;
                                return itemMap.entrySet().stream()
                                        .map(e -> e.getKey() + "=" + e.getValue())
                                        .collect(Collectors.joining(", ", "{", "}"));
                            } else {
                                return String.valueOf(item);
                            }
                        })
                        .collect(Collectors.joining(", "));
                result.put(prefix, collectionStr);
            }
        } else {
            result.put(prefix, value);
        }
    }

    /**
     * Converts a DTO to a flattened map structure suitable for CSV/XLSX export.
     * Nested objects are flattened with dot-separated keys (e.g., "author.name", "author.email").
     *
     * @param dto the DTO to convert
     * @return a flattened map representation of the DTO
     */
    private static <R> Map<String, Object> toFlatMap(R dto) {
        Map<String, Object> originalMap = objectMapper.convertValue(
                dto, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> flatMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
            flattenMap(entry.getKey(), entry.getValue(), flatMap);
        }
        return flatMap;
    }

    /**
     * Exports a list of DTOs to a CSV file.
     *
     * @param dtos the list of DTOs to export
     * @return a byte array containing the CSV data
     */
    public static <R> byte[] toCsv(List<R> dtos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (dtos.isEmpty()) {
                return out.toByteArray();
            }
            List<Map<String, Object>> maps = dtos.stream()
                    .map(ExportUtil::toFlatMap)
                    .toList();
            String[] headers = maps.get(0).keySet().toArray(new String[0]);
            try (CSVPrinter printer = new CSVPrinter(
                    new OutputStreamWriter(out, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.withHeader(headers))) {
                for (Map<String, Object> row : maps) {
                    for (String h : headers) {
                        printer.print(row.get(h));
                    }
                    printer.println();
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export CSV", e);
        }
    }

    /**
     * Exports a list of DTOs to a JSON array.
     *
     * @param dtos the list of DTOs to export
     * @return a byte array containing the JSON data
     */
    public static <R> byte[] toJson(List<R> dtos) {
        try {
            return objectMapper.writeValueAsBytes(dtos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export JSON", e);
        }
    }

    /**
     * Exports a list of DTOs to an XLSX file.
     *
     * @param dtos the list of DTOs to export
     * @return a byte array containing the XLSX file data
     */
    public static <R> byte[] toXlsx(List<R> dtos) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("export");

            if (!dtos.isEmpty()) {
                List<Map<String, Object>> maps = dtos.stream()
                        .map(ExportUtil::toFlatMap)
                        .collect(Collectors.toList());
                String[] headers = maps.get(0).keySet().toArray(new String[0]);
                Row headerRow = sheet.createRow(0);
                for (int c = 0; c < headers.length; c++) {
                    headerRow.createCell(c).setCellValue(headers[c]);
                }
                int r = 1;
                for (Map<String, Object> row : maps) {
                    Row excelRow = sheet.createRow(r++);
                    for (int c = 0; c < headers.length; c++) {
                        Object val = row.get(headers[c]);
                        excelRow.createCell(c).setCellValue(val != null ? val.toString() : "");
                    }
                }
            }
            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        } catch (Exception e)   {
            throw new RuntimeException("Failed to export XLSX", e);
        }
    }

    /**
     * Streams a list of DTOs to a CSV file.
     *
     * @param dtos the iterator of DTOs to export
     * @param out  the output stream to write the CSV to
     */
    public static <R> void streamCsv(Iterator<R> dtos, OutputStream out) {
        try {
            if (!dtos.hasNext()) {
                return;
            }
            
            // Collect all rows first to determine complete header set
            List<Map<String, Object>> rows = new java.util.ArrayList<>();
            java.util.Set<String> allHeaders = new java.util.LinkedHashSet<>();
            
            while (dtos.hasNext()) {
                Map<String, Object> row = toFlatMap(dtos.next());
                rows.add(row);
                allHeaders.addAll(row.keySet());
            }
            
            if (rows.isEmpty()) {
                return;
            }
            
            String[] headers = allHeaders.toArray(new String[0]);
            try (CSVPrinter printer = new CSVPrinter(
                    new OutputStreamWriter(out, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.withHeader(headers))) {
                for (Map<String, Object> row : rows) {
                    writeCsvRow(printer, headers, row);
                }
                printer.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to export CSV", e);
        }
    }

    /**
     * Writes a single row to the CSV printer.
     *
     * @param printer the CSVPrinter to write to
     * @param headers the headers for the columns
     * @param row     the data for the row
     */
    private static void writeCsvRow(CSVPrinter printer, String[] headers,
                                    Map<String, Object> row) throws Exception {
        for (String h : headers) {
            printer.print(row.get(h));
        }
        printer.println();
    }

    /**
     * Streams a list of DTOs to a JSON array.
     *
     * @param dtos the iterator of DTOs to export
     * @param out  the output stream to write the JSON to
     */
    public static <R> void streamJson(Iterator<R> dtos, OutputStream out) {
        try (JsonGenerator gen = objectMapper.getFactory().createGenerator(out)) {
            gen.writeStartArray();
            while (dtos.hasNext()) {
                objectMapper.writeValue(gen, dtos.next());
            }
            gen.writeEndArray();
            gen.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export JSON", e);
        }
    }

    /**
     * Streams a list of DTOs to an XLSX file.
     *
     * @param dtos the iterator of DTOs to export
     * @param out  the output stream to write the XLSX file to
     */
    public static <R> void streamXlsx(Iterator<R> dtos, OutputStream out) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("export");
            if (!dtos.hasNext()) {
                workbook.write(out);
                workbook.dispose();
                return;
            }
            
            // Collect all rows first to determine complete header set
            List<Map<String, Object>> rows = new java.util.ArrayList<>();
            java.util.Set<String> allHeaders = new java.util.LinkedHashSet<>();
            
            while (dtos.hasNext()) {
                Map<String, Object> rowMap = toFlatMap(dtos.next());
                rows.add(rowMap);
                allHeaders.addAll(rowMap.keySet());
            }
            
            if (rows.isEmpty()) {
                workbook.write(out);
                workbook.dispose();
                return;
            }
            
            String[] headers = allHeaders.toArray(new String[0]);
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < headers.length; c++) {
                headerRow.createCell(c).setCellValue(headers[c]);
            }
            
            int r = 1;
            for (Map<String, Object> rowMap : rows) {
                writeXlsxRow(sheet.createRow(r++), headers, rowMap);
            }
            
            workbook.write(out);
            workbook.dispose();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export XLSX", e);
        }
    }

    /**
     * Writes a single row to the XLSX sheet.
     *
     * @param excelRow the row to write to
     * @param headers  the headers for the columns
     * @param row      the data for the row
     */
    private static void writeXlsxRow(Row excelRow, String[] headers, Map<String, Object> row) {
        for (int c = 0; c < headers.length; c++) {
            Object val = row.get(headers[c]);
            excelRow.createCell(c).setCellValue(val != null ? val.toString() : "");
        }
    }
}