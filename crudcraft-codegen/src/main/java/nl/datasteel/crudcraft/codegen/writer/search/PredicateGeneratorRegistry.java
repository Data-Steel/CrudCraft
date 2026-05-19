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
import com.palantir.javapoet.CodeBlock;
import java.util.EnumMap;
import java.util.ServiceLoader;
import nl.datasteel.crudcraft.annotations.SearchOperator;


/**
 * Registry for predicate generators that maps search operators to their corresponding predicate
 * generator implementations. This allows for dynamic retrieval of the appropriate generator based
 * on the search operator used in a search request.
 *
 * <p>Built-in mappings include scalar comparisons ({@code EQUALS -> cb.equal},
 * {@code NOT_EQUALS -> cb.notEqual}), string predicates ({@code CONTAINS}/{@code STARTS_WITH}/
 * {@code ENDS_WITH -> cb.like}), comparable bounds ({@code GT}/{@code GTE}/{@code LT}/{@code LTE}),
 * range operators ({@code RANGE}, {@code BETWEEN}, {@code BEFORE}, {@code AFTER}), membership
 * operators ({@code IN}, {@code NOT_IN}), collection/map containment, and size/emptiness
 * predicates. Custom operators can be added by exposing a {@link PredicateGeneratorProvider}
 * through {@link ServiceLoader}; provider mappings replace existing mappings for the same
 * operator.
 */
public class PredicateGeneratorRegistry {

    private static final ClassName SEARCH_LOGIC =
            ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchLogic");
    private static final ClassName PREDICATE =
            ClassName.get("jakarta.persistence.criteria", "Predicate");
    private static final ClassName JOIN_TYPE =
            ClassName.get("jakarta.persistence.criteria", "JoinType");

    /**
     * Private constructor to prevent instantiation of the registry. This class is designed to be a
     * utility class
     */
    private PredicateGeneratorRegistry() {}

    /**
     * A map that associates each {@link SearchOperator} with its corresponding {@link
     * PredicateGenerator} implementation. This allows for quick lookup of the appropriate generator
     * based on the operator used in a search request.
     */
    private static final EnumMap<SearchOperator, PredicateGenerator> MAP =
            new EnumMap<>(SearchOperator.class);

    static {
        MAP.put(SearchOperator.EQUALS, PredicateGeneratorRegistry::equalsPredicate);
        MAP.put(SearchOperator.NOT_EQUALS, PredicateGeneratorRegistry::notEqualsPredicate);
        MAP.put(SearchOperator.CONTAINS, PredicateGeneratorRegistry::containsPredicate);
        MAP.put(SearchOperator.STARTS_WITH, PredicateGeneratorRegistry::startsWithPredicate);
        MAP.put(SearchOperator.ENDS_WITH, PredicateGeneratorRegistry::endsWithPredicate);
        MAP.put(SearchOperator.IN, PredicateGeneratorRegistry::inPredicate);
        MAP.put(SearchOperator.NOT_IN, PredicateGeneratorRegistry::notInPredicate);
        MAP.put(SearchOperator.REGEX, PredicateGeneratorRegistry::regexPredicate);
        MAP.put(SearchOperator.GT, PredicateGeneratorRegistry::greaterThanPredicate);
        MAP.put(SearchOperator.GTE, PredicateGeneratorRegistry::greaterThanOrEqualPredicate);
        MAP.put(SearchOperator.LT, PredicateGeneratorRegistry::lessThanPredicate);
        MAP.put(SearchOperator.LTE, PredicateGeneratorRegistry::lessThanOrEqualPredicate);
        MAP.put(SearchOperator.RANGE, PredicateGeneratorRegistry::rangePredicate);
        MAP.put(SearchOperator.BEFORE, PredicateGeneratorRegistry::beforePredicate);
        MAP.put(SearchOperator.AFTER, PredicateGeneratorRegistry::afterPredicate);
        MAP.put(SearchOperator.BETWEEN, PredicateGeneratorRegistry::betweenPredicate);
        MAP.put(SearchOperator.IS_EMPTY, PredicateGeneratorRegistry::isEmptyPredicate);
        MAP.put(SearchOperator.NOT_EMPTY, PredicateGeneratorRegistry::notEmptyPredicate);
        MAP.put(SearchOperator.SIZE_EQUALS, PredicateGeneratorRegistry::sizeEqualsPredicate);
        MAP.put(SearchOperator.SIZE_GT, PredicateGeneratorRegistry::sizeGtPredicate);
        MAP.put(SearchOperator.SIZE_LT, PredicateGeneratorRegistry::sizeLtPredicate);
        MAP.put(SearchOperator.CONTAINS_ALL, PredicateGeneratorRegistry::containsAllPredicate);
        MAP.put(SearchOperator.CONTAINS_KEY, PredicateGeneratorRegistry::containsKeyPredicate);
        MAP.put(SearchOperator.CONTAINS_VALUE, PredicateGeneratorRegistry::containsValuePredicate);
        ServiceLoader.load(PredicateGeneratorProvider.class)
                .forEach(provider -> MAP.putAll(provider.generators()));
    }

    /**
     * Retrieves the appropriate {@link PredicateGenerator} for the given {@link SearchOperator}.
     * This method allows for dynamic selection of the generator based on the operator used in a
     * search request.
     *
     * @param op the search operator for which to retrieve the predicate generator
     * @return the corresponding predicate generator, or null if no generator is found for the
     *     operator
     */
    public static PredicateGenerator of(SearchOperator op) {
        return MAP.get(op);
    }

    private static CodeBlock equalsPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.EQUALS,
                CodeBlock.of("$L.in(request.get$L())", field.path(), methodSuffix(field)));
    }

    private static CodeBlock notEqualsPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.NOT_EQUALS,
                CodeBlock.of("cb.not($L.in(request.get$L()))", field.path(), methodSuffix(field)));
    }

    private static CodeBlock containsPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.CONTAINS,
                CodeBlock.of(
                        "cb.like($L, \"%\" + request.get$L() + \"%\")",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock startsWithPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.STARTS_WITH,
                CodeBlock.of(
                        "cb.like($L, request.get$L() + \"%\")",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock endsWithPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.ENDS_WITH,
                CodeBlock.of(
                        "cb.like($L, \"%\" + request.get$L())",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock inPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.IN,
                CodeBlock.of("$L.in(request.get$L())", field.path(), methodSuffix(field)));
    }

    private static CodeBlock notInPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.NOT_IN,
                CodeBlock.of("cb.not($L.in(request.get$L()))", field.path(), methodSuffix(field)));
    }

    private static CodeBlock regexPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.REGEX,
                CodeBlock.of(
                        "cb.like($L, request.get$L())", field.path(), methodSuffix(field)));
    }

    private static CodeBlock greaterThanPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.GT,
                CodeBlock.of(
                        "cb.greaterThan($L, request.get$L())",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock greaterThanOrEqualPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.GTE,
                CodeBlock.of(
                        "cb.greaterThanOrEqualTo($L, request.get$L())",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock lessThanPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.LT,
                CodeBlock.of(
                        "cb.lessThan($L, request.get$L())", field.path(), methodSuffix(field)));
    }

    private static CodeBlock lessThanOrEqualPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.LTE,
                CodeBlock.of(
                        "cb.lessThanOrEqualTo($L, request.get$L())",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock rangePredicate(SearchField field) {
        return boundedPredicate(field, SearchOperator.RANGE);
    }

    private static CodeBlock beforePredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.BEFORE,
                CodeBlock.of(
                        "cb.lessThan($L, request.get$L())", field.path(), methodSuffix(field)));
    }

    private static CodeBlock afterPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.AFTER,
                CodeBlock.of(
                        "cb.greaterThan($L, request.get$L())",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock betweenPredicate(SearchField field) {
        return boundedPredicate(field, SearchOperator.BETWEEN);
    }

    private static CodeBlock isEmptyPredicate(SearchField field) {
        return operatorOnlyPredicate(
                field, SearchOperator.IS_EMPTY, CodeBlock.of("cb.isEmpty($L)", field.path()));
    }

    private static CodeBlock notEmptyPredicate(SearchField field) {
        return operatorOnlyPredicate(
                field, SearchOperator.NOT_EMPTY, CodeBlock.of("cb.isNotEmpty($L)", field.path()));
    }

    private static CodeBlock sizeEqualsPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.SIZE_EQUALS,
                CodeBlock.of(
                        "cb.equal(cb.size($L), request.get$L())",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock sizeGtPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.SIZE_GT,
                CodeBlock.of(
                        "cb.greaterThan(cb.size($L), request.get$L())",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock sizeLtPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.SIZE_LT,
                CodeBlock.of(
                        "cb.lessThan(cb.size($L), request.get$L())",
                        field.path(),
                        methodSuffix(field)));
    }

    private static CodeBlock containsAllPredicate(SearchField field) {
        String method = methodSuffix(field);
        return CodeBlock.builder()
                .beginControlFlow(
                        "if (request.get$L() != null && !request.get$L().isEmpty() "
                                + "&& request.get$LOp() == $T.$L)",
                        method,
                        method,
                        method,
                        SearchOperator.class,
                        SearchOperator.CONTAINS_ALL.name())
                .addStatement("$T innerPredicate = cb.conjunction()", PREDICATE)
                .beginControlFlow("for (var item : request.get$L())", method)
                .addStatement("innerPredicate = cb.and(innerPredicate, cb.isMember(item, $L))",
                        field.path())
                .endControlFlow()
                .addStatement(
                        "p = logic == $T.AND ? cb.and(p, innerPredicate) : cb.or(p,"
                                + " innerPredicate)",
                        SEARCH_LOGIC)
                .addStatement("hasCriteria = true")
                .endControlFlow()
                .build();
    }

    private static CodeBlock containsKeyPredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.CONTAINS_KEY,
                CodeBlock.of(
                        "$L.key().in($L)",
                        mapJoin(field),
                        mapRequestProjection(field, "keySet")));
    }

    private static CodeBlock mapJoin(SearchField field) {
        String path = field.path();
        int getterStart = path.lastIndexOf(".get(\"");
        if (getterStart < 0 || !path.endsWith("\")")) {
            return CodeBlock.of("$L", path);
        }
        String parent = path.substring(0, getterStart);
        String mapField = path.substring(getterStart + 6, path.length() - 2);
        return CodeBlock.of("$L.joinMap($S, $T.LEFT)", parent, mapField, JOIN_TYPE);
    }

    private static CodeBlock containsValuePredicate(SearchField field) {
        return valuePredicate(
                field,
                SearchOperator.CONTAINS_VALUE,
                CodeBlock.of(
                        "$L.value().in($L)",
                        mapJoin(field),
                        mapRequestProjection(field, "values")));
    }

    private static CodeBlock mapRequestProjection(SearchField field, String accessor) {
        if (!hasMapRequestType(field)) {
            return CodeBlock.of("request.get$L()", methodSuffix(field));
        }
        return CodeBlock.of("request.get$L().$L()", methodSuffix(field), accessor);
    }

    private static boolean hasMapRequestType(SearchField field) {
        if (field.descriptor() == null || field.descriptor().getType() == null) {
            return false;
        }
        return field.descriptor().getType().toString().startsWith("java.util.Map");
    }

    private static CodeBlock valuePredicate(
            SearchField field, SearchOperator operator, CodeBlock predicate) {
        String method = methodSuffix(field);
        return CodeBlock.builder()
                .beginControlFlow(
                        "if (request.get$L() != null && request.get$LOp() == $T.$L)",
                        method,
                        method,
                        SearchOperator.class,
                        operator.name())
                .addStatement(
                        "p = logic == $T.AND ? cb.and(p, $L) : cb.or(p, $L)",
                        SEARCH_LOGIC,
                        predicate,
                        predicate)
                .addStatement("hasCriteria = true")
                .endControlFlow()
                .build();
    }

    private static CodeBlock operatorOnlyPredicate(
            SearchField field, SearchOperator operator, CodeBlock predicate) {
        String method = methodSuffix(field);
        return CodeBlock.builder()
                .beginControlFlow(
                        "if (request.get$LOp() == $T.$L)",
                        method,
                        SearchOperator.class,
                        operator.name())
                .addStatement(
                        "p = logic == $T.AND ? cb.and(p, $L) : cb.or(p, $L)",
                        SEARCH_LOGIC,
                        predicate,
                        predicate)
                .addStatement("hasCriteria = true")
                .endControlFlow()
                .build();
    }

    private static CodeBlock boundedPredicate(SearchField field, SearchOperator operator) {
        String method = methodSuffix(field);
        CodeBlock predicate =
                CodeBlock.of(
                        "cb.between($L, request.get$LStart(), request.get$LEnd())",
                        field.path(),
                        method,
                        method);
        return CodeBlock.builder()
                .beginControlFlow(
                        "if (request.get$LStart() != null && request.get$LEnd() != null "
                                + "&& request.get$LOp() == $T.$L)",
                        method,
                        method,
                        method,
                        SearchOperator.class,
                        operator.name())
                .addStatement(
                        "p = logic == $T.AND ? cb.and(p, $L) : cb.or(p, $L)",
                        SEARCH_LOGIC,
                        predicate,
                        predicate)
                .addStatement("hasCriteria = true")
                .endControlFlow()
                .build();
    }

    private static String methodSuffix(SearchField field) {
        String property = field.property();
        if (property == null || property.isEmpty()) {
            return property;
        }
        return Character.toUpperCase(property.charAt(0)) + property.substring(1);
    }
}
