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

package nl.datasteel.crudcraft.sample.vehicle;

import jakarta.persistence.Entity;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;

/**
 * Concrete Porsche entity that extends Car.
 * This should generate full CRUD operations including inherited fields from Car.
 */
@Entity
@CrudCrafted
public class Porsche extends Car {

    @Dto
    @Request
    private String engineType;

    @Dto
    @Request
    private Integer horsepower;

    @Dto
    @Request
    private Boolean turboCharged;

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public Integer getHorsepower() {
        return horsepower;
    }

    public void setHorsepower(Integer horsepower) {
        this.horsepower = horsepower;
    }

    public Boolean getTurboCharged() {
        return turboCharged;
    }

    public void setTurboCharged(Boolean turboCharged) {
        this.turboCharged = turboCharged;
    }
}
