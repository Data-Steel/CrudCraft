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
package nl.datasteel.crudcraft.codegen.descriptor.model.part;

/**
 * Represents boolean flags that describe model characteristics.
 *
 * @param editable whether generated stubs are editable
 * @param crudCraftEntity whether this model is a CrudCraft entity
 * @param embeddable whether this model is marked as embeddable
 * @param abstractClass whether this model is an abstract class
 */
public record ModelFlags(boolean editable, boolean crudCraftEntity, boolean embeddable, boolean abstractClass) {

    /**
     * Returns true if the generated stubs are editable.
     *
     * @return true if editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Returns true if this model is a CrudCraft entity.
     *
     * @return true if CrudCraft entity
     */
    public boolean isCrudCraftEntity() {
        return crudCraftEntity;
    }

    /**
     * Returns true if this model is marked as embeddable.
     *
     * @return true if embeddable
     */
    public boolean isEmbeddable() {
        return embeddable;
    }

    /**
     * Returns true if this model is an abstract class.
     *
     * @return true if abstract
     */
    public boolean isAbstract() {
        return abstractClass;
    }
}
