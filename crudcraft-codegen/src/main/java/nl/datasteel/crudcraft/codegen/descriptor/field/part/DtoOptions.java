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

package nl.datasteel.crudcraft.codegen.descriptor.field.part;

import java.util.Arrays;
import java.util.Objects;


/** Represents the options for a field regarding Data Transfer Objects (DTOs). */
public final class DtoOptions {
    private final boolean inDto;
    private final boolean inRequest;
    private final boolean inRef;
    private final String[] responseDtos;
    private final boolean isLob;

    /**
     * Creates DTO options for a field.
     *
     * @param inDto whether the field is included in the DTO
     * @param inRequest whether the field is included in the request DTO
     * @param inRef whether the field is included in the reference DTO
     * @param responseDtos names of additional response DTO variants this field participates in
     * @param isLob whether the field is a large object (Jakarta {@code @Lob})
     */
    public DtoOptions(
            boolean inDto, boolean inRequest, boolean inRef, String[] responseDtos, boolean isLob) {
        this.inDto = inDto;
        this.inRequest = inRequest;
        this.inRef = inRef;
        this.responseDtos =
                responseDtos == null ? null : Arrays.copyOf(responseDtos, responseDtos.length);
        this.isLob = isLob;
    }

    /**
     * Returns whether the field should be included in the DTO.
     *
     * @return {@code true} when included in response DTO
     */
    public boolean isInDto() {
        return inDto;
    }

    /**
     * Returns whether the field should be included in the request DTO.
     *
     * @return {@code true} when included in request DTO
     */
    public boolean isInRequest() {
        return inRequest;
    }

    /**
     * Returns whether the field should be included in the reference DTO.
     *
     * @return {@code true} when included in ref DTO
     */
    public boolean isInRef() {
        return inRef;
    }

    /**
     * Returns the names of additional response DTO variants this field participates in.
     *
     * @return additional response DTO names
     */
    public String[] responseDtos() {
        return responseDtos == null ? null : Arrays.copyOf(responseDtos, responseDtos.length);
    }

    /**
     * Returns the names of additional response DTO variants this field participates in.
     *
     * @return additional response DTO names
     */
    public String[] getResponseDtos() {
        return responseDtos == null ? null : Arrays.copyOf(responseDtos, responseDtos.length);
    }

    /**
     * Returns whether the field is a large object (Jakarta @Lob).
     *
     * @return {@code true} when field is a LOB
     */
    public boolean isLob() {
        return isLob;
    }

    /**
     * Returns whether the field is included in the DTO.
     *
     * @return {@code true} when included in the DTO
     */
    public boolean inDto() {
        return inDto;
    }

    /**
     * Returns whether the field is included in the request DTO.
     *
     * @return {@code true} when included in the request DTO
     */
    public boolean inRequest() {
        return inRequest;
    }

    /**
     * Returns whether the field is included in the reference DTO.
     *
     * @return {@code true} when included in the reference DTO
     */
    public boolean inRef() {
        return inRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DtoOptions that)) {
            return false;
        }

        if (inDto != that.inDto) {
            return false;
        }
        if (inRequest != that.inRequest) {
            return false;
        }
        if (inRef != that.inRef) {
            return false;
        }
        if (isLob != that.isLob) {
            return false;
        }
        return Arrays.equals(responseDtos, that.responseDtos);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(inDto, inRequest, inRef, isLob);
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
        return "DtoOptions{"
                + "inDto="
                + inDto
                + ", inRequest="
                + inRequest
                + ", inRef="
                + inRef
                + ", responseDtos="
                + Arrays.toString(responseDtos)
                + ", isLob="
                + isLob
                + '}';
    }
}
