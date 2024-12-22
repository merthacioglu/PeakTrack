package org.mhacioglu.peaktrackserver.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mhacioglu.peaktrackserver.exceptions.InvalidWorkoutDataException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutNotFoundException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutTimeConflictException;
import org.mhacioglu.peaktrackserver.model.Exercise;
import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.mhacioglu.peaktrackserver.repository.WorkoutRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class WorkoutServiceTest {
    @Mock
    private WorkoutRepository workoutRepository;

    @InjectMocks
    private WorkoutService workoutService;

    private static User currentUser;
    private static List<Workout> workouts;

    @BeforeAll
    public static void setup() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        Workout w1 = Workout.builder()
                .id(101L)
                .name("Morning Workout")
                .start(LocalDateTime.of(2024, 2, 20, 8, 0))
                .durationInMinutes(60)
                .user(currentUser)
                .build();

        Workout w2 = Workout.builder()
                .id(102L)
                .name("Evening Workout")
                .start(LocalDateTime.of(2024, 2, 20, 20, 30))
                .durationInMinutes(45)
                .user(currentUser)
                .build();

        Exercise e1 = createTestExercise("Bench Press", "Chest exercise using weights",
                Exercise.Category.FLEX, Exercise.MuscleGroup.ABS, 3, 1, 60);
        Exercise e2 = createTestExercise("Jump Rope", "High-intensity cardio workout",
                Exercise.Category.CARDIO,null, 1, 20, 0);
        Exercise e3 = createTestExercise("Lunges", "Leg and glute exercise",
                Exercise.Category.STRENGTH, Exercise.MuscleGroup.GLUTES, 4, 12, 40);

        w1.setExercises(List.of(e1, e2, e3));

        Exercise e4 = createTestExercise("Plank", "Core strengthening exercise",
                Exercise.Category.FLEX, Exercise.MuscleGroup.ABS, 3, 1, 0);
        Exercise e5 = createTestExercise("Deadlift", "Strength exercise for lower back and legs",
                Exercise.Category.STRENGTH, Exercise.MuscleGroup.HAMSTRINGS, 3, 8, 80);

        w2.setExercises(List.of(e4, e5));

        workouts = List.of(w1, w2);


    }

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




    @Test
    @DisplayName("Test scenario: get all workouts of the current user")
    public void getAllWorkouts_ShouldReturnAllWorkouts() {
        when(workoutRepository.findAllByUserId(currentUser.getId())).thenReturn(workouts);
        List<Workout> returnedWorkouts = workoutService.getAllWorkouts(currentUser);
        assertEquals(returnedWorkouts.size(), workouts.size());
    }

    @Test
    @DisplayName("Test scenario: get all workouts that will end in future sorted by start time")
    public void getActiveWorkouts_ShouldReturnsWorkoutEndsInFutureSortedByStart() {
        Workout w1 = Workout.builder()
                .start(LocalDateTime.now().minusMinutes(20))
                .name("Ongoing Workout")
                .durationInMinutes(50)
                .build();

        Workout w2 = Workout.builder()
                .start(LocalDateTime.of(2033, 10, 18, 16, 35))
                .name("Upcoming Workout")
                .durationInMinutes(30)
                .user(currentUser)
                .build();

        Workout w3 = Workout.builder()
                .start(LocalDateTime.of(2023, 10, 18, 16, 35))
                .name("Finished Workout")
                .durationInMinutes(30)
                .user(currentUser)
                .build();

        when(workoutRepository.findAllByUserIdOrderByStartAsc(currentUser.getId()))
                .thenReturn(List.of(w3, w1, w2));


        List<Workout> activeWorkouts = workoutService.getAllActiveWorkouts(currentUser);

        assertNotNull(activeWorkouts);
        assertEquals(activeWorkouts.size(), 2);
        assertTrue(activeWorkouts.stream().allMatch(
                w -> LocalDateTime.now().isBefore(w.getStart().plusMinutes(w.getDurationInMinutes()))
        ));
        // checking if all the returned workouts will end in future

        assertThat(activeWorkouts).isSortedAccordingTo(Comparator.comparing(Workout::getStart));
        // checking if the returned workouts are sorted by start time from soonest to latest

    }


    @Test
    @DisplayName("Test scenario: add a workout to the list of workouts of the current user")
    public void addWorkout_NoProblem() {
        Workout newWorkout = Workout.builder()
                .id(103L)
                .name("Afternoon Workout")
                .start(LocalDateTime.of(2024, 2, 20, 16, 0))
                .durationInMinutes(60)
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId())).thenReturn(workouts);
        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.<Workout>getArgument(0));

        Workout savedWorkout = workoutService.addWorkout(newWorkout, currentUser);

        // Assert
        // Verify the workout was saved
        verify(workoutRepository).save(newWorkout);

        // Verify the user was set correctly
        assertEquals(currentUser, savedWorkout.getUser());

        // Verify the workout details were preserved
        assertEquals(newWorkout.getName(), savedWorkout.getName());
        assertEquals(newWorkout.getStart(), savedWorkout.getStart());
        assertEquals(newWorkout.getDurationInMinutes(), savedWorkout.getDurationInMinutes());
    }

    @Test
    @DisplayName("Test scenario: add workout with conflicting schedule")
    public void addWorkout_ShouldThrowException_WhenThereIsTimingConflict() {

        Workout newWorkout = Workout.builder()
                .id(103L)
                .name("Another morning Workout")
                .start(LocalDateTime.of(2024, 2, 20, 7, 30))
                .durationInMinutes(60)
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId())).thenReturn(workouts);

        assertThrows(WorkoutTimeConflictException.class,
                () -> workoutService.addWorkout(newWorkout, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));


    }

    @Test
    @DisplayName("Test scenario: update the properties of a workout")
    public void updateWorkout_NoProblem() {
        Workout update = Workout.builder()
                .id(101L)
                .name("Another morning Workout")
                .start(LocalDateTime.of(2024, 2, 20, 7, 30))
                .durationInMinutes(75)
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId())).thenReturn(workouts);
        when(workoutRepository.save(any(Workout.class))).
                thenAnswer(invocation -> invocation.<Workout>getArgument(0));

        Workout savedWorkout = workoutService.updateWorkout(update, currentUser);

        update.setUser(currentUser);
        verify(workoutRepository, atMost(1)).save(update);
        assertEquals(currentUser.getId(), savedWorkout.getUser().getId());
        assertEquals(update.getName(), savedWorkout.getName());
        assertEquals(update.getStart(), savedWorkout.getStart());
    }

    @Test
    @DisplayName("Test scenario: add one exercise to the workout with given id")
    public void updateWorkout_ShouldAddNewExercise_WhenExerciseListIsUpdated() {
        Workout update = new Workout();
        update.setId(102L);

        List<Exercise> exercises = new ArrayList<>(workouts.get(1).getExercises());
        exercises.add(createTestExercise("Running", "Cardio exercise for endurance",
                Exercise.Category.CARDIO, null, 1, 30, 0));

        update.setExercises(exercises);
        when(workoutRepository.findAllByUserId(currentUser.getId())).thenReturn(workouts);
        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.<Workout>getArgument(0));

        Workout updatedWorkout = workoutService.updateWorkout(update, currentUser);
        assertNotNull(updatedWorkout.getExercises());
        assertEquals(3, updatedWorkout.getExercises().size());
        assertTrue(updatedWorkout.getExercises().stream().anyMatch(w -> w.getName().equals("Running")));
        verify(workoutRepository, atMost(1)).save(update);
    }

    @Test
    @DisplayName("Test scenario: update workout to have scheduling conflict with another workout")
    public void updateWorkout_ShouldThrowException_WhenThereIsTimingConflict() {
        Workout change = Workout.builder()
                .id(101L)
                .name("Another Evening Workout")
                .start(LocalDateTime.of(2024, 2, 20, 20, 30))
                .durationInMinutes(75)
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId())).thenReturn(workouts);
        assertThrows(WorkoutTimeConflictException.class,
                () -> workoutService.updateWorkout(change, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));

    }

    @Test
    @DisplayName("Test scenario: update workout " +
            "whose id is not in the id list of the current user's workouts")
    public void updateWorkout_ShouldThrowException_WhenWorkoutDoesNotBelongToTheCurrentUser() {
        Workout change = Workout.builder()
                .id(103L)
                .name("Another Morning Workout")
                .start(LocalDateTime.of(2024, 2, 21, 8, 0))
                .durationInMinutes(75)
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId())).thenReturn(workouts);
        assertThrows(WorkoutNotFoundException.class,
                () -> workoutService.updateWorkout(change, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));

    }

    @Test
    @DisplayName("Test scenario: update workout without giving an id")
    public void updateWorkout_ShouldThrowException_WhenWorkoutDoesNotHaveID() {
        Workout change = Workout.builder()
                .name("Another Morning Workout")
                .start(LocalDateTime.of(2024, 2, 21, 8, 0))
                .durationInMinutes(45)
                .build();

        assertThrows(InvalidWorkoutDataException.class,
                () -> workoutService.updateWorkout(change, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));
    }

    @Test
    @DisplayName("Test scenario: delete a workout from the user's workout list")
    public void deleteWorkout_NoProblem() {
        when(workoutRepository.findAllByUserId(currentUser.getId())).thenReturn(workouts);
        workoutService.deleteWorkout(101L, currentUser);

        verify(workoutRepository, atMost(1)).deleteById(101L);
        verify(workoutRepository, atMost(1)).findAllByUserId(currentUser.getId());
    }

    @Test
    @DisplayName("Test scenario: delete a workout which is not in the user's workouts")
    public void  deleteWorkout_ShouldThrowException_WhenWorkoutDoesNotBelongToTheCurrentUser() {
        when(workoutRepository.findAllByUserId(currentUser.getId())).thenReturn(workouts);
        assertThrows(WorkoutNotFoundException.class,
                () -> workoutService.deleteWorkout(103L, currentUser));

        verify(workoutRepository, never()).deleteById(any(Long.class));
    }

}