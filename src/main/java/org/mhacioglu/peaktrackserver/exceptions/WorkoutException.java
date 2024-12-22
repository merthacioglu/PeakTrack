package org.mhacioglu.peaktrackserver.exceptions;

public class WorkoutException extends RuntimeException {
    public WorkoutException(String message) {
        super(message);
    }

    public WorkoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
