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
package nl.datasteel.crudcraft.codegen.descriptor.model.part;

import java.util.List;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;

/**
 * Security-related configuration for a model.
 *
 * @param secure whether the model endpoints require security
 * @param securityPolicy the class handling security policies
 * @param rowSecurityHandlers optional row-level security handler class names
 */
public record ModelSecurity(boolean secure, Class<? extends CrudSecurityPolicy> securityPolicy,
        List<String> rowSecurityHandlers) {

    /**
     * Returns true if the model endpoints require security.
     *
     * @return true if secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Returns the class that handles security policies for this model.
     *
     * @return the security policy class
     */
    public Class<? extends CrudSecurityPolicy> getSecurityPolicy() {
        return securityPolicy;
    }

    /**
     * Returns the class names that handle row-level security for this model, if any.
     *
     * @return list of fully qualified row security handler class names
     */
    public List<String> getRowSecurityHandlers() {
        return rowSecurityHandlers;
    }
}
