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
package nl.datasteel.crudcraft.codegen.writer.stubs;

import com.squareup.javapoet.JavaFile;
import java.util.List;
import javax.lang.model.type.TypeMirror;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MapperGeneratorTest {

    private FieldDescriptor parentField(TypeMirror type) {
        return new FieldDescriptor(
                new Identity("parent", type),
                new DtoOptions(true, true, true, new String[0]),
                new EnumOptions(false, List.of()),
                new Relationship(RelationshipType.MANY_TO_ONE, "", "nl.other.Parent", true, false),
                new Validation(List.of()),
                new SearchOptions(false, List.of(), 0),
                new Security(false, null, null)
        );
    }

    private FieldDescriptor childrenField(TypeMirror type) {
        return new FieldDescriptor(
                new Identity("children", type),
                new DtoOptions(true, true, true, new String[0]),
                new EnumOptions(false, List.of()),
                new Relationship(RelationshipType.ONE_TO_MANY, "", "nl.other.Child", true, false),
                new Validation(List.of()),
                new SearchOptions(false, List.of(), 0),
                new Security(false, null, null)
        );
    }

    private FieldDescriptor simpleField(TypeMirror type) {
        return new FieldDescriptor(
                new Identity("name", type),
                new DtoOptions(true, true, true, new String[0]),
                new EnumOptions(false, List.of()),
                new Relationship(RelationshipType.NONE, "", null, false, false),
                new Validation(List.of()),
                new SearchOptions(false, List.of(), 0),
                new Security(false, null, null)
        );
    }

    private ModelDescriptor descriptor(List<FieldDescriptor> fields) {
        ModelIdentity id = new ModelIdentity("Sample", "com.example", fields, "com.example");
        ModelFlags flags = new ModelFlags(true, true, false);
        EndpointOptions ep = new EndpointOptions(nl.datasteel.crudcraft.annotations.CrudTemplate.FULL, new nl.datasteel.crudcraft.annotations.CrudEndpoint[0], new nl.datasteel.crudcraft.annotations.CrudEndpoint[0], nl.datasteel.crudcraft.annotations.CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(false, null, List.of());
        return new ModelDescriptor(id, flags, ep, sec);
    }

    @Test
    void buildGeneratesMappingsForRelations() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor parent = parentField(tf.type(String.class));
        FieldDescriptor children = childrenField(tf.setOf(String.class));
        ModelDescriptor md = descriptor(List.of(parent, children));
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();
        assertTrue(code.contains("interface SampleMapper"));
        assertTrue(code.contains("@Mapper"));
        assertTrue(code.contains("qualifiedByName = \"SampleMapParent\""));
        assertTrue(code.contains("qualifiedByName = \"SampleMapChildSet\""));
        assertTrue(code.contains("mapParent"));
        assertTrue(code.contains("mapChildSet"));
        assertTrue(code.contains("uses = {ChildMapper.class}"));
        assertFalse(code.contains("ParentMapper.class"));    }

    @Test
    void buildWithoutRelationsHasNoUses() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor simple = simpleField(tf.type(String.class));
        ModelDescriptor md = descriptor(List.of(simple));
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();
        assertFalse(code.contains("uses ="));
        assertFalse(code.contains("qualifiedByName"));
    }

    @Test
    void requiresCrudEntityAndOrder() {
        MapperGenerator gen = new MapperGenerator();
        assertTrue(gen.requiresCrudEntity());
        assertEquals(2, gen.order());
    }

    @Test
    void writeHandlesFilerException() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(true, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor parent = parentField(tf.type(String.class));
        gen.write(descriptor(List.of(parent)), ctx);
        assertFalse(env.messager.notes.isEmpty());
    }

    @Test
    void writeHandlesIOException() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, true));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor parent = parentField(tf.type(String.class));
        gen.write(descriptor(List.of(parent)), ctx);
        assertFalse(env.messager.errors.isEmpty());
    }
}
