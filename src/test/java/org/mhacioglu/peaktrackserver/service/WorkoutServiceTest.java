package org.mhacioglu.peaktrackserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mhacioglu.peaktrackserver.exceptions.InvalidWorkoutDataException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutNotFoundException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutTimeConflictException;
import org.mhacioglu.peaktrackserver.model.Exercise;
import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.mhacioglu.peaktrackserver.model.WorkoutSummary;
import org.mhacioglu.peaktrackserver.repository.WorkoutRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class WorkoutServiceTest {
    private User currentUser;
    private Workout pastWorkout;
    private Workout futureWorkout;
    private Workout ongoingWorkout;
    @Mock
    private WorkoutRepository workoutRepository;
    @InjectMocks
    private WorkoutService workoutService;

    private static Exercise createTestExercise(String name, String desc,
                                               Exercise.Category category, Exercise.MuscleGroup muscleGroup,
                                               int sets, int repetitions, int weight) {
        return Exercise.builder().
                name(name)
                .description(desc)
                .category(category)
                .muscleGroup(muscleGroup)
                .sets(sets)
                .repetitions(repetitions)
                .weight(weight)
                .build();
    }

    @BeforeEach
    public void setup() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        pastWorkout = Workout.builder()
                .id(1L)
                .name("Past Workout")
                .start(LocalDateTime.now().minusHours(2))
                .durationInMinutes(60)
                .user(currentUser)
                .build();

        futureWorkout = Workout.builder()
                .id(2L)
                .name("Upcoming Workout")
                .start(LocalDateTime.now().plusDays(3))
                .durationInMinutes(45)
                .user(currentUser)
                .build();

        ongoingWorkout = Workout.builder()
                .id(3L)
                .name("Ongoing workout")
                .start(LocalDateTime.now().minusMinutes(30))
                .durationInMinutes(60)
                .user(currentUser)
                .build();


        currentUser.setWorkouts(new ArrayList<>(List.of(pastWorkout, futureWorkout, ongoingWorkout)));

        Exercise e1 = createTestExercise("Bench Press", "Chest exercise using weights",
                Exercise.Category.FLEX, Exercise.MuscleGroup.ABS, 3, 1, 60);
        Exercise e2 = createTestExercise("Jump Rope", "High-intensity cardio workout",
                Exercise.Category.CARDIO, null, 1, 20, 0);
        Exercise e3 = createTestExercise("Lunges", "Leg and glute exercise",
                Exercise.Category.STRENGTH, Exercise.MuscleGroup.GLUTES, 4, 12, 40);

        pastWorkout.setExercises(new ArrayList<>(List.of(e1, e2, e3)));

        Exercise e4 = createTestExercise("Plank", "Core strengthening exercise",
                Exercise.Category.FLEX, Exercise.MuscleGroup.ABS, 3, 1, 0);
        Exercise e5 = createTestExercise("Deadlift", "Strength exercise for lower back and legs",
                Exercise.Category.STRENGTH, Exercise.MuscleGroup.HAMSTRINGS, 3, 8, 80);

        futureWorkout.setExercises(new ArrayList<>(List.of(e4, e5)));


    }


    @Test
    @DisplayName("Return the list of workouts sorted by their start date which have ended in the past")
    public void listAllPastWorkouts_ShouldReturnFinishedWorkouts() {
        Workout w1 = Workout.builder()
                .id(999L)
                .name("A very old workout")
                .start(LocalDateTime.now().minusYears(50))
                .durationInMinutes(60)
                .user(currentUser)
                .build();

        Workout w2 = Workout.builder()
                .id(333L)
                .name("A recently finished workout")
                .start(LocalDateTime.now().minusDays(2))
                .durationInMinutes(60)
                .user(currentUser)
                .build();

        currentUser.addWorkout(w1);
        currentUser.addWorkout(w2);

        List<WorkoutSummary> pastWorkouts = workoutService.listAllPastWorkouts(currentUser);

        assertEquals(3, pastWorkouts.size());
        for (int i = 0; i < pastWorkouts.size() - 1; i++) {
            LocalDateTime start1 = pastWorkouts.get(i).getWorkoutStart();
            LocalDateTime start2 = pastWorkouts.get(i+1).getWorkoutStart();

            LocalDateTime end1 = start1.plusMinutes(pastWorkouts.get(i).getWorkoutDuration());
            LocalDateTime end2 = start2.plusMinutes(pastWorkouts.get(i+1).getWorkoutDuration());

            assertTrue(LocalDateTime.now().isAfter(end1) && LocalDateTime.now().isAfter(end2));
            assertTrue(start1.isAfter(start2));
        }

    }

    @Test
    @DisplayName("Return the newly added workout")
    public void addWorkout_ShouldReturnTheNewWorkout() {
        //currentUser.setWorkouts(new ArrayList<>());
        Workout req = Workout.builder()
                .name("New workout")
                .start(LocalDateTime.now().plusWeeks(2))
                .durationInMinutes(80)
                .build();

        Workout res = Workout.builder()
                .name("New workout")
                .start(LocalDateTime.now().plusWeeks(2))
                .durationInMinutes(80)
                .id(4L)
                .user(currentUser)
                .build();

        when(workoutRepository.save(any(Workout.class)))
                .thenReturn(res);

        Workout newWorkout = workoutService.addWorkout(req, currentUser);
        assertNotNull(newWorkout);
        assertEquals(4L, newWorkout.getId());
        assertEquals(res.getName(), newWorkout.getName());
        assertEquals(res.getStart(), newWorkout.getStart());
        assertEquals(4, currentUser.getWorkouts().size());
        verify(workoutRepository, times(1)).save(any(Workout.class));
    }

    @Test
    @DisplayName("Delete the workout by the given ID from the database")
    public void deleteWorkout_ShouldDeleteWorkout() {
        workoutService.deleteWorkout(pastWorkout.getId(), currentUser);
        assertEquals(2, currentUser.getWorkouts().size());
        assertFalse(currentUser.getWorkouts().stream()
                .anyMatch(workout -> workout.getId().longValue() == pastWorkout.getId().longValue()));
        assertNull(pastWorkout.getUser());
        verify(workoutRepository, times(1)).deleteById(anyLong());
    }

    @Test
    @DisplayName("Delete a workout which doesn't belong to the user")
    public void deleteWorkout_ShouldReturnWorkoutNotFoundException() {

        assertThrows(WorkoutNotFoundException.class,
                () -> workoutService.deleteWorkout(4L, currentUser));

        verify(workoutRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Update the workout with the given ID")
    public void updateWorkout_ShouldUpdateWorkout() {
        Workout req = Workout.builder()
                .id(pastWorkout.getId())
                .name("Updated name")
                .durationInMinutes(30)
                .build();

        Workout res = Workout.builder()
                .id(pastWorkout.getId())
                .name(req.getName())
                .start(pastWorkout.getStart())
                .exercises(pastWorkout.getExercises())
                .durationInMinutes(req.getDurationInMinutes())
                .user(currentUser)
                .build();

        when(workoutRepository.save(any(Workout.class)))
                .thenReturn(res);

        Workout updated = workoutService.updateWorkout(req, currentUser);
        assertNotNull(updated);
        assertEquals(res.getId(), updated.getId());
        assertEquals(res.getName(), updated.getName());
        assertEquals(res.getDurationInMinutes(), updated.getDurationInMinutes());
        verify(workoutRepository, times(1)).save(any(Workout.class));
    }

    @Test
    @DisplayName("Updating a workout which conflicts another workout in time")
    public void updateWorkout_ShouldReturnTimingConflictException() {
        Workout req = Workout.builder()
                .id(pastWorkout.getId())
                .start(LocalDateTime.now().minusMinutes(30))
                .build();

        assertThrows(WorkoutTimeConflictException.class,
               () -> workoutService.addWorkout(req, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));

    }

    @Test
    @DisplayName("Updating a workout without providing a workout ID")
    public void updateWorkout_ShouldReturnInvalidWorkoutDataException() {
        Workout req = Workout.builder()
                .name("Updated name")
                .build();

        assertThrows(InvalidWorkoutDataException.class,
                () -> workoutService.updateWorkout(req, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));
    }

}