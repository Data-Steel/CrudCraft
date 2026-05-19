/*
 * Copyright (c) 2026 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

/**
 * Spring Boot starter module that exposes CrudCraft projection runtime dependencies.
 */
module nl.datasteel.crudcraft.starter.projection {
    requires transitive nl.datasteel.crudcraft.runtime.projection;

    exports nl.datasteel.crudcraft.starter.projection;
}
