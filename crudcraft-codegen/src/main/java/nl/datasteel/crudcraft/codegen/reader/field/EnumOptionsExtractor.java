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
package nl.datasteel.crudcraft.codegen.reader.field;

import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.fields.EnumString;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;

/**
 * Singleton extractor for enum field options.
 * This class implements the FieldPartExtractor interface
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class EnumOptionsExtractor implements FieldPartExtractor<EnumOptions> {

    /**
     * Singleton instance of EnumExtractor.
     */
    public static final EnumOptionsExtractor INSTANCE = new EnumOptionsExtractor();

    /**
     * Extracts enum options from a field.
     *
     * @param field the VariableElement representing the field
     * @param env the ProcessingEnvironment for accessing annotations and other processing features
     * @return EnumOptions containing information about the enum field
     */
    @Override
    public EnumOptions extract(VariableElement field, ProcessingEnvironment env) {
        EnumString enumAnnotation = field.getAnnotation(EnumString.class);
        boolean isEnum = enumAnnotation != null;
        List<String> values = isEnum ? List.of(enumAnnotation.values()) : List.of();

        FieldPartExtractor.log(env.getMessager(), Diagnostic.Kind.NOTE, field,
                String.format("Extracting EnumOptions → isEnum = %s, values = %s", isEnum, values)
        );

        return new EnumOptions(isEnum, values);
    }
}
