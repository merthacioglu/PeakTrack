package org.mhacioglu.peaktrackserver.controller;

import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.mhacioglu.peaktrackserver.repository.UserRepository;
import org.mhacioglu.peaktrackserver.repository.WorkoutRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/workout")
public class WorkoutController {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public WorkoutController(WorkoutRepository workoutRepository,
                             UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;

    }

    @GetMapping("/getAll")
    public List<Workout> all() {
        List<Workout> workouts = new ArrayList<>();
        workoutRepository.findAll().forEach(workouts::add);
        return workouts;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workout> one(@PathVariable long id) {
        Optional<Workout> workout = workoutRepository.findById(id);
        return workout.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(("/user/{userId}"))
    public ResponseEntity<List<Workout>> allByUserId(@PathVariable long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
        List<Workout> workouts = workoutRepository.findAllByUserId(userId);
        return new ResponseEntity<>(workouts, HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Workout create(@RequestBody Workout workout) {
        return workoutRepository.save(workout);
    }



}
