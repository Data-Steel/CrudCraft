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

import io.micrometer.observation.ObservationRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CrudCraftObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(CrudCraftObservabilityAutoConfiguration.class));

    @Test
    void registersObservationSupport() {
        contextRunner.run(
                context -> {
                    assertNotNull(context.getBean(ObservationRegistry.class));
                    assertNotNull(context.getBean(CrudCraftObservationSupport.class));
                });
    }

    @Test
    void supportObservesSupplierResult() {
        contextRunner.run(
                context -> {
                    CrudCraftObservationSupport support =
                            context.getBean(CrudCraftObservationSupport.class);
                    assertEquals("ok", support.observe("Book", "findAll", () -> "ok"));
                });
    }

    @Test
    void supportObservesRunnable() {
        CrudCraftObservationSupport support =
                new CrudCraftObservationSupport(ObservationRegistry.NOOP);
        AtomicBoolean invoked = new AtomicBoolean();

        support.observe("Book", "delete", () -> invoked.set(true));

        assertTrue(invoked.get());
    }

    @Test
    void supportUsesUnknownTagsForBlankValues() {
        CrudCraftObservationSupport support =
                new CrudCraftObservationSupport(ObservationRegistry.NOOP);

        assertEquals("ok", support.observe("", " ", () -> "ok"));
        assertEquals("ok", support.observe(null, null, () -> "ok"));
    }

    @Test
    void supportRejectsNullCollaborators() {
        assertThrows(NullPointerException.class, () -> new CrudCraftObservationSupport(null));

        CrudCraftObservationSupport support =
                new CrudCraftObservationSupport(ObservationRegistry.NOOP);
        assertThrows(
                NullPointerException.class,
                () -> support.observe("Book", "findAll", (java.util.function.Supplier<String>) null));
        assertThrows(
                NullPointerException.class,
                () -> support.observe("Book", "delete", (Runnable) null));
    }
}
