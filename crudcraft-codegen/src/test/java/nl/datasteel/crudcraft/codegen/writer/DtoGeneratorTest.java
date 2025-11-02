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
import java.util.List;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for DtoGenerator. */
class DtoGeneratorTest {

    private WriteContext ctx;

    @BeforeEach
    void setup() {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);
        ctx = new WriteContext(env);
    }

    @Test
    void returnsEmptyListForInvalidModel() {
        DtoGenerator gen = new DtoGenerator();
        assertTrue(gen.generate(null, ctx).isEmpty());
    }

    @Test
    void generatesThreeBasicDtos() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of());

        DtoGenerator gen = new DtoGenerator();
        List<JavaFile> files = gen.generate(md, ctx);
        assertEquals(3, files.size());
        assertEquals("BookRequestDto", files.get(0).typeSpec.name);
        assertEquals("BookResponseDto", files.get(1).typeSpec.name);
        assertEquals("BookRef", files.get(2).typeSpec.name);
    }
}
