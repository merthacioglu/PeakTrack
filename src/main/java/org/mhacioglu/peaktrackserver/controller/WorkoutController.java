package org.mhacioglu.peaktrackserver.controller;

import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.mhacioglu.peaktrackserver.service.UserService;
import org.mhacioglu.peaktrackserver.service.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/getAll")
    public ResponseEntity<List<Workout>> all() {
        User currentUser = userService.getCurrentUser();
        return new ResponseEntity<>(workoutService.getAllWorkouts(currentUser), HttpStatus.OK);
    }

    @GetMapping(value = "/getActiveWorkouts")
    public ResponseEntity<List<Workout>> activeWorkouts() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(workoutService.getAllActiveWorkouts(currentUser));
    }


    @PostMapping(path = "/create", consumes = "application/json")
    public ResponseEntity<Workout> create(@RequestBody Workout workout) {
        User currentUser = userService.getCurrentUser();
        Workout newWorkout = workoutService.addWorkout(workout, currentUser);
        return new ResponseEntity<>(newWorkout, HttpStatus.CREATED);
    }


    @DeleteMapping("/delete/{workoutId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("workoutId") Long workoutId) {
        User currentUser = userService.getCurrentUser();
        workoutService.deleteWorkout(workoutId, currentUser);
    }

    @PutMapping(value = "/update", consumes = "application/json")
    public ResponseEntity<Workout> update (@RequestBody Workout workout) {
        User currentUser = userService.getCurrentUser();
        return new ResponseEntity<>(workoutService.updateWorkout(workout, currentUser), HttpStatus.OK);
    }






}
