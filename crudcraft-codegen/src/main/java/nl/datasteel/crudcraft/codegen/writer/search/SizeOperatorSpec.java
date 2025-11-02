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
package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;

/**
 * Handles size operators in search requests, such as greater than, less than,
 * and size equality. This class adds fields for these operators to the generated
 * search request DTO.
 */
public class SizeOperatorSpec implements OperatorSpec {

    /**
     * Adds fields for size operators to the given TypeSpec builder.
     * This method creates private fields for the operator and its value,
     * along with corresponding getter and setter methods.
     *
     * @param cls the TypeSpec builder to which fields will be added
     * @param prop the property name for the operator
     * @param type the type of the operator value
     */
    @Override
    public void addFields(TypeSpec.Builder cls, String prop, TypeName type) {
        TypeName t = TypeNames.simple(type);
        cls.addField(t, prop, Modifier.PRIVATE);
        cls.addMethod(SearchAccessorUtil.getter(prop, t));
        cls.addMethod(SearchAccessorUtil.setter(prop, t));
    }
}

