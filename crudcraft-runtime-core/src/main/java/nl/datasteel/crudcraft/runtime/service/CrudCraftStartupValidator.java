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

package nl.datasteel.crudcraft.runtime.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;


/**
 * Fail-fast startup validator for generated CrudCraft services.
 *
 * <p>The validator runs after Spring creates singleton beans and verifies that generated services
 * have their required collaborators wired before the first request reaches the service. Optional
 * runtime extensions and projection adapters remain optional; missing required repository, mapper,
 * query executor, or generated type metadata fails startup with a focused diagnostic.
 */
public final class CrudCraftStartupValidator implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    /**
     * Creates a startup validator backed by the application context.
     *
     * @param applicationContext Spring context used to discover generated services
     */
    public CrudCraftStartupValidator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /** Validates all generated CrudCraft services after singleton initialization. */
    @Override
    public void afterSingletonsInstantiated() {
        validate();
    }

    /**
     * Validates all registered CrudCraft service beans.
     *
     * @throws IllegalStateException when a generated service is missing a required collaborator
     */
    public void validate() {
        Map<String, CrudService> services =
                applicationContext.getBeansOfType(CrudService.class, false, false);
        List<String> failures = new ArrayList<>();
        services.forEach(
                (beanName, service) -> {
                    CrudService target = unwrapAopProxy(service);
                    if (target instanceof CoreCrudOperations<?, ?, ?, ?, ?> core) {
                        addFailure(beanName, core, failures);
                    }
                });
        if (!failures.isEmpty()) {
            throw new IllegalStateException(
                    "CrudCraft startup validation failed: " + String.join("; ", failures));
        }
    }

    private void addFailure(
            String beanName, CoreCrudOperations<?, ?, ?, ?, ?> service, List<String> failures) {
        List<String> missing = missingRequiredCollaborators(service);
        if (missing.isEmpty()) {
            return;
        }
        failures.add(
                "bean '"
                        + beanName
                        + "' ("
                        + service.getClass().getName()
                        + ") is missing "
                        + String.join(", ", missing)
                        + ". Ensure the generated repository, mapper, and service constructor"
                        + " are registered in the Spring context.");
    }

    private List<String> missingRequiredCollaborators(
            CoreCrudOperations<?, ?, ?, ?, ?> service) {
        List<String> missing = new ArrayList<>();
        if (service.repository == null) {
            missing.add("repository");
        }
        if (service.mapper == null) {
            missing.add("mapper");
        }
        if (service.queryExecutor() == null) {
            missing.add("queryExecutor");
        }
        if (service.entityClass == null) {
            missing.add("entityClass");
        }
        if (service.responseClass == null) {
            missing.add("responseClass");
        }
        if (service.refClass == null) {
            missing.add("refClass");
        }
        return missing;
    }

    private CrudService unwrapAopProxy(CrudService service) {
        Object current = service;
        while (current instanceof Advised advised) {
            try {
                TargetSource targetSource = advised.getTargetSource();
                Object target = targetSource.getTarget();
                if (!(target instanceof CrudService crudService) || target == current) {
                    return service;
                }
                current = crudService;
            } catch (Exception ex) {
                return service;
            }
        }
        return (CrudService) current;
    }
}
