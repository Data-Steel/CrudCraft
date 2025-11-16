package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import nl.datasteel.crudcraft.annotations.SearchOperator;

/**
 * Generates predicates for the {@code ENDS_WITH} operator.
 */
public class EndsWithPredicateGenerator
        extends AbstractPredicateGenerator
        implements PredicateGenerator {

    @Override
    public CodeBlock generate(SearchField f) {
        String m = cap(f.property());
        ClassName predicate = ClassName.get("jakarta.persistence.criteria", "Predicate");
        return CodeBlock.builder()
                .beginControlFlow(
                        "if (request.get$L() != null && !request.get$L().isEmpty() && request.get$LOp() == $T.ENDS_WITH)",
                        m,
                        m,
                        m,
                        SearchOperator.class
                )
                .addStatement("$T[] predicates = new $T[request.get$L().size()]",
                        predicate,
                        predicate,
                        m)
                .addStatement("int i = 0")
                .beginControlFlow("for (String value : request.get$L())", m)
                .addStatement("predicates[i++] = cb.like($L, \"%\" + value)",
                        f.path())
                .endControlFlow()
                .addStatement("p = cb.and(p, cb.or(predicates))")
                .endControlFlow()
                .build();
    }
}
