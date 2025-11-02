/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.projection.mapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;

/**
 * Resolves mapping information for DTO fields at runtime. Each constructor
 * parameter is mapped to an entity path, either by naming convention or by
 * {@link ProjectionField} annotation.
 */
public class ProjectionFieldResolver {

    /**
     * Describes a single field mapping for a DTO constructor parameter.
     *
     * @param path       entity attribute path relative to the current entity
     * @param collection whether the field represents a collection
     * @param nestedType nested DTO type for complex attributes, or {@code null}
     */
    public record FieldMapping(String path, boolean collection, Class<?> nestedType) {}

    /**
     * Resolve field mappings for the given DTO type. The order of mappings
     * corresponds to the constructor parameter order of the DTO.
     *
     * @param dtoType DTO type to analyse
     * @param <D>     DTO type
     * @return list of field mappings in constructor order
     */
    public <D> List<FieldMapping> resolve(Class<D> dtoType) {
        Constructor<?> ctor = findConstructor(dtoType);
        Parameter[] params = ctor.getParameters();
        // use a mutable list so that we can remove fields once they are mapped. This
        // avoids the same field being matched to multiple constructor parameters when
        // using heuristics such as "unique type" or "annotated" lookups.
        List<Field> remaining = new ArrayList<>(Arrays.asList(dtoType.getDeclaredFields()));

        List<FieldMapping> mappings = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            Field field = findField(dtoType, param, i, remaining);
            String path = resolvePath(field);
            boolean collection = Collection.class.isAssignableFrom(field.getType());
            Class<?> nested = null;
            if (collection) {
                Class<?> element = resolveCollectionElement(field);
                if (isDto(element)) nested = element;
            } else if (isDto(field.getType())) {
                nested = field.getType();
            }
            mappings.add(new FieldMapping(path, collection, nested));
            // Mark the field as used so that it won't match subsequent parameters.
            remaining.remove(field);
        }
        return mappings;
    }

    /**
     * Find the best constructor for the DTO type. The "best" constructor is
     * defined as the one with the most parameters, allowing for maximum
     * flexibility in mapping.
     *
     * @param dtoType the DTO class to inspect
     * @return the best constructor for the DTO
     * @throws IllegalStateException if no constructors are found
     */
    private Constructor<?> findConstructor(Class<?> dtoType) {
        Constructor<?>[] ctors = dtoType.getDeclaredConstructors();
        if (ctors.length == 0) {
            throw new IllegalStateException("No constructor found for DTO " + dtoType.getName());
        }
        Constructor<?> best = ctors[0];
        for (Constructor<?> ctor : ctors) {
            if (ctor.getParameterCount() > best.getParameterCount()) {
                best = ctor;
            }
        }
        best.setAccessible(true);
        return best;
    }

    /**
     * Find a field by name in the DTO class. The field must be accessible.
     *
     * @param type the DTO class
     * the constructor parameter to match
     * the index of the parameter in the constructor
     * the declared fields of the DTO class
     */
    // inside ProjectionFieldResolver

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private Field findField(Class<?> type, Parameter param, int index, List<Field> declared) {
        // 1) by exact param name (works if compiled with -parameters)
        try {
            Field f = type.getDeclaredField(param.getName());
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException ignore) {
            // ignore
        }

        // 2) by unique type
        var sameType = declared.stream().filter(f -> f.getType().equals(param.getType())).toList();
        if (sameType.size() == 1) {
            var f = sameType.getFirst(); f.setAccessible(true); return f;
        }

        // 2b) tie-break by @ProjectionField
        var annotated = sameType.stream().filter(f -> f.isAnnotationPresent(ProjectionField.class)).toList();
        if (annotated.size() == 1) {
            var f = annotated.getFirst(); f.setAccessible(true); return f;
        }

        // 2c) if it’s a collection param, prefer the one whose element type “looks like a DTO”
        if (Collection.class.isAssignableFrom(param.getType())) {
            var dtoElem = sameType.stream()
                    .filter(f -> Collection.class.isAssignableFrom(f.getType()))
                    .filter(f -> isDto(resolveCollectionElement(f)))
                    .toList();
            if (dtoElem.size() == 1) {
                var f = dtoElem.getFirst(); f.setAccessible(true); return f;
            }
        }

        // 3) fallback to declared-order index
        if (index >= 0 && index < declared.size()) {
            Field f = declared.get(index); f.setAccessible(true); return f;
        }

        throw new IllegalStateException("No field '" + param.getName() + "' on DTO " + type.getName());
    }

    /**
     * Resolve the entity path for a field. If the field has a {@link ProjectionField}
     * annotation, its value is used; otherwise, the field name is used.
     *
     * @param field the field to inspect
     * @return the resolved entity path
     */
    private String resolvePath(Field field) {
        ProjectionField pf = field.getAnnotation(ProjectionField.class);
        if (pf != null && !pf.value().isEmpty()) {
            return pf.value();
        }
        return field.getName();
    }

    /**
     * Resolve the element type of collection field. If the field is not a
     * parameterized collection, returns {@code Object.class}.
     *
     * @param field the field to inspect
     * @return the element type of the collection, or {@code Object.class} if unknown
     */
    private Class<?> resolveCollectionElement(Field field) {
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType pt) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) return c;
            if (arg instanceof ParameterizedType pArg && pArg.getRawType() instanceof Class<?> raw) return raw;
            if (arg instanceof java.lang.reflect.WildcardType wt) {
                Type[] up = wt.getUpperBounds();
                if (up.length == 1 && up[0] instanceof Class<?> c) return c;
            }
        }
        return Object.class;
    }

    /**
     * Check if the given type is a DTO based on package name and naming convention.
     * This is a heuristic and may not cover all cases.
     *
     * @param type the class to check
     * @return true if the class is likely a DTO
     */
    private boolean isDto(Class<?> type) {
        Package pkg = type.getPackage();
        String pkgName = pkg != null ? pkg.getName() : "";
        return type.getSimpleName().endsWith("Dto") &&
                (pkgName.contains(".dto.") || pkgName.endsWith(".dto"));
    }
}

