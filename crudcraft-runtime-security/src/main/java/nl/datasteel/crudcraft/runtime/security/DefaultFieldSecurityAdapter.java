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

package nl.datasteel.crudcraft.runtime.security;

import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;


/** Default adapter binding runtime-core SPI to FieldSecurityUtil. */
public class DefaultFieldSecurityAdapter implements FieldSecurityAdapter {

    /** Creates a default field security adapter. */
    public DefaultFieldSecurityAdapter() {
        // Constructor without any parameters stays empty
    }

    @Override
    public <T> T filterRead(T dto) {
        return FieldSecurityUtil.filterRead(dto);
    }

    @Override
    public <T> T filterWrite(T request, Object existing) {
        return FieldSecurityUtil.filterWrite(request, existing);
    }

    @Override
    public boolean canReadField(Class<?> dtoType, String fieldName) {
        return FieldSecurityUtil.canReadField(dtoType, fieldName);
    }
}
