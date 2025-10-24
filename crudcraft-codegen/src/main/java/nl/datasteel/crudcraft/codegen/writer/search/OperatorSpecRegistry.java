package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import nl.datasteel.crudcraft.annotations.SearchOperator;

/**
 * Central registry for how SearchRequest fields are generated per operator family.
 * IMPORTANT: Always construct types via JavaPoet ClassName/ParameterizedTypeName
 * so imports (e.g. java.util.Set, java.time.Instant) are emitted correctly.
 */
public final class OperatorSpecRegistry {

    // TODO: NOT EVERY OPERATOR IS IN HERE YET

    private static final EnumSet<SearchOperator> VALUE_OPS = EnumSet.of(
            SearchOperator.EQUALS,
            SearchOperator.NOT_EQUALS,
            SearchOperator.IN,
            SearchOperator.NOT_IN,
            SearchOperator.IS_EMPTY,
            SearchOperator.NOT_EMPTY,
            SearchOperator.STARTS_WITH,
            SearchOperator.ENDS_WITH,
            SearchOperator.CONTAINS
    );

    private static final EnumSet<SearchOperator> RANGE_OPS = EnumSet.of(
            SearchOperator.GT,
            SearchOperator.GTE,
            SearchOperator.LT,
            SearchOperator.LTE,
            SearchOperator.BETWEEN
    );

    private static final EnumSet<SearchOperator> SIZE_OPS = EnumSet.of(
            SearchOperator.SIZE_EQUALS,
            SearchOperator.SIZE_GT,
            SearchOperator.SIZE_LT
    );

    private static final ValueSpec VALUE_SPEC = new ValueSpec();
    private static final RangeSpec RANGE_SPEC = new RangeSpec();
    private static final SizeSpec  SIZE_SPEC  = new SizeSpec();

    private OperatorSpecRegistry() {}

    public static boolean isValueOperator(SearchOperator op) { return VALUE_OPS.contains(op); }
    public static boolean isRangeOperator(SearchOperator op) { return RANGE_OPS.contains(op); }
    public static boolean isSizeOperator(SearchOperator op)  { return SIZE_OPS.contains(op); }

    public static ValueSpec value() { return VALUE_SPEC; }
    public static RangeSpec range() { return RANGE_SPEC; }
    public static SizeSpec size()   { return SIZE_SPEC; }

    // ────────────────────────────────────────────────────────────────────────
    // Implementations
    // ────────────────────────────────────────────────────────────────────────

    /**
     * VALUE operators:
     * - we model ze als "Set<T> name" zodat IN/NOT_IN mooi werken en
     *   EQ/NE/Lx ook simpel 1 waarde kunnen dragen (size 0/1/n).
     */
    public static final class ValueSpec {
        private ValueSpec() {}

        public void addFields(TypeSpec.Builder cls, String name, TypeName elementType) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(name);
            Objects.requireNonNull(elementType);

            // import-safe raw type for Set
            ClassName setRaw = ClassName.get(Set.class);
            // elementType komt van buiten (al gemapt/boxed), dus direct bruikbaar
            ParameterizedTypeName fieldType = ParameterizedTypeName.get(setRaw, elementType);

            // private Set<T> name;
            FieldSpec f = FieldSpec.builder(fieldType, name, Modifier.PRIVATE).build();
            cls.addField(f);

            // getter
            cls.addMethod(MethodSpec.methodBuilder("get" + up(name))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(fieldType)
                    .addStatement("return this.$N", name)
                    .build());

            // setter
            cls.addMethod(MethodSpec.methodBuilder("set" + up(name))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(fieldType, name)
                    .addStatement("this.$N = $N", name, name)
                    .build());
        }
    }

    /**
     * RANGE operators:
     * - we genereren twee velden: T nameStart; T nameEnd;
     */
    public static final class RangeSpec {
        private RangeSpec() {}

        public void addFields(TypeSpec.Builder cls, String name, TypeName type) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(name);
            Objects.requireNonNull(type);

            String start = name + "Start";
            String end   = name + "End";

            FieldSpec fStart = FieldSpec.builder(type, start, Modifier.PRIVATE).build();
            FieldSpec fEnd   = FieldSpec.builder(type, end,   Modifier.PRIVATE).build();

            cls.addField(fStart);
            cls.addField(fEnd);

            // getters/setters
            cls.addMethod(MethodSpec.methodBuilder("get" + up(start))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(type)
                    .addStatement("return this.$N", start)
                    .build());

            cls.addMethod(MethodSpec.methodBuilder("set" + up(start))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(type, start)
                    .addStatement("this.$N = $N", start, start)
                    .build());

            cls.addMethod(MethodSpec.methodBuilder("get" + up(end))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(type)
                    .addStatement("return this.$N", end)
                    .build());

            cls.addMethod(MethodSpec.methodBuilder("set" + up(end))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(type, end)
                    .addStatement("this.$N = $N", end, end)
                    .build());
        }
    }

    /**
     * SIZE operators:
     * - één integer veld met de originele naam (zoals je huidige code verwacht)
     */
    public static final class SizeSpec {
        private SizeSpec() {}

        public void addFields(TypeSpec.Builder cls, String name, TypeName boxedInteger) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(name);
            Objects.requireNonNull(boxedInteger);

            // private Integer name;
            FieldSpec f = FieldSpec.builder(boxedInteger, name, Modifier.PRIVATE).build();
            cls.addField(f);

            cls.addMethod(MethodSpec.methodBuilder("get" + up(name))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(boxedInteger)
                    .addStatement("return this.$N", name)
                    .build());

            cls.addMethod(MethodSpec.methodBuilder("set" + up(name))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(boxedInteger, name)
                    .addStatement("this.$N = $N", name, name)
                    .build());
        }
    }

    // ────────────────────────────────────────────────────────────────────────

    private static String up(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
