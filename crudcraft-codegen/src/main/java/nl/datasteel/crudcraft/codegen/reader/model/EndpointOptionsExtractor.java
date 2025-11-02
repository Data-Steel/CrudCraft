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
package nl.datasteel.crudcraft.codegen.reader.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;

/**
 * Singleton extractor for {@link EndpointOptions}.
 * Extracts endpoint-related options from a model annotated with {@link CrudCrafted}.
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class EndpointOptionsExtractor implements ModelPartExtractor<EndpointOptions> {

    /**
     * Singleton instance of EndpointOptionsExtractor.
     */
    public static final EndpointOptionsExtractor INSTANCE = new EndpointOptionsExtractor();

    /**
     * Extracts endpoint options from the given model class.
     *
     * @param cls the TypeElement representing the model class
     * @param env processing environment for annotation utilities
     * @return an EndpointOptions object containing the extracted options
     */
    @Override
    public EndpointOptions extract(TypeElement cls, ProcessingEnvironment env) {
        CrudCrafted annotation = cls.getAnnotation(CrudCrafted.class);

        CrudTemplate template = (annotation != null)
                ? annotation.template() : CrudTemplate.FULL;

        CrudEndpoint[] omit = (annotation != null)
                ? annotation.omitEndpoints() : new CrudEndpoint[0];

        CrudEndpoint[] include = (annotation != null)
                ? annotation.includeEndpoints() : new CrudEndpoint[0];

        Class<? extends CrudEndpointPolicy> policyClass = extractPolicyClass(annotation, env, cls);

        validatePolicyApplication(policyClass, template, omit, include, env, cls);

        return new EndpointOptions(template, omit, include, policyClass);
    }

    /**
     * Extracts the policy class from the CrudCrafted annotation.
     */
    private Class<? extends CrudEndpointPolicy> extractPolicyClass(
            CrudCrafted annotation, ProcessingEnvironment env, TypeElement cls) {
        if (annotation == null) {
            return CrudTemplate.class;
        }

        try {
            return annotation.endpointPolicy();
            // triggers MirroredTypeException in annotation processing
        } catch (MirroredTypeException mte) {
            String fqn = mte.getTypeMirror().toString();
            try {
                Class<?> clazz = Class.forName(fqn);
                if (CrudEndpointPolicy.class.isAssignableFrom(clazz)) {
                    return clazz.asSubclass(CrudEndpointPolicy.class);
                }
                ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.ERROR, cls,
                        "Policy class does not implement CrudEndpointPolicy: " + fqn);
            } catch (ClassNotFoundException e) {
                ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.ERROR, cls,
                        "Policy class not found: " + fqn);
            }
            throw new IllegalStateException("Unable to load endpoint policy class: " + fqn);
        } catch (Exception e) {
            ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.ERROR, cls,
                    "Failed to resolve endpoint policy class: " + e.getMessage());
            throw new IllegalStateException("Failed to resolve endpoint policy class", e);
        }
    }

    /**
     * Validates and logs information about the applied endpoint policy and overrides.
     *
     * @param policyClass the policy class declared on the model
     * @param template the {@link CrudTemplate} to use when the default policy is active
     * @param omit endpoints explicitly omitted
     * @param include endpoints explicitly included
     * @param env processing environment for logging
     * @param cls the model type being processed
     */
    private void validatePolicyApplication(
            Class<? extends CrudEndpointPolicy> policyClass,
            CrudTemplate template,
            CrudEndpoint[] omit,
            CrudEndpoint[] include,
            ProcessingEnvironment env,
            TypeElement cls) {

        Set<CrudEndpoint> baseEndpoints;

        try {
            CrudEndpointPolicy policyInstance;
            if (policyClass == CrudTemplate.class) {
                policyInstance = template;
            } else {
                policyInstance = policyClass.getDeclaredConstructor().newInstance();
            }
            baseEndpoints = policyInstance.resolveEndpoints();
        } catch (Exception e) {
            ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.ERROR, cls,
                    "Could not instantiate endpoint policy: " + e.getMessage());
            throw new IllegalStateException("Could not instantiate endpoint policy", e);
        }

        Set<CrudEndpoint> resolved = new HashSet<>(baseEndpoints);
        Arrays.asList(omit).forEach(resolved::remove);
        resolved.addAll(Arrays.asList(include));

        if (resolved.isEmpty()) {
            ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.WARNING, cls,
                    "No endpoints resolved: template + omit/include result in empty set.");
        }

        for (CrudEndpoint ep : omit) {
            if (!baseEndpoints.contains(ep)) {
                ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.WARNING, cls,
                        "Omitted endpoint " + ep + " is not part of the base template.");
            }
        }

        for (CrudEndpoint ep : include) {
            if (baseEndpoints.contains(ep)) {
                ModelPartExtractor.log(env.getMessager(), Diagnostic.Kind.WARNING, cls,
                        "Included endpoint " + ep + " is already part of the template.");
            }
        }
    }
}
