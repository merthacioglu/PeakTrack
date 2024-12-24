package org.mhacioglu.peaktrackserver.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkoutSummary {
    private String workoutName;
    private LocalDateTime workoutStart;
    private int workoutDuration;

}
