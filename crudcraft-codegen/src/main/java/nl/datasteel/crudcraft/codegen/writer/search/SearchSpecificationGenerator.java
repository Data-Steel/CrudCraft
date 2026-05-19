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
import com.palantir.javapoet.WildcardTypeName;
import java.util.List;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.fileheader.SearchStrictHeader;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;


/** Generates the Spring Data JPA specification for a searchable model. */
public final class SearchSpecificationGenerator {

    private static final String SEARCH_GENERATOR_NAME = "SearchGenerator";

    /** Creates a search specification generator. */
    public SearchSpecificationGenerator() {
        // Constructor without any parameters stays empty
    }

    /**
     * Generates a specification implementation for the given model and collected fields.
     *
     * @param md searchable model descriptor
     * @param fields collected searchable fields
     * @return generated Java source for the search specification
     */
    public JavaFile generate(ModelDescriptor md, List<SearchField> fields) {
        String pkg = md.getPackageName() + ".search";
        String name = md.getName() + "Specification";

        ClassName specIntf = ClassName.get("org.springframework.data.jpa.domain", "Specification");
        ClassName rootCls = ClassName.get(md.getPackageName(), md.getName());
        ClassName reqCls = ClassName.get(pkg, md.getName() + "SearchRequest");
        String header = SearchStrictHeader.header(md.getName(), pkg, SEARCH_GENERATOR_NAME);

        TypeSpec.Builder cls =
                TypeSpec.classBuilder(name)
                        .addJavadoc(header)
                        .addSuperinterface(ParameterizedTypeName.get(specIntf, rootCls))
                        .addModifiers(Modifier.PUBLIC);
        cls.addField(
                FieldSpec.builder(TypeName.LONG, "serialVersionUID")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("1L")
                        .build());

        addConstructor(cls, reqCls, fields);
        cls.addMethod(toPredicateMethod(rootCls, fields));

        return JavaPoetUtils.javaFile(pkg, cls.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .build();
    }

    private void addConstructor(TypeSpec.Builder cls, ClassName reqCls, List<SearchField> fields) {
        if (!fields.isEmpty()) {
            cls.addField(reqCls, "request", Modifier.PRIVATE, Modifier.FINAL);
            cls.addMethod(
                    MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(reqCls, "request")
                            .addStatement("this.request = new $T(request)", reqCls)
                            .build());
        } else {
            cls.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());
        }
    }

    private MethodSpec toPredicateMethod(ClassName rootCls, List<SearchField> fields) {
        ClassName cb = ClassName.get("jakarta.persistence.criteria", "CriteriaBuilder");
        ClassName rootRaw = ClassName.get("jakarta.persistence.criteria", "Root");
        ClassName queryRaw = ClassName.get("jakarta.persistence.criteria", "CriteriaQuery");
        ClassName predicate = ClassName.get("jakarta.persistence.criteria", "Predicate");
        TypeName rootType = ParameterizedTypeName.get(rootRaw, rootCls);
        TypeName queryType =
                ParameterizedTypeName.get(queryRaw, WildcardTypeName.subtypeOf(Object.class));

        MethodSpec.Builder method =
                MethodSpec.methodBuilder("toPredicate")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(predicate)
                        .addParameter(rootType, "root")
                        .addParameter(queryType, "query")
                        .addParameter(cb, "cb");

        addPredicateInitializer(method, fields);
        if (hasJoins(fields)) {
            method.addStatement("query.distinct(true)");
        }
        for (SearchField field : fields) {
            CodeBlock block = PredicateGeneratorRegistry.of(field.operator()).generate(field);
            method.addCode(block);
        }
        addEmptyOrFallback(method, fields);
        return method.addStatement("return p").build();
    }

    private void addPredicateInitializer(MethodSpec.Builder method, List<SearchField> fields) {
        if (!fields.isEmpty()) {
            method.addStatement(
                            "$T logic = request.getSearchLogic()",
                            ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchLogic"))
                    .addStatement(
                            "Predicate p = logic == $T.AND ? cb.conjunction() : cb.disjunction()",
                            ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchLogic"))
                    .addStatement("boolean hasCriteria = false");
        } else {
            method.addStatement("Predicate p = cb.conjunction()");
        }
    }

    private boolean hasJoins(List<SearchField> fields) {
        for (SearchField field : fields) {
            if (field.path().contains(".join(")) {
                return true;
            }
        }
        return false;
    }

    private void addEmptyOrFallback(MethodSpec.Builder method, List<SearchField> fields) {
        if (fields.isEmpty()) {
            return;
        }
        method.beginControlFlow(
                        "if (!hasCriteria && logic == $T.OR)",
                        ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchLogic"))
                .addStatement("return cb.conjunction()")
                .endControlFlow();
    }
}
