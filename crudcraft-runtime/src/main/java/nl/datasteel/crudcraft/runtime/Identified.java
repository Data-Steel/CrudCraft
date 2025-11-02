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

package nl.datasteel.crudcraft.runtime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Envelope pairing an identifier with a DTO payload for batch operations.
 *
 * @param <ID> identifier type
 * @param <T>  payload type
 */
public class Identified<ID, T> {

    @NotNull
    private ID id;

    @NotNull
    @Valid
    private T data;

    public Identified() {
    }

    public Identified(ID id, T data) {
        this.id = id;
        this.data = data;
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
