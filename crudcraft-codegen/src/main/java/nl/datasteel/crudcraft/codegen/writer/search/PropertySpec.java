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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;

/**
 * Represents a property specification for a searchable field in a model.
 * This includes the field descriptor, the name of the property, and the
 * set of search operators applicable to this property.
 */
public record PropertySpec(FieldDescriptor descriptor, String name, Set<SearchOperator> operators) {

    /**
     * Immutable constructor for PropertySpec.
     */
    public PropertySpec(FieldDescriptor descriptor, String name, Set<SearchOperator> operators) {
        this.descriptor = descriptor;
        this.name = name;
        this.operators = operators == null ? Set.of() : Set.copyOf(operators);
    }

    /**
     * Adds the necessary fields and methods to the provided TypeSpec.Builder for this property.
     */
    public void addMembers(TypeSpec.Builder cls) {
        TypeName raw = TypeName.get(descriptor.getType());
        TypeName type = raw.isPrimitive() ? raw.box() : raw;

        if (descriptor.getRelType() != RelationshipType.NONE || descriptor.isEmbedded()) {
            type = resolveSearchType(type, descriptor.getTargetType());
        }

        if (operators.stream().anyMatch(OperatorSpecRegistry::isValueOperator)) {
            OperatorSpecRegistry.value().addFields(cls, name, type);
        }

        if (operators.stream().anyMatch(OperatorSpecRegistry::isRangeOperator)) {
            OperatorSpecRegistry.range().addFields(cls, name, type);
        }

        if (operators.stream().anyMatch(OperatorSpecRegistry::isSizeOperator)) {
            OperatorSpecRegistry.size().addFields(cls, name, TypeName.INT.box());
        }

        ClassName opEnum = ClassName.get(SearchOperator.class);
        String opField = name + "Op";
        cls.addField(opEnum, opField, Modifier.PRIVATE);
        cls.addMethod(SearchAccessorUtil.getter(opField, opEnum));
        cls.addMethod(SearchAccessorUtil.setter(opField, opEnum));
    }

    /**
     * Adds statements to copy all properties from another instance.
     *
     * @param ctor constructor builder to append the copy statements to
     */
    public void addCopyStatements(MethodSpec.Builder ctor) {
        for (String field : fieldNames()) {
            String up = Character.toUpperCase(field.charAt(0)) + field.substring(1);
            ctor.addStatement("this.set$L(other.get$L())", up, up);
        }
    }

    /**
     * Returns the names of all fields added for this property including operator fields.
     */
    private Set<String> fieldNames() {
        Set<String> names = new LinkedHashSet<>();
        if (operators.stream().anyMatch(OperatorSpecRegistry::isValueOperator)) {
            names.add(name);
        }
        if (operators.stream().anyMatch(OperatorSpecRegistry::isRangeOperator)) {
            names.add(name + "Start");
            names.add(name + "End");
        }
        if (operators.stream().anyMatch(OperatorSpecRegistry::isSizeOperator)) {
            names.add(name);
        }
        names.add(name + "Op");
        return names;
    }

    /**
     * Resolves the search type for relationship or embedded fields by mapping the
     * target entity to its corresponding {@code SearchRequest} type.
     * If the original type is parameterized (e.g. {@code Set<Foo>}), the raw type
     * is preserved while the generic type is replaced by {@code FooSearchRequest}.
     */
    private TypeName resolveSearchType(TypeName original, String targetFqn) {
        String simple = targetFqn.substring(targetFqn.lastIndexOf('.') + 1);
        String pkgBase = targetFqn.substring(0, targetFqn.lastIndexOf('.'));
        ClassName searchClass = ClassName.get(pkgBase + ".search", simple + "SearchRequest");
        if (original instanceof ParameterizedTypeName ptn) {
            return ParameterizedTypeName.get(ptn.rawType, searchClass);
        }
        return searchClass;
    }
}

