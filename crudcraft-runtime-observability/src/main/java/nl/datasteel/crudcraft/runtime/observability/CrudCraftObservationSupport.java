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

package nl.datasteel.crudcraft.runtime.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.util.Objects;
import java.util.function.Supplier;


/** Helper for wrapping CrudCraft runtime operations in Micrometer observations. */
public final class CrudCraftObservationSupport {

    /** Common observation name used for CRUD runtime operations. */
    public static final String OBSERVATION_NAME = "crudcraft.operation";

    private final ObservationRegistry registry;

    /**
     * Creates observation support.
     *
     * @param registry observation registry, bridged to OpenTelemetry by the host application
     */
    public CrudCraftObservationSupport(ObservationRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    /**
     * Observes a CrudCraft operation.
     *
     * @param entityName entity name tag
     * @param operation operation name tag
     * @param supplier operation body
     * @param <T> return type
     * @return supplier result
     */
    public <T> T observe(String entityName, String operation, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return Observation.createNotStarted(OBSERVATION_NAME, registry)
                .contextualName("CrudCraft " + operation)
                .lowCardinalityKeyValue("crudcraft.entity", valueOrUnknown(entityName))
                .lowCardinalityKeyValue("crudcraft.operation", valueOrUnknown(operation))
                .observe(supplier);
    }

    /**
     * Observes a void CrudCraft operation.
     *
     * @param entityName entity name tag
     * @param operation operation name tag
     * @param runnable operation body
     */
    public void observe(String entityName, String operation, Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        observe(
                entityName,
                operation,
                () -> {
                    runnable.run();
                    return null;
                });
    }

    private String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
