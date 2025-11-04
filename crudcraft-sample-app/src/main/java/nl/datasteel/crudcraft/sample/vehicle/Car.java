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
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "car")
@CrudCrafted
@Schema(description = "Abstract base class for all car types.")
public abstract class Car {

    @Id
    @GeneratedValue
    private UUID id;

    @Dto
    @Schema(description = "Model name of the car.", example = "911")
    private String model;

    @Dto
    @Column(name = "model_year")
    @Schema(description = "Production year of the car.", example = "2025")
    private Integer modelYear;

    @Dto
    @Schema(description = "Exterior color of the car.", example = "Guards Red")
    private String color;

    // ✅ One-to-many to Inspection (shared for all car types)
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "All inspections performed on this car.")
    @JsonIgnore
    private Set<Inspection> inspections = new HashSet<>();

    // ---- Getters & Setters ----
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Set<Inspection> getInspections() {
        return inspections;
    }

    public void setInspections(Set<Inspection> inspections) {
        this.inspections = inspections;
    }
}
