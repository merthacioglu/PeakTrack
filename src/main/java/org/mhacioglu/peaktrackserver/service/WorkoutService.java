package org.mhacioglu.peaktrackserver.service;

import org.mhacioglu.peaktrackserver.exceptions.InvalidWorkoutDataException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutNotFoundException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutTimeConflictException;
import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.mhacioglu.peaktrackserver.model.WorkoutSummary;
import org.mhacioglu.peaktrackserver.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class  WorkoutService {
    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    public List<Workout> getAllWorkouts(User user) {
        return workoutRepository.findAllByUserIdOrderByStartDesc(user.getId());
    }

    public List<Workout> getAllActiveWorkouts(User user) {
        return workoutRepository.findAllByUserIdOrderByStartAsc(user.getId())
                .stream().filter(w -> LocalDateTime.now().isBefore(w.getStart().plusMinutes(w.getDurationInMinutes())))
                .toList();
    }

    public List<WorkoutSummary> listAllPastWorkouts(User user) {
        List<Workout> pastWorkouts = workoutRepository.findAllByUserIdOrderByStartDesc(user.getId()).
                stream().filter(w -> LocalDateTime.now().isAfter(w.getStart().plusMinutes(w.getDurationInMinutes())))
                .toList();

        return pastWorkouts.stream().map(workout -> WorkoutSummary.builder()
                .workoutName(workout.getName())
                .workoutStart(workout.getStart())
                .workoutDuration(workout.getDurationInMinutes())
                .build()).toList();


    }

    public Workout addWorkout(Workout workout, User user) {
        List<Workout> workouts = workoutRepository.findAllByUserId(user.getId());
        if (checkIfWorkoutTimeIsValid(workouts, workout)) {
            workout.setUser(user);
        }

        return workoutRepository.save(workout);

    }

    public void deleteWorkout(Long workoutId, User user) {
        List<Workout> workouts = workoutRepository.findAllByUserId(user.getId());
        long count = workouts.stream().map(Workout::getId).
                filter(id -> id.longValue() == workoutId.longValue()).count();
        if (count == 0L)
            throw new WorkoutNotFoundException(workoutId);
        workoutRepository.deleteById(workoutId);

    }

    public Workout updateWorkout(Workout workout, User user) {
        if(workout.getId() == null) {
            throw new InvalidWorkoutDataException("A workout must have a valid workout id.");
        }

        Optional<Workout> optionalWorkout =  workoutRepository.findAllByUserId(user.getId()).stream().
                filter(w -> w.getId().longValue() == workout.getId().longValue()).findFirst();

        Workout existingWorkout = optionalWorkout.orElseThrow(
                () -> new WorkoutNotFoundException(workout.getId())
        );

        if(workout.getName() != null) {
            existingWorkout.setName(workout.getName());
        }

        if (workout.getDurationInMinutes() != 0) {
            existingWorkout.setDurationInMinutes(workout.getDurationInMinutes());
        }

        if (workout.getStart() != null) {
            List<Workout> otherWorkouts = workoutRepository.findAllByUserId(user.getId())
                            .stream().filter(w -> w.getId().longValue() != workout.getId().longValue()).toList();
            checkIfWorkoutTimeIsValid(otherWorkouts, workout);
            existingWorkout.setStart(workout.getStart());
            existingWorkout.setDurationInMinutes(workout.getDurationInMinutes());
        }

        if (workout.getExercises() != null) {
            existingWorkout.setExercises(workout.getExercises());
        }

        return workoutRepository.save(existingWorkout);

    }



    private boolean checkIfWorkoutTimeIsValid(List<Workout> workouts, Workout newWorkout) {
        LocalDateTime newStart = newWorkout.getStart();
        if (newStart == null) {
            throw new InvalidWorkoutDataException("A workout must have a valid start date.");
        }
        LocalDateTime newEnd = newStart.plusMinutes(newWorkout.getDurationInMinutes());
        workouts.forEach(workout -> {
            LocalDateTime start = workout.getStart();
            LocalDateTime end = start.plusMinutes(workout.getDurationInMinutes());
            if (!(newEnd.isBefore(start) || newStart.isAfter(end))) {
                throw new WorkoutTimeConflictException(workout.getId(), newWorkout.getId(),
                        start, newStart, end, newEnd);
            }
        });
        return true;
    }


}
