package org.mhacioglu.peaktrackserver.exceptions;

import java.time.LocalDateTime;

public class WorkoutTimeConflictException extends WorkoutException{

    public WorkoutTimeConflictException( Long id1, Long id2,
                                         LocalDateTime start1, LocalDateTime start2,
                                         LocalDateTime end1, LocalDateTime end2) {
        super(String.format("""
                        Timing conflict detected\s
                        Workout with ID: %d ---> %s - %s\s
                        Workout with ID: %d ---> %s - %s""",
                id1, start1, end1, id2, start2, end2));

    }

    public WorkoutTimeConflictException(String message) {
        super(message);
    }
}
