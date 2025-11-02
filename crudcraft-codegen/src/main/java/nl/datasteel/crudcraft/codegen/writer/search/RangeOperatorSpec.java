/*
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
 * Specification for a range operator in search requests.
 * This operator defines a range with a start and end value,
 * allowing for filtering results based on a range of values.
 */
public class RangeOperatorSpec implements OperatorSpec {

    /**
     * Adds fields to the specified TypeSpec builder for a range operator.
     */
    @Override
    public void addFields(TypeSpec.Builder cls, String prop, TypeName type) {
        TypeName t = TypeNames.simple(type);
        String start = prop + "Start";
        String end = prop + "End";
        cls.addField(t, start, Modifier.PRIVATE);
        cls.addMethod(SearchAccessorUtil.getter(start, t));
        cls.addMethod(SearchAccessorUtil.setter(start, t));
        cls.addField(t, end, Modifier.PRIVATE);
        cls.addMethod(SearchAccessorUtil.getter(end, t));
        cls.addMethod(SearchAccessorUtil.setter(end, t));
    }
}

