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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ExportUtilTest {

    record Person(String name, int age) {}

    record Event(String title, Instant timestamp) {}

    @Test
    void toCsvReturnsEmptyForEmptyList() {
        assertEquals(0, ExportUtil.toCsv(List.of()).length);
    }

    @Test
    void toCsvWritesAllRows() {
        byte[] csv = ExportUtil.toCsv(List.of(new Person("Alice", 30), new Person("Bob", 40)));
        String text = new String(csv, StandardCharsets.UTF_8);
        assertTrue(text.contains("name,age"));
        assertTrue(text.contains("Alice"));
        assertTrue(text.contains("Bob"));
    }

    @Test
    void toJsonSerializesList() {
        byte[] json = ExportUtil.toJson(List.of(new Person("Alice", 30)));
        assertEquals("[{\"name\":\"Alice\",\"age\":30}]", new String(json, StandardCharsets.UTF_8));
    }

    @Test
    void toXlsxHandlesEmptyList() {
        byte[] bytes = ExportUtil.toXlsx(List.of());
        assertTrue(bytes.length > 0);
    }

    @Test
    void toXlsxWritesWorkbook() {
        byte[] bytes = ExportUtil.toXlsx(List.of(new Person("Alice", 30)));
        assertTrue(bytes.length > 0);
    }

    @Test
    void streamCsvWithNoDataProducesNothing() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.<Person>of().iterator(), out);
        assertEquals("", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void streamCsvWritesData() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(new Person("Alice", 30)).iterator(), out);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("Alice"));
    }

    @Test
    void streamJsonWritesArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamJson(List.of(new Person("Alice", 30), new Person("Bob", 40)).iterator(), out);
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Alice"));
        assertTrue(text.startsWith("["));
    }

    @Test
    void streamXlsxHandlesNoData() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.<Person>of().iterator(), out);
        assertTrue(out.toByteArray().length > 0);
    }

    @Test
    void streamXlsxWritesWorkbook() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.of(new Person("Alice", 30)).iterator(), out);
        assertTrue(out.toByteArray().length > 0);
    }

    @Test
    void toCsvHandlesInstantFields() {
        Instant now = Instant.parse("2024-01-15T10:30:00Z");
        byte[] csv = ExportUtil.toCsv(List.of(new Event("Meeting", now)));
        String text = new String(csv, StandardCharsets.UTF_8);
        assertTrue(text.contains("title,timestamp"));
        assertTrue(text.contains("Meeting"));
        assertTrue(text.contains("2024-01-15T10:30:00Z"));
    }

    @Test
    void toJsonHandlesInstantFields() {
        Instant now = Instant.parse("2024-01-15T10:30:00Z");
        byte[] json = ExportUtil.toJson(List.of(new Event("Meeting", now)));
        String text = new String(json, StandardCharsets.UTF_8);
        assertTrue(text.contains("Meeting"));
        assertTrue(text.contains("2024-01-15T10:30:00Z"));
    }

    @Test
    void toXlsxHandlesInstantFields() {
        Instant now = Instant.parse("2024-01-15T10:30:00Z");
        byte[] xlsx = ExportUtil.toXlsx(List.of(new Event("Meeting", now)));
        assertTrue(xlsx.length > 0);
    }

    @Test
    void streamCsvHandlesInstantFields() {
        Instant now = Instant.parse("2024-01-15T10:30:00Z");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(new Event("Meeting", now)).iterator(), out);
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Meeting"));
        assertTrue(text.contains("2024-01-15T10:30:00Z"));
    }

    @Test
    void streamJsonHandlesInstantFields() {
        Instant now = Instant.parse("2024-01-15T10:30:00Z");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamJson(List.of(new Event("Meeting", now)).iterator(), out);
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Meeting"));
        assertTrue(text.contains("2024-01-15T10:30:00Z"));
    }

    @Test
    void streamXlsxHandlesInstantFields() {
        Instant now = Instant.parse("2024-01-15T10:30:00Z");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.of(new Event("Meeting", now)).iterator(), out);
        assertTrue(out.toByteArray().length > 0);
    }
}
