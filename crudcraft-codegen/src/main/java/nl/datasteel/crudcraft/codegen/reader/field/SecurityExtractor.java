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

package nl.datasteel.crudcraft.codegen.reader.field;

import java.util.Arrays;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;

/**
 * Extracts {@link Security} configuration from a field.
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class SecurityExtractor implements FieldPartExtractor<Security> {

    /**
     * Singleton instance.
     */
    public static final SecurityExtractor INSTANCE = new SecurityExtractor();

    /**
     * Extracts Security from a field annotated with @FieldSecurity.
     *
     * @param field the VariableElement representing the field
     * @param env the ProcessingEnvironment for accessing annotations and other processing features
     * @return Security instance containing read and write roles
     */
    @Override
    public Security extract(VariableElement field, ProcessingEnvironment env) {
        FieldSecurity fs = field.getAnnotation(FieldSecurity.class);
        boolean defined = fs != null;
        String[] read = defined ? fs.readRoles() : new String[0];
        String[] write = defined ? fs.writeRoles() : new String[0];

        FieldPartExtractor.log(env.getMessager(), Diagnostic.Kind.NOTE, field,
                String.format("Extracting Security → defined=%s, read=%s, write=%s",
                        defined, Arrays.toString(read), Arrays.toString(write)));

        return new Security(defined, read, write);
    }
}