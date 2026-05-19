/*
 * Copyright (c) 2026 CrudCraft contributors
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

import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import java.util.EnumSet;
import java.util.Objects;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.annotations.SearchOperator;


/**
 * Registry that defines which fields are generated per operator family. IMPORTANT: - Always build
 * types with JavaPoet (ClassName/ParameterizedTypeName) so imports are emitted. -
 * value()/range()/size() MUST return fresh instances (tests require not-same).
 */
public final class OperatorSpecRegistry {

    /** Utility class constructor. */
    private OperatorSpecRegistry() {}

    // Operator classification aligned with CrudCraft enum.

    // Value-like (single/multi token) comparisons & set/collection membership
    private static final EnumSet<SearchOperator> VALUE_OPS =
            EnumSet.of(
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
                    SearchOperator.CONTAINS_VALUE,

                    // single-bound comparable comparisons
                    SearchOperator.GT,
                    SearchOperator.GTE,
                    SearchOperator.LT,
                    SearchOperator.LTE,
                    SearchOperator.BEFORE,
                    SearchOperator.AFTER);

    // Range-like comparisons (including temporal)
    private static final EnumSet<SearchOperator> RANGE_OPS =
            EnumSet.of(
                    SearchOperator.RANGE,
                    SearchOperator.BETWEEN);

    // Size-like comparisons (collections)
    private static final EnumSet<SearchOperator> SIZE_OPS =
            EnumSet.of(SearchOperator.SIZE_EQUALS, SearchOperator.SIZE_GT, SearchOperator.SIZE_LT);

    /**
     * Returns whether the operator is a value-like operator.
     *
     * @param op operator
     * @return {@code true} when operator is value-like
     */
    public static boolean isValueOperator(SearchOperator op) {
        return VALUE_OPS.contains(op);
    }

    /**
     * Returns whether the operator is a range-like operator.
     *
     * @param op operator
     * @return {@code true} when operator is range-like
     */
    public static boolean isRangeOperator(SearchOperator op) {
        return RANGE_OPS.contains(op);
    }

    /**
     * Returns whether the operator is a size-like operator.
     *
     * @param op operator
     * @return {@code true} when operator is size-like
     */
    public static boolean isSizeOperator(SearchOperator op) {
        return SIZE_OPS.contains(op);
    }

    // Factory methods; must return fresh instances.

    /**
     * Creates a value operator field contributor.
     *
     * @return new value spec
     */
    public static ValueSpec value() {
        return new ValueSpec();
    }

    /**
     * Creates a range operator field contributor.
     *
     * @return new range spec
     */
    public static RangeSpec range() {
        return new RangeSpec();
    }

    /**
     * Creates a size operator field contributor.
     *
     * @return new size spec
     */
    public static SizeSpec size() {
        return new SizeSpec();
    }

    // Implementations that add fields to the SearchRequest class.

    /** VALUE operators Uses scalar types (not wrapped in Set) to avoid bracket notation in URLs. */
    public static final class ValueSpec {
        /** Creates a value operator spec. */
        ValueSpec() {}

        /**
         * Adds scalar value fields for the property.
         *
         * @param cls type builder
         * @param name property name
         * @param elementType property type
         */
        public void addFields(
                com.palantir.javapoet.TypeSpec.Builder cls, String name, TypeName elementType) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(name);
            Objects.requireNonNull(elementType);

            // Use the element type directly as a scalar field
            // This avoids Swagger generating bracket notation like field[]=value
            // which violates RFC 7230 and RFC 3986
            TypeName fieldType = elementType;

            FieldSpec f = FieldSpec.builder(fieldType, name, Modifier.PRIVATE).build();
            cls.addField(f);

            cls.addMethod(
                    MethodSpec.methodBuilder("get" + up(name))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(fieldType)
                            .addStatement("return this.$N", name)
                            .build());

            cls.addMethod(
                    MethodSpec.methodBuilder("set" + up(name))
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(fieldType, name)
                            .addStatement("this.$N = $N", name, name)
                            .build());
        }
    }

    /** RANGE operators: Generate two fields: T nameStart; T nameEnd;. */
    public static final class RangeSpec {
        /** Creates a range operator spec. */
        RangeSpec() {}

        /**
         * Adds start/end range fields for the property.
         *
         * @param cls type builder
         * @param name property name
         * @param type property type
         */
        public void addFields(
                com.palantir.javapoet.TypeSpec.Builder cls, String name, TypeName type) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(name);
            Objects.requireNonNull(type);

            String start = name + "Start";
            String end = name + "End";

            FieldSpec startField = FieldSpec.builder(type, start, Modifier.PRIVATE).build();
            FieldSpec endField = FieldSpec.builder(type, end, Modifier.PRIVATE).build();

            cls.addField(startField);
            cls.addField(endField);

            cls.addMethod(
                    MethodSpec.methodBuilder("get" + up(start))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(type)
                            .addStatement("return this.$N", start)
                            .build());

            cls.addMethod(
                    MethodSpec.methodBuilder("set" + up(start))
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(type, start)
                            .addStatement("this.$N = $N", start, start)
                            .build());

            cls.addMethod(
                    MethodSpec.methodBuilder("get" + up(end))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(type)
                            .addStatement("return this.$N", end)
                            .build());

            cls.addMethod(
                    MethodSpec.methodBuilder("set" + up(end))
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(type, end)
                            .addStatement("this.$N = $N", end, end)
                            .build());
        }
    }

    /** SIZE operators: Single Integer-like field with original name. */
    public static final class SizeSpec {
        /** Creates a size operator spec. */
        SizeSpec() {}

        /**
         * Adds size field for the property.
         *
         * @param cls type builder
         * @param name property name
         * @param boxedInteger boxed integer type
         */
        public void addFields(
                com.palantir.javapoet.TypeSpec.Builder cls, String name, TypeName boxedInteger) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(name);
            Objects.requireNonNull(boxedInteger);

            FieldSpec f = FieldSpec.builder(boxedInteger, name, Modifier.PRIVATE).build();
            cls.addField(f);

            cls.addMethod(
                    MethodSpec.methodBuilder("get" + up(name))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(boxedInteger)
                            .addStatement("return this.$N", name)
                            .build());

            cls.addMethod(
                    MethodSpec.methodBuilder("set" + up(name))
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
