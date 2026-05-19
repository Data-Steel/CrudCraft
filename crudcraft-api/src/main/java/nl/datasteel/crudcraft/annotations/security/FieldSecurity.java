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

package nl.datasteel.crudcraft.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Declares role-based read and write access for a field.
 *
 * <p>Roles are application authority names without a framework-mandated prefix. The default
 * {@code "ALL"} value means the generated field-security metadata allows every authenticated or
 * unauthenticated caller that reaches the service layer; endpoint RBAC remains a separate
 * controller concern.
 *
 * <pre>{@code
 * @FieldSecurity(readRoles = "ADMIN", writeRoles = "ADMIN")
 * private String internalNote;
 * }</pre>
 *
 * <pre>{@code
 * @FieldSecurity(readRoles = {"SUPPORT", "ADMIN"}, writePolicy = WritePolicy.FAIL_ON_DENIED)
 * private String customerEmail;
 * }</pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldSecurity {

    /**
     * Roles allowed to read the field.
     *
     * @return roles with read access
     */
    String[] readRoles() default {"ALL"};

    /**
     * Roles allowed to write the field.
     *
     * @return roles with write access
     */
    String[] writeRoles() default {"ALL"};

    /**
     * Policy applied when write access is denied.
     *
     * @return denied-write handling policy
     */
    WritePolicy writePolicy() default WritePolicy.SKIP_ON_DENIED;
}
