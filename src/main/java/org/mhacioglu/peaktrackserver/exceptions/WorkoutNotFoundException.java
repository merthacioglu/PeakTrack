package org.mhacioglu.peaktrackserver.exceptions;

public class WorkoutNotFoundException extends WorkoutException{

    public WorkoutNotFoundException(Long workoutId) {
        super(String.format("Workout with id %d does not exist", workoutId));
    }

}
