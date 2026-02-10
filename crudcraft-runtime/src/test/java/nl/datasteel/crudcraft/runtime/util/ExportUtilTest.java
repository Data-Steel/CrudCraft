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
import java.util.Set;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    
    @Test
    void streamCsvWithExportRequestIncludeFieldsFiltersFields() {
        Post post = new Post("Java Tips", new Author("John Doe", "john@example.com"), List.of("java", "spring"));
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setIncludeFields(Set.of("title"));
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(post).iterator(), out, exportRequest);
        String text = out.toString(StandardCharsets.UTF_8);
        
        assertTrue(text.contains("title"));
        assertTrue(text.contains("Java Tips"));
        assertFalse(text.contains("author.name"));
        assertFalse(text.contains("John Doe"));
    }
    
    @Test
    void streamCsvWithExportRequestExcludeFieldsFiltersFields() {
        Post post = new Post("Java Tips", new Author("John Doe", "john@example.com"), List.of("java", "spring"));
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setExcludeFields(Set.of("author.email", "tags"));
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(post).iterator(), out, exportRequest);
        String text = out.toString(StandardCharsets.UTF_8);
        
        assertTrue(text.contains("title"));
        assertTrue(text.contains("author.name"));
        assertFalse(text.contains("author.email"));
        assertFalse(text.contains("tags"));
    }
    
    @Test
    void streamCsvWithMaxDepthZeroFlattensNestedObjects() {
        Post post = new Post("Java Tips", new Author("John Doe", "john@example.com"), List.of("java"));
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setMaxDepth(0);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(post).iterator(), out, exportRequest);
        String text = out.toString(StandardCharsets.UTF_8);
        
        assertTrue(text.contains("title"));
        assertTrue(text.contains("Java Tips"));
        // With maxDepth=0, nested objects should be serialized as JSON strings
        assertFalse(text.contains("author.name"));
        assertFalse(text.contains("author.email"));
        // Should contain some form of author data (as JSON)
        assertTrue(text.contains("author") || text.contains("John"));
    }
    
    @Test
    void streamXlsxWithExportRequestFiltersFields() {
        Post post = new Post("Java Tips", new Author("John Doe", "john@example.com"), List.of("java"));
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setIncludeFields(Set.of("title", "author.name"));
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamXlsx(List.of(post).iterator(), out, exportRequest);
        // Just verify it produces output without errors
        assertTrue(out.toByteArray().length > 0);
    }
    
    @Test
    void exportRequestEffectiveMaxDepthHandlesNegativeValues() {
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setMaxDepth(-5);
        
        assertEquals(0, exportRequest.getEffectiveMaxDepth());
    }
    
    @Test
    void exportRequestEffectiveMaxDepthDefaultsToOne() {
        ExportRequest exportRequest = new ExportRequest();
        
        assertEquals(1, exportRequest.getEffectiveMaxDepth());
    }
    
    @Test
    void exportRequestEffectiveMaxDepthReturnsSetValue() {
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setMaxDepth(3);
        
        assertEquals(3, exportRequest.getEffectiveMaxDepth());
    }
    
    @Test
    void exportRequestExcludeParentExcludesDescendants() {
        Post post = new Post("Java Tips", new Author("John Doe", "john@example.com"), List.of("java"));
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setExcludeFields(Set.of("author"));  // Exclude entire author subtree
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportUtil.streamCsv(List.of(post).iterator(), out, exportRequest);
        String text = out.toString(StandardCharsets.UTF_8);
        
        assertTrue(text.contains("title"));
        assertTrue(text.contains("Java Tips"));
        // With author excluded, should not have author.name or author.email
        assertFalse(text.contains("author.name"));
        assertFalse(text.contains("author.email"));
        assertFalse(text.contains("John Doe"));
    }
}
