package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public final class TypeRefs {
    private TypeRefs() {}

    public static ClassName SET()     {return ClassName.get(Set.class); }
    public static ClassName LIST()    { return ClassName.get(List.class); }
    public static ClassName INSTANT() { return ClassName.get(Instant.class); }

    public static TypeName setOf(TypeName element) {
        return ParameterizedTypeName.get(SET(), element);
    }

    public static TypeName listOf(TypeName element) {
        return ParameterizedTypeName.get(LIST(), element);
    }
}
