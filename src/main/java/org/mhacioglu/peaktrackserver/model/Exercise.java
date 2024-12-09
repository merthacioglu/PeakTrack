package org.mhacioglu.peaktrackserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
public class Exercise implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private MuscleGroup muscleGroup;


    @NotNull
    @Min(1)
    private int sets;

    @NotNull
    @Min(1)
    private int repetitions;

    private int weight;




    @NotNull
    private enum Category {
        CARDIO, STRENGTH, FLEX, BALANCE
    }


    private enum MuscleGroup {
        CHEST, BACK, SHOULDERS, ARMS,
        QUADS, HAMSTRINGS, GLUTES, CALVES,
        ABS, OBLIQUES
    }


}
