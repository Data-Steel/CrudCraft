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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;

@Entity
@Table(name = "inspection")
@CrudCrafted
@Schema(description = "Represents an inspection or maintenance record for a car.")
public class Inspection {

    @Id
    @Dto(ref = true)
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    @Dto
    @Request
    @Schema(description = "The car that this inspection belongs to.")
    private Car car;

    @Dto(ref = true)
    @Request
    @Schema(description = "Date of inspection.", example = "2025-11-03")
    private LocalDate performedOn;

    @Dto
    @Request
    @Schema(description = "Notes or tasks performed during this inspection.")
    @Column(length = 2000)
    private String notes;

    // ---- Getters & Setters ----
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public LocalDate getPerformedOn() {
        return performedOn;
    }

    public void setPerformedOn(LocalDate performedOn) {
        this.performedOn = performedOn;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
