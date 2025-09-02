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

package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.fileheader.SearchStrictHeader;
import nl.datasteel.crudcraft.codegen.writer.search.PredicateGeneratorRegistry;
import nl.datasteel.crudcraft.codegen.writer.search.PropertySpec;
import nl.datasteel.crudcraft.codegen.writer.search.SearchField;
import nl.datasteel.crudcraft.codegen.writer.search.SearchFieldCollector;
import nl.datasteel.crudcraft.runtime.config.CrudCraftSearchProperties;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;

/**
 * Generates a SearchRequest DTO and Specification implementation for
 * each CrudCraft entity that has @Searchable fields.
 */
public class SearchGenerator implements Generator {

    /**
     * Generates the SearchRequest DTO and Specification for the given model descriptor.
     */
    @Override
    public List<JavaFile> generate(ModelDescriptor md, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(md, ctx)) {
            return List.of();
        }

        int depth = CrudCraftSearchProperties.getStaticDepth();
        List<SearchField> fields = List.copyOf(new SearchFieldCollector(ctx).collect(md, depth));

        JavaFile req = generateRequest(md, fields);
        JavaFile spec = generateSpecification(md, fields);
        return List.of(req, spec);
    }

    @Override
    public int order() {
        return 1;
    }

    /**
     * Generates a SearchRequest DTO for the given model descriptor
     * based on the provided search fields.
     *
     * @param md the model descriptor containing metadata about the entity
     * @param fields the list of search fields to include in the request
     * @return a JavaFile representing the generated SearchRequest class
     */
    private JavaFile generateRequest(ModelDescriptor md, List<SearchField> fields) {
        String pkg  = md.getPackageName() + ".search";
        String name = md.getName() + "SearchRequest";

        ClassName requestIface = ClassName.get(
                "nl.datasteel.crudcraft.runtime.search",
                "SearchRequest");
        ClassName entityCls    = ClassName.get(md.getPackageName(), md.getName());

        Map<String, Set<SearchOperator>> propOps = fields.stream()
                .collect(Collectors.toMap(
                        SearchField::property,
                        sf -> Set.of(sf.operator()),
                        (a, b) -> {
                            Set<SearchOperator> merged = new HashSet<>(a);
                            merged.addAll(b);
                            return Set.copyOf(merged);
                        },
                        LinkedHashMap::new
                ));
        Map<String, FieldDescriptor> propDesc = fields.stream()
                .collect(Collectors.toMap(
                        SearchField::property,
                        SearchField::descriptor,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        TypeSpec.Builder cls = TypeSpec.classBuilder(name)
                .addJavadoc(SearchStrictHeader.header(
                                md.getName(),
                                pkg,
                                getClass().getSimpleName())
                )
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(requestIface, entityCls))
                .addSuperinterface(ClassName.get(Serializable.class));

        List<PropertySpec> specs = propOps.entrySet().stream()
                .map(e -> new PropertySpec(propDesc.get(e.getKey()), e.getKey(), e.getValue()))
                .toList();

        specs.forEach(ps -> ps.addMembers(cls));


        ClassName specClass = ClassName.get(pkg, md.getName() + "Specification");

        // Only generate a copy constructor when there are properties to copy
        if (!specs.isEmpty()) {
            MethodSpec.Builder copyCtor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(pkg, name), "other");
            copyCtor.beginControlFlow("if (other != null)");
            specs.forEach(ps -> ps.addCopyStatements(copyCtor));
            copyCtor.endControlFlow();
            cls.addMethod(copyCtor.build());
        }

        MethodSpec.Builder toSpec = MethodSpec.methodBuilder("toSpecification")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(
                        ClassName.get("org.springframework.data.jpa.domain", "Specification"),
                        entityCls));
        if (!specs.isEmpty()) {
            toSpec.addStatement("return new $T(this)", specClass);
        } else {
            toSpec.addStatement("return new $T()", specClass);
        }
        cls.addMethod(toSpec.build());

        return JavaFile.builder(pkg, cls.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    /**
     * Generates a Specification implementation for the given model descriptor
     * based on the provided search fields.
     *
     * @param md the model descriptor containing metadata about the entity
     * @param fields the list of search fields to include in the specification
     * @return a JavaFile representing the generated specification class
     */
    private JavaFile generateSpecification(ModelDescriptor md, List<SearchField> fields) {
        String pkg = md.getPackageName() + ".search";
        String name = md.getName() + "Specification";

        ClassName specIntf = ClassName.get("org.springframework.data.jpa.domain",
                "Specification");
        ClassName rootCls = ClassName.get(md.getPackageName(), md.getName());
        ClassName reqCls = ClassName.get(pkg, md.getName() + "SearchRequest");

        TypeSpec.Builder cls = TypeSpec.classBuilder(name)
                .addJavadoc(SearchStrictHeader.header(
                        md.getName(),
                        pkg,
                        this.getClass().getSimpleName()
                ))
                .addSuperinterface(ParameterizedTypeName.get(specIntf, rootCls))
                .addModifiers(Modifier.PUBLIC);

        if (!fields.isEmpty()) {
            cls.addField(reqCls, "request", Modifier.PRIVATE, Modifier.FINAL);
            cls.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(reqCls, "request")
                    .addStatement("this.request = new $T(request)", reqCls)
                    .build());
        } else {
            cls.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .build());
        }

        ClassName cb = ClassName.get("jakarta.persistence.criteria",
                "CriteriaBuilder");
        ClassName rootRaw = ClassName.get("jakarta.persistence.criteria",
                "Root");
        ClassName queryRaw = ClassName.get("jakarta.persistence.criteria",
                "CriteriaQuery");
        ClassName predicate = ClassName.get("jakarta.persistence.criteria",
                "Predicate");
        TypeName rootTypeRaw   = ParameterizedTypeName.get(rootRaw, rootCls);
        TypeName rootType = rootTypeRaw.isPrimitive() ? rootTypeRaw.box() : rootTypeRaw;
        TypeName queryType = ParameterizedTypeName.get(
                queryRaw,
                WildcardTypeName.subtypeOf(Object.class)
        );

        MethodSpec.Builder method = MethodSpec.methodBuilder("toPredicate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(predicate)
                .addParameter(rootType, "root")
                .addParameter(queryType, "query")
                .addParameter(cb, "cb")
                .addStatement("Predicate p = cb.conjunction()");

        for (SearchField sf : fields) {
            CodeBlock block = PredicateGeneratorRegistry
                    .of(sf.operator())
                    .generate(sf);

            method.addCode(block);
        }

        method.addStatement("return p");

        cls.addMethod(method.build());

        return JavaFile.builder(pkg, cls.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }
}

