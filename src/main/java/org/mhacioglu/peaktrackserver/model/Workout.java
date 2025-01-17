package org.mhacioglu.peaktrackserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workout implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull
    private String name;

    @NotNull
    private LocalDateTime start;

    @NotNull
    @Min(10)
    private int  durationInMinutes;

    @ManyToMany
    @JoinTable(
            name = "workout_exercises",
            joinColumns = @JoinColumn(name = "workout_id"),
            inverseJoinColumns = @JoinColumn(name = "exercise_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "pk_workout_exercises",
                    columnNames = {"workout_id", "exercise_id"}
            )
    )
    private List<Exercise> exercises;

    @ManyToOne(optional = false)
    private RegisteredUser user;

    private String comment;

}
