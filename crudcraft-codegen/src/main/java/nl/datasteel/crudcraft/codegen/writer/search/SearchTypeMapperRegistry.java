package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Central place to decide which type the SearchRequest should expose for a given model type.
 * - Always return JavaPoet TypeNames (never raw strings) so imports are generated.
 * - Extensible via register(...) to support custom library types.
 */
public final class SearchTypeMapperRegistry {

    /**
     * A mapper turns a model field type into a SearchRequest field type.
     * It can also map parameterized types (e.g., Set<T>) element-wise.
     */
    public interface Mapper {
        boolean supports(TypeName original);
        TypeName toSearchType(TypeName original);
    }

    private static final List<Mapper> DEFAULT_MAPPERS = List.of(
            new SimpleMapper(LocalDate.class),
            new SimpleMapper(LocalDateTime.class),
            new SimpleMapper(OffsetDateTime.class),
            new SimpleMapper(ZonedDateTime.class),
            new SimpleMapper(Instant.class),
            new SimpleMapper(UUID.class),
            new SimpleMapper(BigDecimal.class),
            new SimpleMapper(BigInteger.class),
            new CollectionMapper()
    );

    private static final java.util.LinkedList<Mapper> CUSTOM = new java.util.LinkedList<>();

    private SearchTypeMapperRegistry() {}

    public static void register(Mapper mapper) {
        if (mapper != null) {
            CUSTOM.addFirst(mapper); // custom has priority
        }
    }

    /**
     * Map a model-field TypeName to its SearchRequest field TypeName.
     * Keeps parameterized raw types (e.g. Set/List) and maps their args recursively.
     */
    public static TypeName map(TypeName original) {
        if (original == null) return TypeName.OBJECT;

        for (Mapper m : CUSTOM) {
            if (m.supports(original)) {
                return m.toSearchType(original);
            }
        }
        for (Mapper m : DEFAULT_MAPPERS) {
            if (m.supports(original)) {
                return m.toSearchType(original);
            }
        }
        return original;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Maps a single concrete class to itself (ensuring ClassName import). */
    private static final class SimpleMapper implements Mapper {
        private final ClassName cn;
        private SimpleMapper(Class<?> cls) {
            this.cn = ClassName.get(cls);
        }
        @Override public boolean supports(TypeName original) {
            return original instanceof ClassName c && c.canonicalName().equals(cn.canonicalName());
        }
        @Override public TypeName toSearchType(TypeName original) {
            return cn;
        }
    }

    /** Maps parameterized collections preserving raw type and mapping type arguments recursively. */
    private static final class CollectionMapper implements Mapper {
        private static final Set<String> SUPPORTED_RAW = Set.of(
                "java.util.Set", "java.util.List", "java.util.Collection"
        );
        @Override public boolean supports(TypeName original) {
            if (original instanceof ParameterizedTypeName p) {
                TypeName raw = p.rawType;
                return (raw instanceof ClassName c) && SUPPORTED_RAW.contains(c.canonicalName());
            }
            return false;
        }
        @Override public TypeName toSearchType(TypeName original) {
            ParameterizedTypeName p = (ParameterizedTypeName) original;
            ClassName raw = p.rawType;
            TypeName[] args = new TypeName[p.typeArguments.size()];
            for (int i = 0; i < args.length; i++) {
                args[i] = map(p.typeArguments.get(i));
            }
            return ParameterizedTypeName.get(raw, args);
        }
    }
}
