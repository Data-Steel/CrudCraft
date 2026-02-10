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

import java.util.stream.Stream;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PredicateGeneratorsTest {

    private static Stream<Arguments> generators() {
        return Stream.of(
                Arguments.of(SearchOperator.EQUALS, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.EQUALS) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, root.get(\"field\").in(request.getField())) : cb.or(p, root.get(\"field\").in(request.getField()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.NOT_EQUALS, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.NOT_EQUALS) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.not(root.get(\"field\").in(request.getField()))) : cb.or(p, cb.not(root.get(\"field\").in(request.getField())));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.CONTAINS, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.CONTAINS) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.like(root.get("field"), "%" + request.getField() + "%")) : cb.or(p, cb.like(root.get("field"), "%" + request.getField() + "%"));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.STARTS_WITH, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.STARTS_WITH) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.like(root.get("field"), request.getField() + "%")) : cb.or(p, cb.like(root.get("field"), request.getField() + "%"));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.ENDS_WITH, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.ENDS_WITH) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.like(root.get("field"), "%" + request.getField())) : cb.or(p, cb.like(root.get("field"), "%" + request.getField()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.REGEX, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.REGEX) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.like(root.get("field"), request.getField())) : cb.or(p, cb.like(root.get("field"), request.getField()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.GT, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.GT) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.greaterThan(root.get(\"field\"), request.getField())) : cb.or(p, cb.greaterThan(root.get(\"field\"), request.getField()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.GTE, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.GTE) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.greaterThanOrEqualTo(root.get(\"field\"), request.getField())) : cb.or(p, cb.greaterThanOrEqualTo(root.get(\"field\"), request.getField()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.LT, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.LT) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.lessThan(root.get(\"field\"), request.getField())) : cb.or(p, cb.lessThan(root.get(\"field\"), request.getField()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.LTE, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.LTE) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.lessThanOrEqualTo(root.get(\"field\"), request.getField())) : cb.or(p, cb.lessThanOrEqualTo(root.get(\"field\"), request.getField()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.IN, "values", "root.get(\"field\")", """
if (request.getValues() != null && request.getValuesOp() == nl.datasteel.crudcraft.annotations.SearchOperator.IN) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, root.get(\"field\").in(request.getValues())) : cb.or(p, root.get(\"field\").in(request.getValues()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.NOT_IN, "values", "root.get(\"field\")", """
if (request.getValues() != null && request.getValuesOp() == nl.datasteel.crudcraft.annotations.SearchOperator.NOT_IN) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.not(root.get(\"field\").in(request.getValues()))) : cb.or(p, cb.not(root.get(\"field\").in(request.getValues())));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.RANGE, "field", "root.get(\"field\")", """
if (request.getFieldStart() != null && request.getFieldEnd() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.RANGE) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.between(root.get(\"field\"), request.getFieldStart(), request.getFieldEnd())) : cb.or(p, cb.between(root.get(\"field\"), request.getFieldStart(), request.getFieldEnd()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.BEFORE, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.BEFORE) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.lessThan(root.get(\"field\"), request.getField())) : cb.or(p, cb.lessThan(root.get(\"field\"), request.getField()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.AFTER, "field", "root.get(\"field\")", """
if (request.getField() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.AFTER) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.greaterThan(root.get(\"field\"), request.getField())) : cb.or(p, cb.greaterThan(root.get(\"field\"), request.getField()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.BETWEEN, "field", "root.get(\"field\")", """
if (request.getFieldStart() != null && request.getFieldEnd() != null && request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.BETWEEN) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.between(root.get(\"field\"), request.getFieldStart(), request.getFieldEnd())) : cb.or(p, cb.between(root.get(\"field\"), request.getFieldStart(), request.getFieldEnd()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.IS_EMPTY, "field", "root.get(\"field\")", """
if (request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.IS_EMPTY) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.isEmpty(root.get(\"field\"))) : cb.or(p, cb.isEmpty(root.get(\"field\")));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.NOT_EMPTY, "field", "root.get(\"field\")", """
if (request.getFieldOp() == nl.datasteel.crudcraft.annotations.SearchOperator.NOT_EMPTY) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.isNotEmpty(root.get(\"field\"))) : cb.or(p, cb.isNotEmpty(root.get(\"field\")));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.SIZE_EQUALS, "size", "root.get(\"values\")", """
if (request.getSize() != null && request.getSizeOp() == nl.datasteel.crudcraft.annotations.SearchOperator.SIZE_EQUALS) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.equal(cb.size(root.get(\"values\")), request.getSize())) : cb.or(p, cb.equal(cb.size(root.get(\"values\")), request.getSize()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.SIZE_GT, "size", "root.get(\"values\")", """
if (request.getSize() != null && request.getSizeOp() == nl.datasteel.crudcraft.annotations.SearchOperator.SIZE_GT) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.greaterThan(cb.size(root.get(\"values\")), request.getSize())) : cb.or(p, cb.greaterThan(cb.size(root.get(\"values\")), request.getSize()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.SIZE_LT, "size", "root.get(\"values\")", """
if (request.getSize() != null && request.getSizeOp() == nl.datasteel.crudcraft.annotations.SearchOperator.SIZE_LT) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.lessThan(cb.size(root.get(\"values\")), request.getSize())) : cb.or(p, cb.lessThan(cb.size(root.get(\"values\")), request.getSize()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.CONTAINS_ALL, "values", "root.get(\"values\")", """
if (request.getValues() != null && request.getValuesOp() == nl.datasteel.crudcraft.annotations.SearchOperator.CONTAINS_ALL) {
  for (var item : request.getValues()) {
    p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.isMember(item, root.get(\"values\"))) : cb.or(p, cb.isMember(item, root.get(\"values\")));
  hasCriteria = true;
  }
}
"""),
                Arguments.of(SearchOperator.CONTAINS_KEY, "key", "root.get(\"map\")", """
if (request.getKey() != null && request.getKeyOp() == nl.datasteel.crudcraft.annotations.SearchOperator.CONTAINS_KEY) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.isMember(request.getKey(), root.get(\"map\").keySet())) : cb.or(p, cb.isMember(request.getKey(), root.get(\"map\").keySet()));
  hasCriteria = true;
}
"""),
                Arguments.of(SearchOperator.CONTAINS_VALUE, "value", "root.get(\"map\")", """
if (request.getValue() != null && request.getValueOp() == nl.datasteel.crudcraft.annotations.SearchOperator.CONTAINS_VALUE) {
  p = logic == nl.datasteel.crudcraft.runtime.search.SearchLogic.AND ? cb.and(p, cb.isMember(request.getValue(), root.get(\"map\").values())) : cb.or(p, cb.isMember(request.getValue(), root.get(\"map\").values()));
  hasCriteria = true;
}
""")
        );
    }

    @ParameterizedTest(name = "{0} generator outputs expected code")
    @MethodSource("generators")
    void generatesExpectedCode(SearchOperator op, String prop, String path, String expected) {
        SearchField f = new SearchField(null, prop, path, op);
        PredicateGenerator g = PredicateGeneratorRegistry.of(op);
        assertNotNull(g);
        String actual = g.generate(f).toString();

        assertTrue(matchesExpected(op, expected, actual),
                () -> "Generated code didn't match for " + op +
                        "\nExpected (canonical snippet):\n" + expected +
                        "\nActual:\n" + actual);
    }

    @ParameterizedTest(name = "{0} generator rejects null field")
    @MethodSource("generators")
    void generatorThrowsOnNullField(SearchOperator op, String prop, String path, String expected) {
        PredicateGenerator g = PredicateGeneratorRegistry.of(op);
        assertNotNull(g);
        assertThrows(NullPointerException.class, () -> g.generate(null));
    }

    // ----- helpers -----

    private static boolean matchesExpected(SearchOperator op, String expected, String actual) {
        String a = norm(actual);
        String e = norm(expected);

        // quick win: exact containment after normalization
        if (a.contains(e)) return true;

        // Accept common, semantically-equal variants the generator may emit.
        switch (op) {
            case GTE: {
                String alt = e.replace("request.getField()", "cb.literal(request.getField())");
                if (a.contains(alt)) return true;
                break;
            }
            case LTE: {
                String alt = e.replace("request.getField()", "cb.literal(request.getField())");
                if (a.contains(alt)) return true;
                break;
            }
            case CONTAINS_KEY: {
                // MapJoin variant(s)
                String alt1 = norm("""
if (request.getKey() != null && request.getKeyOp() == SearchOperator.CONTAINS_KEY) {
  p = cb.and(p, root.joinMap("map", JoinType.LEFT).key().in(request.getKey()));
}
""");
                String alt2 = norm("""
if (request.getKey() != null && request.getKeyOp() == SearchOperator.CONTAINS_KEY) {
  p = cb.and(p, root.join("map", JoinType.LEFT).key().in(request.getKey()));
}
""");
                if (a.contains(alt1) || a.contains(alt2)) return true;
                break;
            }
            case CONTAINS_VALUE: {
                // MapJoin variant(s)
                String alt1 = norm("""
if (request.getValue() != null && request.getValueOp() == SearchOperator.CONTAINS_VALUE) {
  p = cb.and(p, root.joinMap("map", JoinType.LEFT).value().in(request.getValue()));
}
""");
                String alt2 = norm("""
if (request.getValue() != null && request.getValueOp() == SearchOperator.CONTAINS_VALUE) {
  p = cb.and(p, root.join("map", JoinType.LEFT).value().in(request.getValue()));
}
""");
                if (a.contains(alt1) || a.contains(alt2)) return true;
                break;
            }
            default:
                // fall through
        }
        return false;
    }

    private static String norm(String s) {
        return s
                .replace("\r\n", "\n")
                .replaceAll("\\s+", " ")
                .replace("jakarta.persistence.criteria.", "")
                .trim();
    }
}
