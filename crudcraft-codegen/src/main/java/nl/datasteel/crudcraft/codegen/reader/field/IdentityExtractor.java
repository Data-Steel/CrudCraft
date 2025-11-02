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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;

/**
 * Singleton extractor for the Identity field part.
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class IdentityExtractor implements FieldPartExtractor<Identity> {

    /**
     * Singleton instance of IdentityExtractor.
     */
    public static final IdentityExtractor INSTANCE = new IdentityExtractor();

    /**
     * Extracts the Identity field part from a VariableElement.
     *
     * @param field the VariableElement representing the field
     * @param env the ProcessingEnvironment for accessing annotations and other processing features
     * @return an Identity object representing the field's identity
     */
    @Override
    public Identity extract(VariableElement field, ProcessingEnvironment env) {
        FieldPartExtractor.log(env.getMessager(), Diagnostic.Kind.NOTE, field,
                "Extracting Identity field part");
        return new Identity(field.getSimpleName().toString(), field.asType());
    }
}
