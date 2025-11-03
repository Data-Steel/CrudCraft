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

    // ✅ One-to-many to PorscheOption (specific for Porsche)
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
