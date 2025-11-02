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
package nl.datasteel.crudcraft.sample.vehicle;

import jakarta.persistence.Entity;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;

/**
 * Concrete Toyota entity that extends Car.
 * This should generate full CRUD operations including inherited fields from Car.
 */
@Entity
@CrudCrafted
public class Toyota extends Car {

    @Dto
    @Request
    private Boolean hybrid;

    @Dto
    @Request
    private Integer fuelEfficiency;

    @Dto
    @Request
    private String safetyRating;

    public Boolean getHybrid() {
        return hybrid;
    }

    public void setHybrid(Boolean hybrid) {
        this.hybrid = hybrid;
    }

    public Integer getFuelEfficiency() {
        return fuelEfficiency;
    }

    public void setFuelEfficiency(Integer fuelEfficiency) {
        this.fuelEfficiency = fuelEfficiency;
    }

    public String getSafetyRating() {
        return safetyRating;
    }

    public void setSafetyRating(String safetyRating) {
        this.safetyRating = safetyRating;
    }
}
