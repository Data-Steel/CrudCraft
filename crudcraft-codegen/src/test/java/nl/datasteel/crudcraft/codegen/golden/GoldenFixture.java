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

package nl.datasteel.crudcraft.codegen.golden;

import java.util.List;


/** Describes one generated-source golden fixture. */
record GoldenFixture(
        String name,
        List<String> inputResources,
        List<String> processorOptions,
        String expectedError,
        boolean verifyInsomnia) {

    private static final String ROOT = "golden/";

    static GoldenFixture success(String name, String... inputs) {
        return new GoldenFixture(name, resources(name, inputs), List.of(), "", false);
    }

    static GoldenFixture successWithInsomnia(String name, String... inputs) {
        return new GoldenFixture(name, resources(name, inputs), List.of(), "", true);
    }

    static GoldenFixture successWithOptions(String name, List<String> options, String... inputs) {
        return new GoldenFixture(name, resources(name, inputs), List.copyOf(options), "", false);
    }

    static GoldenFixture failure(String name, String expectedError, String... inputs) {
        return new GoldenFixture(name, resources(name, inputs), List.of(), expectedError, false);
    }

    boolean expectsFailure() {
        return !expectedError.isBlank();
    }

    private static List<String> resources(String name, String... inputs) {
        return List.of(inputs).stream().map(input -> ROOT + name + "/input/" + input).toList();
    }

    @Override
    public String toString() {
        return name;
    }
}
