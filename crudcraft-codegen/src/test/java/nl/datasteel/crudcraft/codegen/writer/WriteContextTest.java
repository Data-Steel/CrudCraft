/**
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
package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

/** Tests for WriteContext. */
class WriteContextTest {

    private ProcessingEnvironment env;
    private Elements elements;
    private Messager messager;
    private Filer filer;
    private WriteContext ctx;

    @BeforeEach
    void setup() {
        env = mock(ProcessingEnvironment.class);
        elements = mock(Elements.class);
        messager = mock(Messager.class);
        filer = mock(Filer.class);
        when(env.getElementUtils()).thenReturn(elements);
        when(env.getMessager()).thenReturn(messager);
        when(env.getFiler()).thenReturn(filer);
        ctx = new WriteContext(env);
    }

    @Test
    void snapshotCreatesCopy() {
        WriteContext copy = WriteContext.snapshotOf(ctx);
        assertNotSame(ctx, copy);
        assertSame(ctx.env(), copy.env());
    }

    @Test
    void findTypeElementDelegatesToEnvironment() {
        TypeElement te = mock(TypeElement.class);
        when(elements.getTypeElement("Foo")).thenReturn(te);
        assertEquals(te, ctx.findTypeElement("Foo"));
    }

    @Test
    void findTypeElementReturnsNullWhenNotFound() {
        when(elements.getTypeElement("Bar")).thenReturn(null);
        assertNull(ctx.findTypeElement("Bar"));
    }

    @Test
    void writeDelegatesToFiler() throws Exception {
        JavaFile jf = JavaFile.builder("com.example", TypeSpec.classBuilder("Test").build()).build();
        JavaFileObject jfo = new SimpleJavaFileObject(URI.create("string:///Test.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public StringWriter openWriter() { return new StringWriter(); }
        };
        when(filer.createSourceFile("com.example.Test")).thenReturn(jfo);
        ctx.write(jf);
        verify(filer).createSourceFile("com.example.Test");
    }

    @Test
    void writeReportsErrors() throws Exception {
        JavaFile jf = JavaFile.builder("com.example", TypeSpec.classBuilder("Err").build()).build();
        when(filer.createSourceFile("com.example.Err")).thenThrow(new IOException("bad"));
        ctx.write(jf);
        verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), contains("Failed to write"));
    }
}
