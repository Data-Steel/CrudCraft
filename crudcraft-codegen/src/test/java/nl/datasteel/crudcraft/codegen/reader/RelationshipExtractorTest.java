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
package nl.datasteel.crudcraft.codegen.reader;

import java.lang.reflect.Proxy;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.reader.field.RelationshipExtractor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RelationshipExtractorTest {
    private Elements elements;

    @BeforeEach
    void compile() {
        String src = "package t;" +
                "import jakarta.persistence.*;" +
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "@CrudCrafted class Other {}" +
                "class C {" +
                "@OneToMany(mappedBy=\"c\") java.util.List<Other> otm;" +
                "@ManyToMany(mappedBy=\"c\", targetEntity=Other.class) java.util.List<Other> mtm;" +
                "@OneToOne(mappedBy=\"c\") Other oto;" +
                "@ManyToOne Other mto;" +
                "@Embedded Other emb;" +
                "String none;" +
                "}";
        elements = CompilationTestUtils.elements("t.C", src);
    }

    private VariableElement field(String name) {
        var type = elements.getTypeElement("t.C");
        return (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals(name)).findFirst().orElseThrow();
    }

    @Test
    void extractsDifferentRelationshipTypes() {
        Relationship otm = RelationshipExtractor.INSTANCE.extract(field("otm"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.ONE_TO_MANY, otm.getRelationshipType());
        Relationship mtm = RelationshipExtractor.INSTANCE.extract(field("mtm"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.MANY_TO_MANY, mtm.getRelationshipType());
        assertTrue(mtm.isTargetCrud());
        Relationship oto = RelationshipExtractor.INSTANCE.extract(field("oto"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.ONE_TO_ONE, oto.getRelationshipType());
        Relationship mto = RelationshipExtractor.INSTANCE.extract(field("mto"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.MANY_TO_ONE, mto.getRelationshipType());
        Relationship none = RelationshipExtractor.INSTANCE.extract(field("none"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.NONE, none.getRelationshipType());
        Relationship emb = RelationshipExtractor.INSTANCE.extract(field("emb"), new TestUtils.ProcessingEnvStub(elements));
        assertTrue(emb.isEmbedded());
    }

    @Test
    void handlesNullFieldTypeGracefully() {
        VariableElement ve = (VariableElement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{VariableElement.class},
                (p, m, a) -> switch (m.getName()) {
                    case "asType" -> null;
                    case "getAnnotation" -> null;
                    case "getAnnotationMirrors" -> List.of();
                    case "getKind" -> ElementKind.FIELD;
                    default -> null;
                });
        Relationship rel = RelationshipExtractor.INSTANCE.extract(ve, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.NONE, rel.getRelationshipType());
        assertEquals("java.lang.Object", rel.getTargetType());
    }
}
