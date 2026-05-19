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

package nl.datasteel.crudcraft.codegen.reader.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.security.ClientScoped;
import nl.datasteel.crudcraft.annotations.security.CrudSecurity;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.annotations.security.EndpointRbac;
import nl.datasteel.crudcraft.annotations.security.OwnedBy;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.annotations.security.TenantScoped;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.RowScope;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ScopeKind;


/**
 * Singleton extractor for {@link ModelSecurity}. Extracts {@link ModelSecurity} configuration from
 * a model.
 */
@SuppressWarnings("java:S6548")
public class ModelSecurityExtractor implements ModelPartExtractor<ModelSecurity> {

    /** Singleton instance. */
    public static final ModelSecurityExtractor INSTANCE = new ModelSecurityExtractor();

    private static final Set<CrudEndpoint> READ_ENDPOINTS =
            EnumSet.of(
                    CrudEndpoint.GET_ALL,
                    CrudEndpoint.GET_ALL_REF,
                    CrudEndpoint.GET_ONE,
                    CrudEndpoint.FIND_BY_IDS,
                    CrudEndpoint.EXISTS,
                    CrudEndpoint.COUNT,
                    CrudEndpoint.SEARCH,
                    CrudEndpoint.EXPORT);
    private static final Set<CrudEndpoint> WRITE_ENDPOINTS =
            EnumSet.of(
                    CrudEndpoint.POST,
                    CrudEndpoint.PUT,
                    CrudEndpoint.PATCH,
                    CrudEndpoint.BULK_CREATE,
                    CrudEndpoint.BULK_UPDATE,
                    CrudEndpoint.BULK_PATCH,
                    CrudEndpoint.BULK_UPSERT,
                    CrudEndpoint.VALIDATE);
    private static final Set<CrudEndpoint> DELETE_ENDPOINTS =
            EnumSet.of(CrudEndpoint.DELETE, CrudEndpoint.BULK_DELETE);

    /** Creates the model security extractor. */
    public ModelSecurityExtractor() {}

    @Override
    public ModelSecurity extract(TypeElement cls, ProcessingEnvironment env) {
        CrudCrafted annotation = cls.getAnnotation(CrudCrafted.class);
        boolean secure = annotation != null && annotation.secure();

        Class<? extends CrudSecurityPolicy> securityPolicy =
                resolveSecurityPolicy(annotation, cls, env);

        List<String> rowSecurityHandlers = resolveRowSecurityHandlers(cls, env);
        List<RowScope> rowScopes = resolveRowScopes(cls);
        Map<CrudEndpoint, String> endpointExpressions = resolveEndpointExpressions(cls);
        secure =
                secure
                        || !rowSecurityHandlers.isEmpty()
                        || !rowScopes.isEmpty()
                        || !endpointExpressions.isEmpty();

        return new ModelSecurity(
                secure, securityPolicy, rowSecurityHandlers, rowScopes, endpointExpressions);
    }

    private static Class<? extends CrudSecurityPolicy> resolveSecurityPolicy(
            CrudCrafted craft, TypeElement cls, ProcessingEnvironment env) {
        if (craft == null) {
            return CrudSecurityPolicy.class;
        }

        try {
            // If we can read it directly (no mirrored exception), return it.
            return craft.securityPolicy();
        } catch (MirroredTypeException mte) {
            String fqn = mte.getTypeMirror().toString();
            try {
                Class<?> loadedClass = tryLoadPossiblyNested(fqn);
                if (!CrudSecurityPolicy.class.isAssignableFrom(loadedClass)) {
                    throw new IllegalStateException(
                            "Security policy does not implement "
                                    + CrudSecurityPolicy.class.getSimpleName()
                                    + ": "
                                    + fqn);
                }
                Class<? extends CrudSecurityPolicy> clazz =
                        loadedClass.asSubclass(CrudSecurityPolicy.class);
                if (clazz != CrudSecurityPolicy.class) {
                    tryInstantiate(clazz, cls, env);
                }
                return clazz;
            } catch (ClassNotFoundException e) {
                ModelPartExtractor.log(
                        env.getMessager(),
                        Diagnostic.Kind.NOTE,
                        cls,
                        "Security policy class not found: " + fqn);
            } catch (Exception e) {
                ModelPartExtractor.log(
                        env.getMessager(),
                        Diagnostic.Kind.ERROR,
                        cls,
                        "Error reading security policy: " + e.getMessage());
            }
        } catch (Exception e) {
            ModelPartExtractor.log(
                    env.getMessager(),
                    Diagnostic.Kind.ERROR,
                    cls,
                    "Error reading security policy: " + e.getMessage());
        }

        return CrudSecurityPolicy.class;
    }

    private static List<String> resolveRowSecurityHandlers(
            TypeElement cls, ProcessingEnvironment env) {
        RowSecurity rowSec = cls.getAnnotation(RowSecurity.class);
        if (rowSec == null) {
            return Collections.emptyList();
        }

        List<String> fromMirrors = resolveRowSecurityHandlersFromMirrors(cls);
        if (!fromMirrors.isEmpty()) {
            return fromMirrors;
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
            ModelPartExtractor.log(
                    env.getMessager(),
                    Diagnostic.Kind.ERROR,
                    cls,
                    "Error reading row security handler: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    private static List<String> resolveRowSecurityHandlersFromMirrors(TypeElement cls) {
        List<String> extractedHandlers = List.of();
        for (AnnotationMirror mirror : cls.getAnnotationMirrors()) {
            if (!RowSecurity.class
                    .getCanonicalName()
                    .equals(mirror.getAnnotationType().toString())) {
                continue;
            }
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                    mirror.getElementValues().entrySet()) {
                if (!"handlers".contentEquals(entry.getKey().getSimpleName())) {
                    continue;
                }
                Object value = entry.getValue().getValue();
                if (!(value instanceof List<?> values) || values.isEmpty()) {
                    return List.of();
                }
                List<String> handlers = new ArrayList<>(values.size());
                for (Object raw : values) {
                    if (raw instanceof AnnotationValue annotationValue) {
                        Object handlerValue = annotationValue.getValue();
                        if (handlerValue instanceof TypeMirror typeMirror) {
                            handlers.add(eraseGenerics(typeMirror.toString()));
                        }
                    }
                }
                extractedHandlers = handlers;
            }
            if (!extractedHandlers.isEmpty()) {
                break;
            }
        }
        return extractedHandlers;
    }

    private static void tryInstantiate(Class<?> clazz, TypeElement cls, ProcessingEnvironment env) {
        try {
            clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            ModelPartExtractor.log(
                    env.getMessager(),
                    Diagnostic.Kind.ERROR,
                    cls,
                    "Failed to instantiate class: " + clazz.getCanonicalName());
        }
    }

    private static String eraseGenerics(String typeName) {
        int idx = typeName.indexOf('<');
        return idx >= 0 ? typeName.substring(0, idx) : typeName;
    }

    private static List<RowScope> resolveRowScopes(TypeElement cls) {
        List<RowScope> scopes = new ArrayList<>();
        TenantScoped tenantScoped = cls.getAnnotation(TenantScoped.class);
        if (tenantScoped != null) {
            scopes.add(new RowScope(ScopeKind.TENANT, tenantScoped.field(), tenantScoped.claim()));
        }
        ClientScoped clientScoped = cls.getAnnotation(ClientScoped.class);
        if (clientScoped != null) {
            scopes.add(new RowScope(ScopeKind.CLIENT, clientScoped.field(), clientScoped.claim()));
        }
        OwnedBy ownedBy = cls.getAnnotation(OwnedBy.class);
        if (ownedBy != null) {
            scopes.add(new RowScope(ScopeKind.OWNER, ownedBy.field(), ownedBy.claim()));
        }
        return scopes;
    }

    private static Map<CrudEndpoint, String> resolveEndpointExpressions(TypeElement cls) {
        CrudSecurity crudSecurity = cls.getAnnotation(CrudSecurity.class);
        if (crudSecurity == null) {
            return Map.of();
        }
        Map<CrudEndpoint, String> resolved = new EnumMap<>(CrudEndpoint.class);
        String read = rolesToExpression(crudSecurity.readRoles(), "readRoles");
        String write = rolesToExpression(crudSecurity.writeRoles(), "writeRoles");
        String delete = rolesToExpression(crudSecurity.deleteRoles(), "deleteRoles");
        READ_ENDPOINTS.forEach(endpoint -> resolved.put(endpoint, read));
        WRITE_ENDPOINTS.forEach(endpoint -> resolved.put(endpoint, write));
        DELETE_ENDPOINTS.forEach(endpoint -> resolved.put(endpoint, delete));
        Set<CrudEndpoint> seenOverrides = EnumSet.noneOf(CrudEndpoint.class);
        for (EndpointRbac override : crudSecurity.endpoints()) {
            CrudEndpoint endpoint = override.endpoint();
            if (!seenOverrides.add(endpoint)) {
                throw new IllegalStateException(
                        "Duplicate @EndpointRbac rule for endpoint: " + endpoint);
            }
            resolved.put(
                    endpoint,
                    rolesToExpression(override.roles(), "endpoints[" + endpoint + "].roles"));
        }
        return resolved;
    }

    private static String rolesToExpression(String[] rawRoles, String source) {
        if (rawRoles == null || rawRoles.length == 0) {
            return "denyAll()";
        }
        List<String> normalized =
                Arrays.stream(rawRoles)
                        .map(ModelSecurityExtractor::normalizeRole)
                        .sorted(Comparator.naturalOrder())
                        .distinct()
                        .toList();
        if (normalized.stream().anyMatch(String::isBlank)) {
            throw new IllegalStateException("Invalid empty role in " + source);
        }
        if (normalized.size() == 1) {
            return "hasRole('" + normalized.getFirst() + "')";
        }
        String joined =
                normalized.stream()
                        .map(role -> "'" + role + "'")
                        .collect(java.util.stream.Collectors.joining(", "));
        return "hasAnyRole(" + joined + ")";
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String trimmed = role.trim();
        if (trimmed.startsWith("ROLE_")) {
            return trimmed.substring("ROLE_".length());
        }
        return trimmed;
    }

    /**
     * Attempts to load a class by canonical name; if that fails, progressively replaces inner-class
     * dots with '$' to get the binary name.
     */
    private static Class<?> tryLoadPossiblyNested(String canonicalName)
            throws ClassNotFoundException {
        List<String> candidates = new ArrayList<>();
        candidates.add(canonicalName);
        List<Integer> dotPositions = new ArrayList<>();
        for (int i = 0; i < canonicalName.length(); i++) {
            if (canonicalName.charAt(i) == '.') {
                dotPositions.add(i);
            }
        }

        Collections.reverse(dotPositions);
        String candidate = canonicalName;
        for (int dot : dotPositions) {
            candidate = candidate.substring(0, dot) + '$' + candidate.substring(dot + 1);
            candidates.add(candidate);
        }

        for (String className : candidates) {
            Class<?> loaded = tryLoadClass(className);
            if (loaded != null) {
                return loaded;
            }
        }
        throw new ClassNotFoundException(canonicalName);
    }

    private static Class<?> tryLoadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
