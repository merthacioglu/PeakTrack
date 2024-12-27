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
import org.mhacioglu.peaktrackserver.model.WorkoutSummary;
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
    private static Workout pastWorkout;
    private static Workout futureWorkout;
    private static Workout ongoingWorkout;

    @BeforeAll
    public static void setup() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        pastWorkout = Workout.builder()
                .id(101L)
                .name("Past Workout")
                .start(LocalDateTime.now().minusHours(2))
                .durationInMinutes(60)
                .user(currentUser)
                .build();

        futureWorkout = Workout.builder()
                .id(102L)
                .name("Upcoming Workout")
                .start(LocalDateTime.now().plusHours(3))
                .durationInMinutes(45)
                .user(currentUser)
                .build();

        ongoingWorkout= Workout.builder()
                .id(103L)
                .name("Ongoing workout")
                .start(LocalDateTime.now().minusMinutes(30))
                .durationInMinutes(60)
                .user(currentUser)
                .build();

        Exercise e1 = createTestExercise("Bench Press", "Chest exercise using weights",
                Exercise.Category.FLEX, Exercise.MuscleGroup.ABS, 3, 1, 60);
        Exercise e2 = createTestExercise("Jump Rope", "High-intensity cardio workout",
                Exercise.Category.CARDIO,null, 1, 20, 0);
        Exercise e3 = createTestExercise("Lunges", "Leg and glute exercise",
                Exercise.Category.STRENGTH, Exercise.MuscleGroup.GLUTES, 4, 12, 40);

        pastWorkout.setExercises(List.of(e1, e2, e3));

        Exercise e4 = createTestExercise("Plank", "Core strengthening exercise",
                Exercise.Category.FLEX, Exercise.MuscleGroup.ABS, 3, 1, 0);
        Exercise e5 = createTestExercise("Deadlift", "Strength exercise for lower back and legs",
                Exercise.Category.STRENGTH, Exercise.MuscleGroup.HAMSTRINGS, 3, 8, 80);

        futureWorkout.setExercises(List.of(e4, e5));


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
        when(workoutRepository.findAllByUserIdOrderByStartDesc(currentUser.getId()))
                .thenReturn(List.of(futureWorkout, ongoingWorkout, pastWorkout));
        List<Workout> returnedWorkouts = workoutService.getAllWorkouts(currentUser);
        assertEquals(returnedWorkouts.size(), 3);
    }

    @Test
    @DisplayName("Test scenario: get all workouts that will end in future sorted by start time")
    public void getActiveWorkouts_ShouldReturnsWorkoutEndsInFutureSortedByStart() {

        when(workoutRepository.findAllByUserIdOrderByStartAsc(currentUser.getId()))
                .thenReturn(List.of(pastWorkout, ongoingWorkout, futureWorkout));


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
    @DisplayName("Test scenario: list all ended workouts")
    public void generateReport_ShouldReturnPastWorkoutInfoForTheCurrentUser() {
        when(workoutRepository.findAllByUserIdOrderByStartDesc(currentUser.getId()))
                .thenReturn(List.of(futureWorkout, ongoingWorkout, pastWorkout));

        List<WorkoutSummary> report = workoutService.listAllPastWorkouts(currentUser);
        assertNotEquals(null, report);
        assertEquals(1, report.size());
        assertEquals(pastWorkout.getName(), report.getFirst().getWorkoutName());
        verify(workoutRepository, times(1)).
                findAllByUserIdOrderByStartDesc(currentUser.getId());

    }


    @Test
    @DisplayName("Test scenario: add a workout to the list of workouts of the current user")
    public void addWorkout_NoProblem() {
        Workout newWorkout = Workout.builder()
                .id(104L)
                .name("New Workout")
                .start(LocalDateTime.now().plusYears(30))
                .durationInMinutes(60)
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId()))
                .thenReturn(List.of(pastWorkout, futureWorkout, ongoingWorkout));
        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.<Workout>getArgument(0));

        Workout savedWorkout = workoutService.addWorkout(newWorkout, currentUser);

        verify(workoutRepository, times(1)).save(newWorkout);

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
                .id(104L)
                .name("Another workout")
                .start(LocalDateTime.now().minusHours(1))
                .durationInMinutes(60)
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId()))
                .thenReturn(List.of(pastWorkout, futureWorkout, ongoingWorkout));

        assertThrows(WorkoutTimeConflictException.class,
                () -> workoutService.addWorkout(newWorkout, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));


    }

    @Test
    @DisplayName("Test scenario: update the properties of a workout")
    public void updateWorkout_NoProblem() {
        Workout update = Workout.builder()
                .id(101L)
                .durationInMinutes(100)
                .name("Another morning Workout")
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId()))
                .thenReturn(List.of(pastWorkout, futureWorkout, ongoingWorkout));
        when(workoutRepository.save(any(Workout.class))).
                thenAnswer(invocation -> invocation.<Workout>getArgument(0));

        Workout savedWorkout = workoutService.updateWorkout(update, currentUser);

        update.setUser(currentUser);
        verify(workoutRepository, atMost(1)).save(update);
        assertEquals(currentUser.getId(), savedWorkout.getUser().getId());
        assertEquals(update.getDurationInMinutes(), savedWorkout.getDurationInMinutes());
    }

    @Test
    @DisplayName("Test scenario: add one exercise to the workout with given id")
    public void updateWorkout_ShouldAddNewExercise_WhenExerciseListIsUpdated() {
        Workout update = new Workout();
        update.setId(102L);

        List<Exercise> exercises = new ArrayList<>(futureWorkout.getExercises());
        exercises.add(createTestExercise("Running", "Cardio exercise for endurance",
                Exercise.Category.CARDIO, null, 1, 30, 0));

        update.setExercises(exercises);
        when(workoutRepository.findAllByUserId(currentUser.getId())).
                thenReturn(List.of(pastWorkout, futureWorkout, ongoingWorkout));
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
                .start(LocalDateTime.now())
                .durationInMinutes(75)
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId()))
                .thenReturn(List.of(pastWorkout, futureWorkout, ongoingWorkout));
        assertThrows(WorkoutTimeConflictException.class,
                () -> workoutService.updateWorkout(change, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));

    }

    @Test
    @DisplayName("Test scenario: update workout " +
            "whose id is not in the id list of the current user's workouts")
    public void updateWorkout_ShouldThrowException_WhenWorkoutDoesNotBelongToTheCurrentUser() {
        Workout change = Workout.builder()
                .id(104L)
                .name("Another Morning Workout")
                .start(LocalDateTime.of(2024, 2, 21, 8, 0))
                .durationInMinutes(75)
                .build();

        when(workoutRepository.findAllByUserId(currentUser.getId()))
                .thenReturn(List.of(pastWorkout, futureWorkout, ongoingWorkout));
        assertThrows(WorkoutNotFoundException.class,
                () -> workoutService.updateWorkout(change, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));

    }

    @Test
    @DisplayName("Test scenario: update workout without giving an id")
    public void updateWorkout_ShouldThrowException_WhenWorkoutDoesNotHaveID() {
        Workout change = Workout.builder()
                .name("Another Morning Workout")
                .start(LocalDateTime.now())
                .durationInMinutes(45)
                .build();

        assertThrows(InvalidWorkoutDataException.class,
                () -> workoutService.updateWorkout(change, currentUser));

        verify(workoutRepository, never()).save(any(Workout.class));
    }

    @Test
    @DisplayName("Test scenario: delete a workout from the user's workout list")
    public void deleteWorkout_NoProblem() {
        when(workoutRepository.findAllByUserId(currentUser.getId())).
                thenReturn(List.of(pastWorkout, futureWorkout, ongoingWorkout));
        workoutService.deleteWorkout(101L, currentUser);

        verify(workoutRepository, atMost(1)).deleteById(101L);
        verify(workoutRepository, atMost(1)).findAllByUserId(currentUser.getId());
    }

    @Test
    @DisplayName("Test scenario: delete a workout which is not in the user's workouts")
    public void  deleteWorkout_ShouldThrowException_WhenWorkoutDoesNotBelongToTheCurrentUser() {
        when(workoutRepository.findAllByUserId(currentUser.getId())).
                thenReturn(List.of(pastWorkout, futureWorkout, ongoingWorkout));
        assertThrows(WorkoutNotFoundException.class,
                () -> workoutService.deleteWorkout(104L, currentUser));

        verify(workoutRepository, never()).deleteById(any(Long.class));
    }

}