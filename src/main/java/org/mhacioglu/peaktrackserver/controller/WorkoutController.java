package org.mhacioglu.peaktrackserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.mhacioglu.peaktrackserver.model.WorkoutSummary;
import org.mhacioglu.peaktrackserver.service.UserService;
import org.mhacioglu.peaktrackserver.service.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping( value = "/api/workout" )
public class WorkoutController {

    private final WorkoutService workoutService;
    private final UserService userService;

    public WorkoutController(UserService userService,
                             WorkoutService workoutService) {
        this.workoutService = workoutService;
        this.userService = userService;

    }

    @Operation(
            summary = "Get all workouts for authenticated user",
            description = "Retrieves all workout records associated with the currently " +
                    "authenticated user. Workouts are returned in chronological order " +
                    "from newest to oldest."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the list of workouts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = Workout.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is unauthenticated",
                    content = @Content
            )
    })
    @GetMapping(value = "/getAll")
    public ResponseEntity<List<Workout>> all() {
        User currentUser = userService.getCurrentUser();
        List<Workout> workouts = currentUser.getWorkouts();
        workouts.sort(Comparator.comparing(Workout::getStart));
        return new ResponseEntity<>(workouts, HttpStatus.OK);
    }

    @Operation(
            summary = "Get active workouts for authenticated user",
            description = "Retrieves all unfinished workouts scheduled for the currently " +
                    "authenticated user. The response is in chronological order from most recent to" +
                    "the latest workout. An active workout is defined as one where " +
                    "the start time and duration indicate a future time period. " +
                    "This helps users track their upcoming workout schedule."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the list of active workouts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = Workout.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    @GetMapping(value = "/getActiveWorkouts")
    public ResponseEntity<List<Workout>> activeWorkouts() {
        User currentUser = userService.getCurrentUser();
        List<Workout> workouts = currentUser.getWorkouts().stream()
                .filter(w -> LocalDateTime.now().isBefore(w.getStart().plusMinutes(w.getDurationInMinutes())))
                .sorted(Comparator.comparing(Workout::getStart)).toList();

        return new ResponseEntity<>(workouts, HttpStatus.OK);
    }

    @Operation(
            summary = "Generate workout progress report",
            description = "Retrieves a comprehensive summary of all completed workouts for the " +
                    "currently authenticated user. This endpoint helps users track their " +
                    "fitness progress by providing detailed information about past workouts " +
                    "The summaries are returned in chronological order, from most recent to oldest."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated the workout progress report",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = WorkoutSummary.class,
                                    description = "List of workout summaries containing performance metrics and completion details"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    @GetMapping(value = "/generateReport")
    public ResponseEntity<List<WorkoutSummary>> report() {
        User currentUser = userService.getCurrentUser();
        return new ResponseEntity<>(workoutService.listAllPastWorkouts(currentUser), HttpStatus.OK);
    }




    @Operation(
            summary = "Create a new workout",
            description = "Creates a new workout for the currently authenticated user. " +
                    "The system validates that the new workout's time period doesn't " +
                    "conflict with any existing workouts. A conflict occurs when the " +
                    "new workout's time period overlaps with another workout's start " +
                    "and end times. The workout must include a valid start time and duration."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Workout successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Workout.class,
                                    description = "The newly created workout with generated ID and user information"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid workout data provided (e.g., missing start time)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error (e.g., workout time conflict with existing workout)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping(path = "/create", consumes = "application/json")
    public ResponseEntity<Workout> create(@RequestBody Workout workout) {
        User currentUser = userService.getCurrentUser();
        Workout newWorkout = workoutService.addWorkout(workout, currentUser);
        return new ResponseEntity<>(newWorkout, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Delete a workout",
            description = "Deletes a workout belonging to the authenticated user." +
                    " Returns 404 if workout is not found under user's workouts.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Workout successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout not found under user's workouts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication is required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
    })
    @DeleteMapping("/delete/{workoutId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("workoutId") Long workoutId) {
        User currentUser = userService.getCurrentUser();
        workoutService.deleteWorkout(workoutId, currentUser);
    }


    @Operation(
            summary = "Update an existing workout",
            description = "Updates a workout belonging to the authenticated user. " +
                    "Partial updates are supported - only provided fields will be updated.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Workout.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data - Workout ID missing, " +
                            "invalid start time, or time conflict with other workouts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout not found under user's workouts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication is required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PutMapping(value = "/update", consumes = "application/json")
    public ResponseEntity<Workout> update (@RequestBody Workout workout) {
        User currentUser = userService.getCurrentUser();
        return new ResponseEntity<>(workoutService.updateWorkout(workout, currentUser), HttpStatus.OK);
    }






}
