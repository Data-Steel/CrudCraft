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
package nl.datasteel.crudcraft.runtime.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExportUtilTest {

    record Person(String name, int age) {}

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
}
