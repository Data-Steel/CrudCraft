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
import java.math.BigDecimal;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;

@CrudCrafted
@Entity
@Table(name = "porsche_option")
@Schema(description = "Represents a configuration option or package specific to Porsche cars.")
public class PorscheOption {

    @Id
    @Dto(ref = true)
    @GeneratedValue
    private UUID id;

    @Dto(value = {"List"})
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "porsche_id", nullable = false)
    @Schema(description = "The Porsche associated with this option.")
    private Porsche porsche;

    @Dto(ref = true)
    @Request
    @Schema(description = "Unique code for the option.", example = "X50")
    @Column(length = 64, nullable = false)
    private String code;

    @Dto
    @Request
    @Schema(description = "Description of the option.", example = "Powerkit with upgraded turbo and ECU.")
    @Column(length = 1000)
    private String description;

    @Dto(ref = true)
    @Request
    @Schema(description = "Price of the option.", example = "12950.00", format = "decimal")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    // ---- Getters & Setters ----
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Porsche getPorsche() {
        return porsche;
    }

    public void setPorsche(Porsche porsche) {
        this.porsche = porsche;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
