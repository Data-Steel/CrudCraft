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
package nl.datasteel.crudcraft.codegen.reader.field;

import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;

/**
 * Extracts {@link SearchOptions} for a field.
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class SearchOptionsExtractor implements FieldPartExtractor<SearchOptions> {

    /**
     * Singleton instance.
     */
    public static final SearchOptionsExtractor INSTANCE = new SearchOptionsExtractor();

    /**
     * Extracts SearchOptions from a field annotated with @Searchable.
     *
     * @param field the VariableElement representing the field
     * @param env the ProcessingEnvironment for accessing annotations and other processing features
     * @return SearchOptions instance containing searchable status, operators, and depth
     */
    @Override
    public SearchOptions extract(VariableElement field, ProcessingEnvironment env) {
        TypeMirror fieldType = field.asType();
        Searchable annotation = field.getAnnotation(Searchable.class);

        boolean isSearchable = annotation != null;
        List<SearchOperator> operators = isSearchable
                ? Arrays.asList(annotation.operators())
                : List.of();

        if (isSearchable && operators.isEmpty()) {
            boolean isRelationship = isRelationshipField(field);
            operators = getDefaultOperatorsFor(fieldType, isRelationship);
        }

        int depth = isSearchable ? annotation.depth() : 0;

        FieldPartExtractor.log(env.getMessager(), Diagnostic.Kind.NOTE, field,
                String.format("Extracting SearchOptions → searchable = %s, "
                        + "operators = %s, depth = %d",
                        isSearchable, operators, depth));

        return new SearchOptions(isSearchable, operators, depth);
    }

    /**
     * Returns default operators for a field type if none are explicitly declared.
     */
    private static List<SearchOperator> getDefaultOperatorsFor(TypeMirror type, boolean isRelationship) {
        final String t = type.toString();

        if (isString(t)) {
            return DEFAULT_STRING_OPERATORS;
        }
        if (isNumeric(t)) {
            return DEFAULT_NUMERIC_OPERATORS;
        }
        if (isBoolean(t)) {
            return DEFAULT_BOOLEAN_OPERATORS;
        }
        if (isUuid(t)) {
            return DEFAULT_UUID_OPERATORS;
        }
        if (isDateTime(t)) {
            return DEFAULT_DATETIME_OPERATORS;
        }
        if (isEnum(type)) {
            return DEFAULT_ENUM_OPERATORS;
        }
        if (isRelationship) {
            return DEFAULT_RELATIONSHIP_OPERATORS;
        }
        if (isCollection(t)) {
            return DEFAULT_COLLECTION_OPERATORS;
        }
        if (isMap(t)) {
            return DEFAULT_MAP_OPERATORS;
        }

        return DEFAULT_FALLBACK_OPERATORS;
    }

    /**
     * Checks if the field is a JPA relationship (OneToOne, OneToMany, ManyToOne, ManyToMany, or Embedded).
     */
    private static boolean isRelationshipField(VariableElement field) {
        return field.getAnnotation(jakarta.persistence.OneToOne.class) != null
                || field.getAnnotation(jakarta.persistence.OneToMany.class) != null
                || field.getAnnotation(jakarta.persistence.ManyToOne.class) != null
                || field.getAnnotation(jakarta.persistence.ManyToMany.class) != null
                || field.getAnnotation(jakarta.persistence.Embedded.class) != null;
    }

    /**
     * Checks if the type is a String.
     */
    private static boolean isString(String t) {
        return t.equals("java.lang.String");
    }

    /**
     * Checks if the type is a boolean or Boolean.
     */
    private static boolean isBoolean(String t) {
        return t.equals("boolean") || t.equals("java.lang.Boolean");
    }

    /**
     * Checks if the type is a numeric type.
     */
    private static boolean isNumeric(String t) {
        return List.of(
                "int", "java.lang.Integer", "long", "java.lang.Long",
                "float", "java.lang.Float", "double", "java.lang.Double"
        ).contains(t);
    }

    /**
     * Checks if the type is a UUID.
     */
    private static boolean isUuid(String t) {
        return t.equals("java.util.UUID");
    }

    /**
     * Checks if the type is a date/time type.
     */
    private static boolean isDateTime(String t) {
        return List.of("java.time.LocalDate", "java.time.LocalDateTime", "java.time.Instant")
                .contains(t);
    }

    /**
     * Checks if the type is an enum.
     */
    private static boolean isEnum(TypeMirror type) {
        return type instanceof DeclaredType dt && dt.asElement().getKind() == ElementKind.ENUM;
    }

    /**
     * Checks if the type is a collection (List, Set, Collection).
     */
    private static boolean isCollection(String t) {
        return t.startsWith("java.util.")
                && (t.contains("List") || t.contains("Set") || t.contains("Collection"));
    }

    /**
     * Checks if the type is a Map.
     */
    private static boolean isMap(String t) {
        return t.startsWith("java.util.Map");
    }

    // Operator groups

    /**
     * Default operators for String fields.
     */
    private static final List<SearchOperator> DEFAULT_STRING_OPERATORS = List.of(
            SearchOperator.EQUALS, SearchOperator.CONTAINS, SearchOperator.STARTS_WITH,
            SearchOperator.ENDS_WITH, SearchOperator.IN, SearchOperator.NOT_EQUALS,
            SearchOperator.NOT_IN, SearchOperator.REGEX
    );

    /**
     * Default operators for numeric fields.
     */
    private static final List<SearchOperator> DEFAULT_NUMERIC_OPERATORS = List.of(
            SearchOperator.EQUALS, SearchOperator.GT, SearchOperator.LT,
            SearchOperator.GTE, SearchOperator.LTE, SearchOperator.IN,
            SearchOperator.NOT_EQUALS, SearchOperator.NOT_IN, SearchOperator.RANGE
    );

    /**
     * Default operators for boolean fields.
     */
    private static final List<SearchOperator> DEFAULT_BOOLEAN_OPERATORS = List.of(
            SearchOperator.EQUALS, SearchOperator.NOT_EQUALS
    );

    /**
     * Default operators for UUID fields.
     */
    private static final List<SearchOperator> DEFAULT_UUID_OPERATORS = List.of(
            SearchOperator.EQUALS, SearchOperator.IN,
            SearchOperator.NOT_EQUALS, SearchOperator.NOT_IN
    );

    /**
     * Default operators for date/time fields.
     */
    private static final List<SearchOperator> DEFAULT_DATETIME_OPERATORS = List.of(
            SearchOperator.EQUALS, SearchOperator.BEFORE, SearchOperator.AFTER,
            SearchOperator.BETWEEN, SearchOperator.GTE, SearchOperator.LTE,
            SearchOperator.NOT_EQUALS
    );

    /**
     * Default operators for enum fields.
     */
    private static final List<SearchOperator> DEFAULT_ENUM_OPERATORS = List.of(
            SearchOperator.EQUALS, SearchOperator.IN,
            SearchOperator.NOT_EQUALS, SearchOperator.NOT_IN
    );

    /**
     * Default operators for collection fields.
     */
    private static final List<SearchOperator> DEFAULT_COLLECTION_OPERATORS = List.of(
            SearchOperator.CONTAINS, SearchOperator.IS_EMPTY, SearchOperator.NOT_EMPTY,
            SearchOperator.SIZE_EQUALS, SearchOperator.SIZE_GT,
            SearchOperator.SIZE_LT, SearchOperator.CONTAINS_ALL
    );

    /**
     * Default operators for relationship fields (OneToOne, OneToMany, ManyToOne, ManyToMany).
     * Relationships should use EQUALS for matching by ID/reference.
     */
    private static final List<SearchOperator> DEFAULT_RELATIONSHIP_OPERATORS = List.of(
            SearchOperator.EQUALS
    );

    /**
     * Default operators for map fields.
     */
    private static final List<SearchOperator> DEFAULT_MAP_OPERATORS = List.of(
            SearchOperator.CONTAINS_KEY, SearchOperator.CONTAINS_VALUE,
            SearchOperator.SIZE_EQUALS
    );

    /**
     * Default fallback operators if no specific operators are defined.
     */
    private static final List<SearchOperator> DEFAULT_FALLBACK_OPERATORS = List.of(
            SearchOperator.EQUALS
    );
}