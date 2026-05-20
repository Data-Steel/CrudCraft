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
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.fileheader.SearchStrictHeader;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;


/** Generates the mutable search request DTO for a searchable model. */
public final class SearchRequestGenerator {

    private static final String SEARCH_GENERATOR_NAME = "SearchGenerator";

    /** Creates a search request generator. */
    public SearchRequestGenerator() {
        // Constructor without any parameters stays empty
    }

    /**
     * Generates a search request DTO for the given model descriptor and collected fields.
     *
     * @param md searchable model descriptor
     * @param fields collected searchable fields
     * @return generated Java source for the search request DTO
     */
    public JavaFile generate(ModelDescriptor md, List<SearchField> fields) {
        String pkg = md.getPackageName() + ".search";
        String name = md.getName() + "SearchRequest";

        ClassName requestIface =
                ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchRequest");
        ClassName entityCls = ClassName.get(md.getPackageName(), md.getName());

        Map<String, Set<SearchOperator>> propOps = new LinkedHashMap<>();
        Map<String, FieldDescriptor> propDesc = new LinkedHashMap<>();
        for (SearchField field : fields) {
            propOps.computeIfAbsent(
                            field.property(),
                            property -> {
                                property.length();
                                return EnumSet.noneOf(SearchOperator.class);
                            })
                    .add(field.operator());
            propDesc.putIfAbsent(field.property(), field.descriptor());
        }

        final ClassName searchLogicCls =
                ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchLogic");
        ClassName notThreadSafe =
                ClassName.get("nl.datasteel.crudcraft.runtime.search", "NotThreadSafe");
        ClassName suppressFbWarnings =
                ClassName.get("edu.umd.cs.findbugs.annotations", "SuppressFBWarnings");
        String header = SearchStrictHeader.header(md.getName(), pkg, SEARCH_GENERATOR_NAME);

        TypeSpec.Builder cls =
                TypeSpec.classBuilder(name)
                        .addJavadoc(header)
                        .addJavadoc(
                                "\n<p>Nested searchable paths are generated only up to the"
                                        + " configured search generation depth. Treat dotted paths"
                                        + " as an allow-list generated at compile time; requests"
                                        + " outside this list are rejected before a JPA"
                                        + " specification is built.\n"
                                        + "\n@see nl.datasteel.crudcraft.runtime.search."
                                        + "SearchPathGuard\n")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(
                                com.palantir.javapoet.AnnotationSpec.builder(
                                                SuppressWarnings.class)
                                        .addMember("value", "$S", "serial")
                                        .build())
                        .addAnnotation(notThreadSafe)
                        .addAnnotation(
                                com.palantir.javapoet.AnnotationSpec.builder(suppressFbWarnings)
                                        .addMember(
                                                "value",
                                                "{$S, $S}",
                                                "EI_EXPOSE_REP",
                                                "EI_EXPOSE_REP2")
                                        .addMember(
                                                "justification",
                                                "$S",
                                                "Spring binds generated search request collections"
                                                        + " and maps through live accessors")
                                        .build())
                        .addSuperinterface(ParameterizedTypeName.get(requestIface, entityCls))
                        .addSuperinterface(ClassName.get(Serializable.class));
        cls.addField(
                FieldSpec.builder(TypeName.LONG, "serialVersionUID")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("1L")
                        .build());

        List<PropertySpec> specs = propertySpecs(propOps, propDesc);
        for (PropertySpec spec : specs) {
            spec.addMembers(cls);
        }

        addAllowedPathFields(cls, propOps, fields);
        addSearchLogicMembers(cls, searchLogicCls);
        addRequestContractMethods(cls, requestIface, specs);
        addCopyConstructor(cls, pkg, name, specs);
        addToSpecification(cls, pkg, md, entityCls, specs);

        return JavaPoetUtils.javaFile(pkg, cls.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .build();
    }

    private List<PropertySpec> propertySpecs(
            Map<String, Set<SearchOperator>> propOps, Map<String, FieldDescriptor> propDesc) {
        List<PropertySpec> specs = new ArrayList<>();
        for (Map.Entry<String, Set<SearchOperator>> entry : propOps.entrySet()) {
            specs.add(
                    new PropertySpec(
                            propDesc.get(entry.getKey()),
                            entry.getKey(),
                            EnumSet.copyOf(entry.getValue())));
        }
        return specs;
    }

    private void addAllowedPathFields(
            TypeSpec.Builder cls,
            Map<String, Set<SearchOperator>> propOps,
            List<SearchField> fields) {
        List<CodeBlock> searchableBlocks = new ArrayList<>();
        List<CodeBlock> sortableBlocks = new ArrayList<>();
        for (Map.Entry<String, Set<SearchOperator>> entry : propOps.entrySet()) {
            String property = entry.getKey();
            searchableBlocks.add(CodeBlock.of("$S", property));
            if (isSortable(entry.getValue(), property)) {
                sortableBlocks.add(CodeBlock.of("$S", property));
            }
        }
        CodeBlock searchableSet =
                CodeBlock.of("$T.of($L)", Set.class, CodeBlock.join(searchableBlocks, ", "));
        CodeBlock sortableSet =
                CodeBlock.of("$T.of($L)", Set.class, CodeBlock.join(sortableBlocks, ", "));
        cls.addField(
                FieldSpec.builder(
                                ParameterizedTypeName.get(
                                        ClassName.get(Set.class), ClassName.get(String.class)),
                                "ALLOWED_SEARCH_PATHS",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer(searchableSet)
                        .build());
        cls.addField(
                FieldSpec.builder(
                                TypeName.INT,
                                "MAX_SEARCH_PATH_DEPTH",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$L", maxSearchPathDepth(fields))
                        .build());
        cls.addField(
                FieldSpec.builder(
                                ParameterizedTypeName.get(
                                        ClassName.get(Set.class), ClassName.get(String.class)),
                                "ALLOWED_SORT_PATHS",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$L", sortableSet)
                        .build());
        cls.addField(
                FieldSpec.builder(
                                ParameterizedTypeName.get(
                                        ClassName.get(Map.class),
                                        ClassName.get(String.class),
                                        ParameterizedTypeName.get(
                                                ClassName.get(Set.class),
                                                ClassName.get(SearchOperator.class))),
                                "ALLOWED_SEARCH_OPERATORS",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$L", buildAllowedOperatorsByPath(propOps))
                        .build());
    }

    private void addSearchLogicMembers(TypeSpec.Builder cls, ClassName searchLogicCls) {
        cls.addField(searchLogicCls, "searchLogic", Modifier.PRIVATE);
        cls.addMethod(
                MethodSpec.methodBuilder("getSearchLogic")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(searchLogicCls)
                        .addStatement(
                                "return searchLogic != null ? searchLogic : $T.OR", searchLogicCls)
                        .build());
        cls.addMethod(
                MethodSpec.methodBuilder("setSearchLogic")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(searchLogicCls, "searchLogic")
                        .addStatement("this.searchLogic = searchLogic")
                        .build());
    }

    private void addRequestContractMethods(
            TypeSpec.Builder cls, ClassName requestIface, List<PropertySpec> specs) {
        final ClassName searchPathGuard =
                ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchPathGuard");
        cls.addMethod(
                MethodSpec.methodBuilder("allowedSortPaths")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(
                                ParameterizedTypeName.get(
                                        ClassName.get(Set.class), ClassName.get(String.class)))
                        .addStatement("return ALLOWED_SORT_PATHS")
                        .build());
        cls.addMethod(
                MethodSpec.methodBuilder("allowedSearchPaths")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(
                                ParameterizedTypeName.get(
                                        ClassName.get(Set.class), ClassName.get(String.class)))
                        .addStatement("return ALLOWED_SEARCH_PATHS")
                        .build());
        cls.addMethod(
                MethodSpec.methodBuilder("allowedSearchOperators")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(
                                ParameterizedTypeName.get(
                                        ClassName.get(Map.class),
                                        ClassName.get(String.class),
                                        ParameterizedTypeName.get(
                                                ClassName.get(Set.class),
                                                ClassName.get(SearchOperator.class))))
                        .addStatement("return $T.copyOf(ALLOWED_SEARCH_OPERATORS)", Map.class)
                        .build());
        cls.addMethod(buildRequestedCriteriaMethod(specs, requestIface));
        cls.addMethod(
                MethodSpec.methodBuilder("maxSearchPathDepth")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.INT)
                        .addStatement("return MAX_SEARCH_PATH_DEPTH")
                        .build());
        MethodSpec.Builder validate =
                MethodSpec.methodBuilder("validate")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC);
        validate.beginControlFlow(
                        "for ($T criterion : requestedSearchCriteria())",
                        requestIface.nestedClass("SearchCriterion"))
                .beginControlFlow("if (criterion != null)")
                .addStatement(
                        "$T.enforceMaxDepth(criterion.path(), MAX_SEARCH_PATH_DEPTH)",
                        searchPathGuard)
                .endControlFlow()
                .endControlFlow()
                .addStatement("$T.super.validate()", requestIface);
        cls.addMethod(validate.build());
    }

    private void addCopyConstructor(
            TypeSpec.Builder cls, String pkg, String name, List<PropertySpec> specs) {
        cls.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());
        if (specs.isEmpty()) {
            return;
        }
        MethodSpec.Builder copyCtor =
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(pkg, name), "other");
        copyCtor.beginControlFlow("if (other != null)");
        for (PropertySpec spec : specs) {
            for (String field : spec.copyFieldNames()) {
                copyCtor.addStatement("this.$L = other.$L", field, field);
            }
        }
        copyCtor.addStatement("this.searchLogic = other.searchLogic");
        copyCtor.endControlFlow();
        cls.addMethod(copyCtor.build());
    }

    private void addToSpecification(
            TypeSpec.Builder cls,
            String pkg,
            ModelDescriptor md,
            ClassName entityCls,
            List<PropertySpec> specs) {
        ClassName specClass = ClassName.get(pkg, md.getName() + "Specification");
        MethodSpec.Builder toSpec =
                MethodSpec.methodBuilder("toSpecification")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(
                                ParameterizedTypeName.get(
                                        ClassName.get(
                                                "org.springframework.data.jpa.domain",
                                                "Specification"),
                                        entityCls));
        toSpec.addStatement("validate()");
        if (!specs.isEmpty()) {
            toSpec.addStatement("return new $T(this)", specClass);
        } else {
            toSpec.addStatement("return new $T()", specClass);
        }
        cls.addMethod(toSpec.build());
    }

    private MethodSpec buildRequestedCriteriaMethod(
            List<PropertySpec> specs, ClassName requestIface) {
        ClassName arrayList = ClassName.get(ArrayList.class);
        ParameterizedTypeName listType =
                ParameterizedTypeName.get(
                        ClassName.get(List.class), requestIface.nestedClass("SearchCriterion"));

        MethodSpec.Builder method =
                MethodSpec.methodBuilder("requestedSearchCriteria")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(listType)
                        .addStatement("$T criteria = new $T<>()", listType, arrayList);

        for (PropertySpec spec : specs) {
            String property = spec.name();
            String propertyUpper = capitalize(property);
            List<String> checks = requestedCriteriaChecks(spec, propertyUpper);
            if (checks.isEmpty()) {
                continue;
            }
            method.beginControlFlow("if ($L)", String.join(" || ", checks))
                    .addStatement(
                            "criteria.add(new $T($S, get$LOp()))",
                            requestIface.nestedClass("SearchCriterion"),
                            property,
                            propertyUpper)
                    .endControlFlow();
        }

        return method.addStatement("return criteria").build();
    }

    private List<String> requestedCriteriaChecks(PropertySpec spec, String propertyUpper) {
        List<String> checks = new ArrayList<>();
        boolean hasValueOperator = false;
        boolean hasRangeOperator = false;
        for (SearchOperator operator : spec.operators()) {
            hasValueOperator =
                    hasValueOperator
                            || OperatorSpecRegistry.isValueOperator(operator)
                            || OperatorSpecRegistry.isSizeOperator(operator);
            hasRangeOperator =
                    hasRangeOperator || OperatorSpecRegistry.isRangeOperator(operator);
        }
        if (hasValueOperator) {
            checks.add("get" + propertyUpper + "() != null");
        }
        if (hasRangeOperator) {
            checks.add("get" + propertyUpper + "Start() != null");
            checks.add("get" + propertyUpper + "End() != null");
        }
        return checks;
    }

    private int maxSearchPathDepth(List<SearchField> fields) {
        int maxDepth = 0;
        for (SearchField field : fields) {
            maxDepth = Math.max(maxDepth, criteriaPathDepth(field.path()));
        }
        return maxDepth;
    }

    private int criteriaPathDepth(String path) {
        if (path == null || path.isBlank()) {
            return 0;
        }
        int depth = countOccurrences(path, ".get(");
        depth += countOccurrences(path, ".join(");
        depth += countOccurrences(path, ".joinMap(");
        return Math.max(depth, 1);
    }

    private int countOccurrences(String value, String pattern) {
        int originalLength = value.length();
        int withoutPatternLength = value.replace(pattern, "").length();
        return (originalLength - withoutPatternLength) / pattern.length();
    }

    private CodeBlock buildAllowedOperatorsByPath(
            Map<String, Set<SearchOperator>> operatorsByPath) {
        if (operatorsByPath.isEmpty()) {
            return CodeBlock.of("$T.of()", Map.class);
        }
        List<CodeBlock> entries = new ArrayList<>();
        for (Map.Entry<String, Set<SearchOperator>> entry : operatorsByPath.entrySet()) {
            List<CodeBlock> ops = new ArrayList<>();
            for (SearchOperator operator : entry.getValue()) {
                ops.add(CodeBlock.of("$T.$L", SearchOperator.class, operator.name()));
            }
            CodeBlock opSet = CodeBlock.of("$T.of($L)", Set.class, CodeBlock.join(ops, ", "));
            entries.add(CodeBlock.of("$T.entry($S, $L)", Map.class, entry.getKey(), opSet));
        }
        return CodeBlock.of("$T.ofEntries($L)", Map.class, CodeBlock.join(entries, ", "));
    }

    private boolean isSortable(Set<SearchOperator> operators, String property) {
        if (property.endsWith("Size")) {
            return false;
        }
        for (SearchOperator operator : operators) {
            if (supportsSorting(operator)) {
                return true;
            }
        }
        return false;
    }

    private boolean supportsSorting(SearchOperator operator) {
        return switch (operator) {
            case EQUALS,
                    NOT_EQUALS,
                    RANGE,
                    BETWEEN,
                    GT,
                    GTE,
                    LT,
                    LTE,
                    BEFORE,
                    AFTER,
                    CONTAINS,
                    STARTS_WITH,
                    ENDS_WITH,
                    REGEX,
                    IN,
                    NOT_IN -> true;
            default -> false;
        };
    }

    private String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
