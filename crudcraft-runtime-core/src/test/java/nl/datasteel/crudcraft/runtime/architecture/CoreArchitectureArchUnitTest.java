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

package nl.datasteel.crudcraft.runtime.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


class CoreArchitectureArchUnitTest {

    private static final String[] OPTIONAL_RUNTIME_PACKAGES = {
        "nl.datasteel.crudcraft.runtime.search..",
        "nl.datasteel.crudcraft.runtime.security..",
        "nl.datasteel.crudcraft.runtime.projection..",
        "nl.datasteel.crudcraft.runtime.export..",
        "nl.datasteel.crudcraft.runtime.extensions.."
    };

    @Test
    void coreCodeMustNotDependOnOptionalRuntimeModules() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage("nl.datasteel.crudcraft.runtime..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(OPTIONAL_RUNTIME_PACKAGES)
                        .because(
                                "runtime-core must stay neutral and only use extension contracts.");

        rule.check(
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages("nl.datasteel.crudcraft.runtime"));
    }
}
