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
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    @Dto
    @Request
    @Schema(description = "The car that this inspection belongs to.")
    private Car car;

    @Dto
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
