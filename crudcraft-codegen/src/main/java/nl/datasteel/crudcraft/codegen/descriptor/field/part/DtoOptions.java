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
package nl.datasteel.crudcraft.codegen.descriptor.field.part;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the options for a field regarding Data Transfer Objects (DTOs).
 * This includes whether the field should be included in the DTO, request DTO, and reference DTO.
 *
 * @param inDto whether the field is included in the DTO
 * @param inRequest whether the field is included in the request DTO
 * @param inRef whether the field is included in the reference DTO
 * @param responseDtos names of additional response DTO variants this field participates in
 */
public record DtoOptions(boolean inDto, boolean inRequest, boolean inRef, String[] responseDtos) {
    /**
     * Returns whether the field should be included in the DTO.
     */
    public boolean isInDto() {
        return inDto;
    }

    /**
     * Returns whether the field should be included in the request DTO.
     */
    public boolean isInRequest() {
        return inRequest;
    }

    /**
     * Returns whether the field should be included in the reference DTO.
     */
    public boolean isInRef() {
        return inRef;
    }

    /**
     * Returns the names of additional response DTO variants this field participates in.
     */
    public String[] getResponseDtos() {
        return responseDtos;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DtoOptions that)) return false;

        if (inDto != that.inDto) return false;
        if (inRequest != that.inRequest) return false;
        if (inRef != that.inRef) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(responseDtos, that.responseDtos);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(inDto, inRequest, inRef);
        result = 31 * result + Arrays.hashCode(responseDtos);
        return result;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "DtoOptions{" +
                "inDto=" + inDto +
                ", inRequest=" + inRequest +
                ", inRef=" + inRef +
                ", responseDtos=" + Arrays.toString(responseDtos) +
                '}';
    }
}
