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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;

@Entity
@CrudCrafted
@Schema(description = "Concrete Porsche entity extending Car.",
        requiredProperties = {"engineType", "horsepower", "turboCharged"})
@DiscriminatorValue("PORSCHE")
@Table(name = "porsche")
public class Porsche extends Car {

    @Dto
    @Request
    @Schema(description = "Type of engine used in the Porsche.", example = "V8 Twin Turbo")
    private String engineType;

    @Dto
    @Request
    @Schema(description = "Horsepower (metric).", example = "450", minimum = "0")
    private Integer horsepower;

    @Dto
    @Request
    @Schema(description = "Whether this Porsche is turbocharged.", example = "true")
    private Boolean turboCharged;

    @Dto
    @Request
    @OneToMany(mappedBy = "porsche", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "List of option packages available for this Porsche.")
    @JsonIgnore
    private Set<PorscheOption> options = new HashSet<>();

    // ---- Getters & Setters ----
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

    public Set<PorscheOption> getOptions() {
        return options;
    }

    public void setOptions(Set<PorscheOption> options) {
        this.options = options;
    }
}
