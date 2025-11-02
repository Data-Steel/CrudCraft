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
package nl.datasteel.crudcraft.codegen.reader.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.annotations.security.policy.PermitAllSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;

/**
 * Singleton extractor for {@link ModelSecurity}.
 * Extracts {@link ModelSecurity} configuration from a model.
 */
@SuppressWarnings("java:S6548")
public class ModelSecurityExtractor implements ModelPartExtractor<ModelSecurity> {

    public static final ModelSecurityExtractor INSTANCE = new ModelSecurityExtractor();

    @Override
    public ModelSecurity extract(TypeElement cls, ProcessingEnvironment env) {
        CrudCrafted annotation = cls.getAnnotation(CrudCrafted.class);
        boolean secure = annotation == null || annotation.secure();

        Class<? extends CrudSecurityPolicy> securityPolicy =
                resolveSecurityPolicy(annotation, cls, env);

        List<String> rowSecurityHandlers = resolveRowSecurityHandlers(cls, env);

        return new ModelSecurity(secure, securityPolicy, rowSecurityHandlers);
    }

    private static Class<? extends CrudSecurityPolicy> resolveSecurityPolicy(
            CrudCrafted craft,
            TypeElement cls,
            ProcessingEnvironment env
    ) {
        if (craft == null) {
            return PermitAllSecurityPolicy.class;
        }

        try {
            // If we can read it directly (no mirrored exception), return it.
            return craft.securityPolicy();
        } catch (MirroredTypeException mte) {
            String fqn = mte.getTypeMirror().toString();
            try {
                @SuppressWarnings("unchecked")
                Class<? extends CrudSecurityPolicy> clazz =
                        (Class<? extends CrudSecurityPolicy>) tryLoadPossiblyNested(fqn);
                tryInstantiate(clazz, cls, env); // pre-instantiation check (logs on failure)
                return clazz;
            } catch (ClassNotFoundException e) {
                ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.NOTE, cls,
                        "Security policy class not found: " + fqn);
            } catch (Exception e) {
                ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.ERROR, cls,
                        "Error reading security policy: " + e.getMessage());
            }
        } catch (Exception e) {
            ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.ERROR, cls,
                    "Error reading security policy: " + e.getMessage());
        }

        return PermitAllSecurityPolicy.class;
    }

    private static List<String> resolveRowSecurityHandlers(
            TypeElement cls,
            ProcessingEnvironment env
    ) {
        RowSecurity rowSec = cls.getAnnotation(RowSecurity.class);
        if (rowSec == null) {
            return Collections.emptyList();
        }

        try {
            Class<?>[] handlers = rowSec.handlers();
            List<String> names = new ArrayList<>(handlers.length);
            for (Class<?> handler : handlers) {
                names.add(handler.getCanonicalName());
            }
            return names;
        } catch (MirroredTypeException mte) {
            String fqn = eraseGenerics(mte.getTypeMirror().toString());
            return List.of(fqn);
        } catch (MirroredTypesException mte) {
            List<String> handlers = new ArrayList<>();
            for (TypeMirror tm : mte.getTypeMirrors()) {
                handlers.add(eraseGenerics(tm.toString()));
            }
            return handlers;
        } catch (Exception e) {
            ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.ERROR, cls,
                    "Error reading row security handler: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    private static void tryInstantiate(Class<?> clazz, TypeElement cls, ProcessingEnvironment env) {
        try {
            clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.ERROR,  cls,
                    "Failed to instantiate class: " + clazz.getCanonicalName());
        }
    }

    private static String eraseGenerics(String typeName) {
        int idx = typeName.indexOf('<');
        return idx >= 0 ? typeName.substring(0, idx) : typeName;
    }

    /**
     * Attempts to load a class by canonical name; if that fails, progressively
     * replaces inner-class dots with '$' to get the binary name.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    private static Class<?> tryLoadPossiblyNested(String canonicalName) throws ClassNotFoundException {
        // First try canonical form (works for top-level classes).
        try {
            return Class.forName(canonicalName);
        } catch (ClassNotFoundException ignore) {
            // Fall through to try with '$' for nested types.
        }

        String candidate = canonicalName;
        int lastDot = candidate.lastIndexOf('.');
        while (lastDot > 0) {
            candidate = candidate.substring(0, lastDot) + '$' + candidate.substring(lastDot + 1);
            try {
                return Class.forName(candidate);
            } catch (ClassNotFoundException ignore) {
                // keep trying by converting earlier dots
            }
            lastDot = candidate.lastIndexOf('.');
        }
        throw new ClassNotFoundException(canonicalName);
    }
}
