package org.mhacioglu.peaktrackserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.mhacioglu.peaktrackserver.model.RegisteredUser;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.mhacioglu.peaktrackserver.model.WorkoutSummary;
import org.mhacioglu.peaktrackserver.service.UserService;
import org.mhacioglu.peaktrackserver.service.WorkoutService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(value = "/api/workout")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final UserService userService;

    public WorkoutController(UserService userService,
                             WorkoutService workoutService) {
        this.workoutService = workoutService;
        this.userService = userService;

    }

    @Operation(
            summary = "Get workouts within a date range",
            description = "Retrieves all workouts for the current user, optionally filtered by start and end dates. " +
                    "If dates are provided, workouts are filtered to those starting between the given dates. " +
                    "Results are sorted by start date in descending order (newest first)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Workouts successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Workout.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized to access workouts",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping(value = "/all")
    public ResponseEntity<List<Workout>> all(
            @Parameter(
                    description = "Start date-time to filter workouts (inclusive). Format: yyyy-MM-dd HH:mm",
                    example = "2024-01-09 14:30"
            )
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime from,

            @Parameter(
                    description = "End date-time to filter workouts (inclusive). Format: yyyy-MM-dd HH:mm",
                    example = "2024-01-09 16:30"
            )
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime to) {

        RegisteredUser registeredUser = userService.getCurrentUser();
        List<Workout> workouts = workoutService.getWorkoutsBetween(from, to, registeredUser);
        return new ResponseEntity<>(workouts, HttpStatus.OK);
    }

    @Operation(
            summary = "Generate workout summary report",
            description = "Generates a comprehensive report of all past workouts for the currently authenticated user. " +
                    "The report includes summarized information about each completed workout, making it suitable " +
                    "for analysis and review of workout history. Each summary provides key metrics and statistics " +
                    "about the workout session."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Report successfully generated",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = WorkoutSummary.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized to access workout data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error while generating report",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping(value = "/generateReport")
    public ResponseEntity<List<WorkoutSummary>> report() {
        RegisteredUser currentRegisteredUser = userService.getCurrentUser();
        return new ResponseEntity<>(workoutService.listAllPastWorkouts(currentRegisteredUser), HttpStatus.OK);
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
        RegisteredUser currentRegisteredUser = userService.getCurrentUser();
        Workout newWorkout = workoutService.addWorkout(workout, currentRegisteredUser);
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
        RegisteredUser currentRegisteredUser = userService.getCurrentUser();
        workoutService.deleteWorkout(workoutId, currentRegisteredUser);
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
    public ResponseEntity<Workout> update(@RequestBody Workout workout) {
        RegisteredUser currentRegisteredUser = userService.getCurrentUser();
        return new ResponseEntity<>(workoutService.updateWorkout(workout, currentRegisteredUser), HttpStatus.OK);
    }


}
