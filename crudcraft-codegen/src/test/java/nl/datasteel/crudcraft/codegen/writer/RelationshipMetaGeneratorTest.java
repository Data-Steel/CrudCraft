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
package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.JavaFile;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for RelationshipMetaGenerator. */
class RelationshipMetaGeneratorTest {

    private WriteContext ctx;

    @BeforeEach
    void setup() {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);
        ctx = new WriteContext(env);
    }

    @Test
    void generateReturnsEmptyForInvalidModel() {
        RelationshipMetaGenerator gen = new RelationshipMetaGenerator();
        List<JavaFile> files = gen.generate(null, ctx);
        assertTrue(files.isEmpty());
    }

    @Test
    void generatesFieldCachesForBidirectionalRelationsOnly() {
        FieldDescriptor fd1 = mock(FieldDescriptor.class);
        when(fd1.getName()).thenReturn("author");
        when(fd1.getTargetType()).thenReturn("com.example.Author");
        when(fd1.getRelType()).thenReturn(RelationshipType.ONE_TO_ONE);
        when(fd1.getMappedBy()).thenReturn(null);
        FieldDescriptor fd2 = mock(FieldDescriptor.class);
        when(fd2.getName()).thenReturn("publisher");
        when(fd2.getRelType()).thenReturn(RelationshipType.MANY_TO_ONE);

        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of(fd1, fd2));

        RelationshipMetaGenerator gen = new RelationshipMetaGenerator();
        List<JavaFile> files = gen.generate(md, ctx);
        assertEquals(1, files.size());
        String src = files.get(0).toString();
        assertTrue(src.contains("authorField"));
        assertTrue(src.contains("author_booksField"));
        assertFalse(src.contains("publisherField"));
    }

    @Test
    void generatesUniqueVariableNamesForMultipleOneToManyRelationships() {
        // Test for issue: duplicate local variable names when multiple @OneToMany relations exist
        FieldDescriptor fd1 = mock(FieldDescriptor.class);
        when(fd1.getName()).thenReturn("comments");
        when(fd1.getTargetType()).thenReturn("com.example.Comment");
        when(fd1.getRelType()).thenReturn(RelationshipType.ONE_TO_MANY);
        when(fd1.getMappedBy()).thenReturn("post");

        FieldDescriptor fd2 = mock(FieldDescriptor.class);
        when(fd2.getName()).thenReturn("likes");
        when(fd2.getTargetType()).thenReturn("com.example.Like");
        when(fd2.getRelType()).thenReturn(RelationshipType.ONE_TO_MANY);
        when(fd2.getMappedBy()).thenReturn("post");

        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Post");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of(fd1, fd2));

        RelationshipMetaGenerator gen = new RelationshipMetaGenerator();
        List<JavaFile> files = gen.generate(md, ctx);
        assertEquals(1, files.size());
        String src = files.get(0).toString();

        // Verify unique variable names for the first relationship
        assertTrue(src.contains("commentsChildren"), "Should have commentsChildren variable");
        assertTrue(src.contains("commentsChild"), "Should have commentsChild variable");
        
        // Verify unique variable names for the second relationship
        assertTrue(src.contains("likesChildren"), "Should have likesChildren variable");
        assertTrue(src.contains("likesChild"), "Should have likesChild variable");

        // Verify correct inverse handling - should use .set() not .add()/.remove()
        assertTrue(src.contains("comments_postField.set(commentsChild, entity)"),
                "Should set inverse field for comments");
        assertTrue(src.contains("likes_postField.set(likesChild, entity)"),
                "Should set inverse field for likes");
        assertTrue(src.contains("comments_postField.set(commentsChild, null)"),
                "Should clear inverse field for comments");
        assertTrue(src.contains("likes_postField.set(likesChild, null)"),
                "Should clear inverse field for likes");

        // Should NOT contain the old incorrect code patterns
        assertFalse(src.contains(".add(entity)"), "Should not use .add() for ONE_TO_MANY inverse");
        assertFalse(src.contains(".remove(entity)"), "Should not use .remove() for ONE_TO_MANY inverse");
    }
}
