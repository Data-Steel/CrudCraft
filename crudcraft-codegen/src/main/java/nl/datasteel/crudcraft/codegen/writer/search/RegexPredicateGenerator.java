package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import nl.datasteel.crudcraft.annotations.SearchOperator;

/**
 * Generates a predicate for checking if a string field matches a regular expression
 * specified in a search request. This is used to filter results based on regex patterns.
 */
public class RegexPredicateGenerator
        extends AbstractPredicateGenerator
        implements PredicateGenerator {

    /**
     * Generates a CodeBlock that checks if the search request's property
     * matches a regular expression.
     *
     * @param f the search field containing the property and operator information
     * @return a CodeBlock representing the predicate logic for the regex condition
     */
    @Override
    public CodeBlock generate(SearchField f) {
        String m = cap(f.property());
        ClassName predicate = ClassName.get("jakarta.persistence.criteria", "Predicate");
        return CodeBlock.builder()
                .beginControlFlow(
                        "if (request.get$L() != null && !request.get$L().isEmpty() && request.get$LOp() "
                                + "== $T.REGEX)",
                        m,
                        m,
                        m,
                        SearchOperator.class)
                .addStatement("$T[] predicates = new $T[request.get$L().size()]",
                        predicate,
                        predicate,
                        m)
                .addStatement("int i = 0")
                .beginControlFlow("for (String value : request.get$L())", m)
                .addStatement("predicates[i++] = cb.like($L, value)",
                        f.path())
                .endControlFlow()
                .addStatement("p = cb.and(p, cb.or(predicates))")
                .endControlFlow()
                .build();
    }
}
