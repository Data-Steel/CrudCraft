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
    
    record Address(String city, String country) {}
    
    record Author(String name, String email) {}
    
    record Post(String title, Author author, List<String> tags) {}
    
    record PostWithNullAuthor(String title, Author author) {}

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
    
    @Test
    void toCsvFlattensNestedObjects() {
        Post post = new Post("Java Tips", new Author("John Doe", "john@example.com"), List.of("java", "spring"));
        byte[] csv = ExportUtil.toCsv(List.of(post));
        String text = new String(csv, StandardCharsets.UTF_8);
        assertTrue(text.contains("title"));
        assertTrue(text.contains("author.name"));
        assertTrue(text.contains("author.email"));
        assertTrue(text.contains("Java Tips"));
        assertTrue(text.contains("John Doe"));
        assertTrue(text.contains("john@example.com"));
    }
    
    @Test
    void streamCsvFlattensNestedObjects() {
        Post post = new Post("Java Tips", new Author("John Doe", "john@example.com"), List.of("java", "spring"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(post).iterator(), out);
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("author.name"));
        assertTrue(text.contains("author.email"));
        assertTrue(text.contains("John Doe"));
        assertTrue(text.contains("john@example.com"));
    }
    
    @Test
    void streamXlsxFlattensNestedObjects() {
        Post post = new Post("Java Tips", new Author("John Doe", "john@example.com"), List.of("java", "spring"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.of(post).iterator(), out);
        assertTrue(out.toByteArray().length > 0);
    }
    
    @Test
    void streamCsvHandlesNullNestedObjects() {
        PostWithNullAuthor post1 = new PostWithNullAuthor("Post 1", null);
        PostWithNullAuthor post2 = new PostWithNullAuthor("Post 2", new Author("Jane", "jane@example.com"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(post1, post2).iterator(), out);
        String text = out.toString(StandardCharsets.UTF_8);
        // Should contain headers from both rows
        assertTrue(text.contains("title"));
        assertTrue(text.contains("author.name") || text.contains("author.email"));
        assertTrue(text.contains("Post 1"));
        assertTrue(text.contains("Post 2"));
        assertTrue(text.contains("Jane"));
    }
    
    @Test
    void streamXlsxHandlesNullNestedObjects() {
        PostWithNullAuthor post1 = new PostWithNullAuthor("Post 1", null);
        PostWithNullAuthor post2 = new PostWithNullAuthor("Post 2", new Author("Jane", "jane@example.com"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.of(post1, post2).iterator(), out);
        assertTrue(out.toByteArray().length > 0);
    }
    
    @Test
    void toCsvHandlesCollectionsAsCommaSeparated() {
        Post post = new Post("Java Tips", new Author("John Doe", "john@example.com"), List.of("java", "spring", "boot"));
        byte[] csv = ExportUtil.toCsv(List.of(post));
        String text = new String(csv, StandardCharsets.UTF_8);
        assertTrue(text.contains("tags"));
        assertTrue(text.contains("java"));
        assertTrue(text.contains("spring"));
        assertTrue(text.contains("boot"));
    }
}
