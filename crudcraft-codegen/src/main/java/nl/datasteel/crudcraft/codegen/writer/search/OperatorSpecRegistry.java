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
package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import nl.datasteel.crudcraft.annotations.SearchOperator;

/**
 * Registry that defines which fields are generated per operator family.
 * IMPORTANT:
 * - Always build types with JavaPoet (ClassName/ParameterizedTypeName) so imports are emitted.
 * - value()/range()/size() MUST return fresh instances (tests require not-same).
 */
public final class OperatorSpecRegistry {

    private OperatorSpecRegistry() {}

    // ────────────────────────────────────────────────────────────────────────
    // Operator classification — aligned with CrudCraft enum
    // ────────────────────────────────────────────────────────────────────────

    // Value-like (single/multi token) comparisons & set/collection membership
    private static final EnumSet<SearchOperator> VALUE_OPS = EnumSet.of(
            // equality / inequality
            SearchOperator.EQUALS,
            SearchOperator.NOT_EQUALS,

            // membership
            SearchOperator.IN,
            SearchOperator.NOT_IN,

            // string/regex patterns
            SearchOperator.CONTAINS,
            SearchOperator.STARTS_WITH,
            SearchOperator.ENDS_WITH,
            SearchOperator.REGEX,

            // collection membership helpers (NOT size/empty)
            SearchOperator.CONTAINS_ALL,
            SearchOperator.CONTAINS_KEY,
            SearchOperator.CONTAINS_VALUE
    );

    // Range-like comparisons (including temporal)
    private static final EnumSet<SearchOperator> RANGE_OPS = EnumSet.of(
            SearchOperator.RANGE,
            SearchOperator.BETWEEN,
            SearchOperator.GT,
            SearchOperator.GTE,
            SearchOperator.LT,
            SearchOperator.LTE,
            SearchOperator.BEFORE,
            SearchOperator.AFTER
    );

    // Size-like comparisons (collections)
    private static final EnumSet<SearchOperator> SIZE_OPS = EnumSet.of(
            SearchOperator.SIZE_EQUALS,
            SearchOperator.SIZE_GT,
            SearchOperator.SIZE_LT
    );

    public static boolean isValueOperator(SearchOperator op) {
        return VALUE_OPS.contains(op);
    }

    public static boolean isRangeOperator(SearchOperator op) {
        return RANGE_OPS.contains(op);
    }

    public static boolean isSizeOperator(SearchOperator op) {
        return SIZE_OPS.contains(op);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Factory methods — must return FRESH instances
    // ────────────────────────────────────────────────────────────────────────

    public static ValueSpec value() { return new ValueSpec(); }
    public static RangeSpec range() { return new RangeSpec(); }
    public static SizeSpec  size()  { return new SizeSpec(); }

    // ────────────────────────────────────────────────────────────────────────
    // Implementations that actually add fields to the SearchRequest class
    // ────────────────────────────────────────────────────────────────────────

    /**
     * VALUE operators
     */
    public static final class ValueSpec {
        ValueSpec() {}
        public void addFields(com.squareup.javapoet.TypeSpec.Builder cls, String name, TypeName elementType) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(name);
            Objects.requireNonNull(elementType);

            ClassName setRaw = ClassName.get(Set.class);
            ParameterizedTypeName fieldType = ParameterizedTypeName.get(setRaw, elementType);

            FieldSpec f = FieldSpec.builder(fieldType, name, Modifier.PRIVATE).build();
            cls.addField(f);

            cls.addMethod(MethodSpec.methodBuilder("get" + up(name))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(fieldType)
                    .addStatement("return this.$N", name)
                    .build());

            cls.addMethod(MethodSpec.methodBuilder("set" + up(name))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(fieldType, name)
                    .addStatement("this.$N = $N", name, name)
                    .build());
        }
    }

    /**
     * RANGE operators:
     * Generate two fields: T nameStart; T nameEnd;
     */
    public static final class RangeSpec {
        RangeSpec() {}
        public void addFields(com.squareup.javapoet.TypeSpec.Builder cls, String name, TypeName type) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(name);
            Objects.requireNonNull(type);

            String start = name + "Start";
            String end   = name + "End";

            FieldSpec fStart = FieldSpec.builder(type, start, Modifier.PRIVATE).build();
            FieldSpec fEnd   = FieldSpec.builder(type, end,   Modifier.PRIVATE).build();

            cls.addField(fStart);
            cls.addField(fEnd);

            cls.addMethod(MethodSpec.methodBuilder("get" + up(start))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(type)
                    .addStatement("return this.$N", start)
                    .build());

            cls.addMethod(MethodSpec.methodBuilder("set" + up(start))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(type, start)
                    .addStatement("this.$N = $N", start, start)
                    .build());

            cls.addMethod(MethodSpec.methodBuilder("get" + up(end))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(type)
                    .addStatement("return this.$N", end)
                    .build());

            cls.addMethod(MethodSpec.methodBuilder("set" + up(end))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(type, end)
                    .addStatement("this.$N = $N", end, end)
                    .build());
        }
    }

    /**
     * SIZE operators:
     * Single Integer-like field with original name.
     */
    public static final class SizeSpec {
        SizeSpec() {}
        public void addFields(com.squareup.javapoet.TypeSpec.Builder cls, String name, TypeName boxedInteger) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(name);
            Objects.requireNonNull(boxedInteger);

            FieldSpec f = FieldSpec.builder(boxedInteger, name, Modifier.PRIVATE).build();
            cls.addField(f);

            cls.addMethod(MethodSpec.methodBuilder("get" + up(name))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(boxedInteger)
                    .addStatement("return this.$N", name)
                    .build());

            cls.addMethod(MethodSpec.methodBuilder("set" + up(name))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(boxedInteger, name)
                    .addStatement("this.$N = $N", name, name)
                    .build());
        }
    }

    private static String up(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
