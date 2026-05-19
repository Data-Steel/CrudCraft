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

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;


/**
 * Represents a property specification for a searchable field in a model.
 *
 */
public final class PropertySpec {

    private final FieldDescriptor descriptor;
    private final String name;
    private final Set<SearchOperator> operators;

    /**
     * Creates an immutable property specification.
     *
     * @param descriptor source field descriptor
     * @param name generated request property name
     * @param operators supported operators
     */
    public PropertySpec(FieldDescriptor descriptor, String name, Set<SearchOperator> operators) {
        this.descriptor = descriptor;
        this.name = name;
        this.operators = operators == null ? Set.of() : Set.copyOf(operators);
    }

    /**
     * Returns the source field descriptor.
     *
     * @return source descriptor
     */
    public FieldDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Returns the generated request property name.
     *
     * @return property name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the supported search operators.
     *
     * @return immutable operator set
     */
    public Set<SearchOperator> operators() {
        return operators;
    }

    /**
     * Adds generated fields and accessors for this property to the request DTO.
     *
     * @param cls the request DTO builder
     */
    public void addMembers(TypeSpec.Builder cls) {
        TypeName raw = TypeName.get(descriptor.getType());
        TypeName type = raw.isPrimitive() ? raw.box() : raw;

        // Map the type early so imports get generated consistently
        type = SearchTypeMapperRegistry.map(type);

        // Note: We do NOT create nested SearchRequest types for relationships.
        // The SearchFieldCollector already flattens nested fields (e.g., "author.name"
        // becomes "authorName"), so we use the actual field types directly. This avoids
        // Swagger generating bracket notation like "author[name]=value" which requires
        // relaxing Tomcat's security settings.

        OperatorFamilies families = operatorFamilies();
        if (families.size()) {
            TypeName integer = TypeName.INT.box();
            cls.addField(FieldSpec.builder(integer, name, Modifier.PRIVATE).build());
            cls.addMethod(
                    MethodSpec.methodBuilder("get" + up(name))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(integer)
                            .addStatement("return this.$N", name)
                            .build());
            cls.addMethod(
                    MethodSpec.methodBuilder("set" + up(name))
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(integer, name)
                            .addStatement("this.$N = $N", name, name)
                            .build());
            addOperatorMembers(cls);
            return;
        }
        if (families.value()) {
            FieldSpec field = FieldSpec.builder(type, name, Modifier.PRIVATE).build();
            cls.addField(field);
            cls.addMethod(
                    MethodSpec.methodBuilder("get" + up(name))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(type)
                            .addStatement("return this.$N", name)
                            .build());
            cls.addMethod(
                    MethodSpec.methodBuilder("set" + up(name))
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(type, name)
                            .addStatement("this.$N = $N", name, name)
                            .build());
        }
        if (families.range()) {
            String start = name + "Start";
            String end = name + "End";
            cls.addField(FieldSpec.builder(type, start, Modifier.PRIVATE).build());
            cls.addField(FieldSpec.builder(type, end, Modifier.PRIVATE).build());
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
        addOperatorMembers(cls);
    }

    private void addOperatorMembers(TypeSpec.Builder cls) {
        ClassName opEnum = ClassName.get(SearchOperator.class);
        String opField = name + "Op";
        cls.addField(opEnum, opField, Modifier.PRIVATE);
        cls.addMethod(SearchAccessorUtil.getter(opField, opEnum));
        cls.addMethod(SearchAccessorUtil.setter(opField, opEnum));
    }

    /**
     * Returns generated request field names that must be copied by a copy constructor.
     *
     * @return field names that participate in generated copy construction
     */
    public Set<String> copyFieldNames() {
        Set<String> names = new LinkedHashSet<>();
        OperatorFamilies families = operatorFamilies();
        if (families.size()) {
            names.add(name);
            names.add(name + "Op");
            return names;
        }
        if (families.value()) {
            names.add(name);
        }
        if (families.range()) {
            names.add(name + "Start");
            names.add(name + "End");
        }
        names.add(name + "Op");
        return names;
    }

    private OperatorFamilies operatorFamilies() {
        boolean value = false;
        boolean range = false;
        boolean size = false;
        for (SearchOperator operator : operators) {
            value = value || OperatorSpecRegistry.isValueOperator(operator);
            range = range || OperatorSpecRegistry.isRangeOperator(operator);
            size = size || OperatorSpecRegistry.isSizeOperator(operator);
        }
        return new OperatorFamilies(value, range, size);
    }

    private static String up(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private record OperatorFamilies(boolean value, boolean range, boolean size) {}
}
