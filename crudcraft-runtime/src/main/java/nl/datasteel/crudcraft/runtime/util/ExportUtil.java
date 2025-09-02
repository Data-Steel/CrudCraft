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
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Private constructor to prevent instantiation.
     * This class provides static utility methods only.
     */
    private ExportUtil() {
        throw new IllegalStateException("Utility class should not be instantiated");
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
                    .map(d -> objectMapper.convertValue(
                            d, new TypeReference<Map<String, Object>>() {}))
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
                        .map(d -> objectMapper.convertValue(
                                d, new TypeReference<Map<String, Object>>() {}))
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
            R first = dtos.next();
            Map<String, Object> firstMap =
                    objectMapper.convertValue(first, new TypeReference<Map<String, Object>>() {});
            String[] headers = firstMap.keySet().toArray(new String[0]);
            try (CSVPrinter printer = new CSVPrinter(
                    new OutputStreamWriter(out, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.withHeader(headers))) {
                writeCsvRow(printer, headers, firstMap);
                while (dtos.hasNext()) {
                    Map<String, Object> row =
                            objectMapper.convertValue(
                                    dtos.next(), new TypeReference<Map<String, Object>>() {});
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
            R first = dtos.next();
            Map<String, Object> firstMap =
                    objectMapper.convertValue(first, new TypeReference<Map<String, Object>>() {});
            String[] headers = firstMap.keySet().toArray(new String[0]);
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < headers.length; c++) {
                headerRow.createCell(c).setCellValue(headers[c]);
            }
            int r = 1;
            writeXlsxRow(sheet.createRow(r++), headers, firstMap);
            while (dtos.hasNext()) {
                Map<String, Object> rowMap = objectMapper.convertValue(
                        dtos.next(), new TypeReference<Map<String, Object>>() {});
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