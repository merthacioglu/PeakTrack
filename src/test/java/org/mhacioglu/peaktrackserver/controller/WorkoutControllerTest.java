package org.mhacioglu.peaktrackserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mhacioglu.peaktrackserver.exceptions.InvalidWorkoutDataException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutNotFoundException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutTimeConflictException;
import org.mhacioglu.peaktrackserver.model.WorkoutSummary;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.mhacioglu.peaktrackserver.config.JwtAuthenticationFilter;
import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.mhacioglu.peaktrackserver.service.JwtService;
import org.mhacioglu.peaktrackserver.service.UserService;
import org.mhacioglu.peaktrackserver.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(WorkoutController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WorkoutControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkoutService workoutService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Workout w1;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");

        w1 = Workout.builder()
                .id(101L)
                .name("Morning Workout")
                .start(LocalDateTime.now().minusYears(1))
                .durationInMinutes(60)
                .user(user)
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    @DisplayName("Get all workouts for the current user")
    void getAllWorkouts_ShouldReturnWorkoutList() throws Exception {
        List<Workout> workouts = List.of(w1);
        when(workoutService.getAllWorkouts(user)).thenReturn(workouts);

        mockMvc.perform(get("/api/workout/getAll")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(w1.getId()))
                .andExpect(jsonPath("$[0].name").value(w1.getName()));
    }

    @Test
    @DisplayName("Get all active workouts which will end in future sorted by start date")
    public void getAllActiveWorkouts_ShouldReturnWorkoutsThatEndAfterNow() throws Exception {

        Workout w1 = Workout.builder()
                .id(101L)
                .name("Ongoing workout")
                .start(LocalDateTime.now().minusMinutes(30))
                .durationInMinutes(60)
                .user(user)
                .build();

        Workout w2 = Workout.builder()
                .id(102L)
                .name("Upcoming workout")
                .start(LocalDateTime.of(2041, 1, 1, 8, 35))
                .durationInMinutes(60)
                .user(user)
                .build();

        when(workoutService.getAllActiveWorkouts(user))
                .thenReturn(List.of(w1, w2));

        mockMvc.perform(get("/api/workout/getActiveWorkouts")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(w1.getId()))
                .andExpect(jsonPath("$[0].name").value(w1.getName()))
                .andExpect(jsonPath("$[1].id").value(w2.getId()))
                .andExpect(jsonPath("$[1].name").value(w2.getName()));

    }

    @Test
    @DisplayName("List all completed workouts as a report to track progress")
    void generateReport_ShouldReturnCompletedWorkouts() throws Exception {
        WorkoutSummary ws1 = WorkoutSummary.builder()
                    .workoutName("Past workout 1")
                    .workoutStart(LocalDateTime.now().minusYears(1))
                    .workoutDuration(70)
                    .build();

        WorkoutSummary ws2 = WorkoutSummary.builder()
                .workoutName("Past workout 2")
                .workoutStart(LocalDateTime.now().minusMonths(3))
                .workoutDuration(45)
                .build();

        when(workoutService.listAllPastWorkouts(eq(user)))
                .thenReturn(List.of(ws2, ws1));

        mockMvc.perform(get("/api/workout/generateReport")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("Response: " + result.getResponse().getContentAsString());
                    System.out.println("Expected: " + ws2.getWorkoutStart());
                })
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].workoutName").value(ws2.getWorkoutName()))
                .andExpect(jsonPath("$[1].workoutName").value(ws1.getWorkoutName()));


        verify(workoutService, times(1)).listAllPastWorkouts(eq(user));

    }

    @Test
    @DisplayName("Create a new workout and return the created workout")
    void createWorkout_ShouldReturnCreatedWorkout() throws Exception {
        Workout newWorkout = Workout.builder()
                .name("Evening Workout")
                .start(LocalDateTime.now())
                .durationInMinutes(100)
                .build();

        when(workoutService.addWorkout(any(Workout.class), eq(user)))
                .thenAnswer(invocation -> {
                    Workout w = invocation.getArgument(0);
                    w.setId(102L);
                    w.setUser(user);
                    return w;
                });

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/workout/create")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newWorkout))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(newWorkout.getName()));

        verify(workoutService).addWorkout(any(Workout.class), eq(user));
    }

    @Test
    @DisplayName("Create a new workout whose time period conflicts with another workout")
    void createWorkout_ShouldReturn400BadRequestErrorBecauseOfWorkoutTimeConflictException() throws Exception {
        Workout w2 = Workout.builder()
                .name("Another morning Workout")
                .start(LocalDateTime.now().minusYears(1))
                .durationInMinutes(100)
                .user(user)
                .build();

        when(workoutService.addWorkout(any(Workout.class), eq(user)))
                .thenThrow(new WorkoutTimeConflictException(
                        w1.getId(),
                        null,
                        w1.getStart(),
                        w2.getStart(),
                        w1.getStart().plusMinutes(w1.getDurationInMinutes()),
                        w2.getStart().plusMinutes(w2.getDurationInMinutes())
                ));

        RequestBuilder rb = MockMvcRequestBuilders
                .post("/api/workout/create")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(w2))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertInstanceOf(WorkoutTimeConflictException.class, result.getResolvedException());
                    String message = result.getResolvedException().getMessage();
                    assertTrue(message.contains("Timing conflict detected"));
                });
    }

    @Test
    @DisplayName("Create a new workout which misses a start date")
    void createWorkout_ShouldReturn400BadRequestBecauseOfInvalidWorkoutDataException() throws Exception {
        Workout w2 = Workout.builder()
                .name("Another morning Workout")
                .durationInMinutes(100)
                .user(user)
                .build();


        when(workoutService.addWorkout(eq(w2), eq(user)))
                .thenThrow(new InvalidWorkoutDataException("A workout must have a valid start date."));

        RequestBuilder rb = MockMvcRequestBuilders
                .post("/api/workout/create")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(w2))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertInstanceOf(InvalidWorkoutDataException.class, result.getResolvedException());
                    String message = result.getResolvedException().getMessage();
                    assertEquals(message, "A workout must have a valid start date.");
                });
    }


    @Test
    @DisplayName("Update an existing workout and return the updated workout")
    void updateWorkout_ShouldReturnUpdatedWorkout() throws Exception {
        Workout updateWorkout = Workout.builder()
                .id(101L)
                .name("Updated Workout")
                .start(LocalDateTime.of(2024, 4, 12, 9, 0))
                .durationInMinutes(45)
                .build();

        when(workoutService.updateWorkout(any(Workout.class), eq(user)))
                .thenReturn(updateWorkout);

       RequestBuilder requestBuilder = MockMvcRequestBuilders
                .put("/api/workout/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateWorkout));

       mockMvc.perform(requestBuilder)
                   .andDo(print())
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.name")
                   .value(updateWorkout.getName()));

        verify(workoutService).updateWorkout(any(Workout.class), eq(user));
    }

    @Test
    @DisplayName("Setting a new time slot for a workout which will conflict with another workout")
    void updateWorkout_ShouldReturn500InternalServerErrorBecauseOfWorkoutTimeConflictException() throws Exception {
        Workout updatedWorkout = Workout.builder()
                .id(102L)
                .name("This workout will be updated")
                .start(LocalDateTime.now().minusYears(1))
                .durationInMinutes(90)
                .build();

        when(workoutService.updateWorkout(updatedWorkout, user))
        .thenThrow(new WorkoutTimeConflictException(
                w1.getId(),
                updatedWorkout.getId(),
                w1.getStart(),
                updatedWorkout.getStart(),
                w1.getStart().plusMinutes(w1.getDurationInMinutes()),
                updatedWorkout.getStart().plusMinutes(updatedWorkout.getDurationInMinutes())));



        RequestBuilder rb = MockMvcRequestBuilders
                .put("/api/workout/update")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedWorkout))
                .contentType(MediaType.APPLICATION_JSON);


        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertInstanceOf(WorkoutTimeConflictException.class, result.getResolvedException());
                    String message = result.getResolvedException().getMessage();
                    assertTrue(message.contains("Timing conflict detected"));
                });

    }

    @Test
    @DisplayName("Update a workout when no ID is provided")
    void updateWorkout_ShouldReturn400BadRequestBecauseNoWorkoutIdIsProvided() throws Exception {
        Workout updatedWorkout = Workout.builder()
                .name("Evening Workout")
                .start(LocalDateTime.now().minusMonths(3).minusHours(6))
                .durationInMinutes(45)
                .build();

        when(workoutService.updateWorkout(updatedWorkout, user))
                .thenThrow(new InvalidWorkoutDataException("A workout must have a valid workout ID"));

        RequestBuilder rb = MockMvcRequestBuilders
                .put("/api/workout/update")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedWorkout))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertInstanceOf(InvalidWorkoutDataException.class, result.getResolvedException());
                    String message = result.getResolvedException().getMessage();
                    assertTrue(message.contains("A workout must have a valid workout ID"));
                });
    }

    @Test
    @DisplayName("Update a workout when there is no workout with the provided ID " +
            "for the current user")
    void updateWorkout_ShouldReturn404NotFoundBecauseTheWorkoutDoesNotBelongToTheCurrentUser() throws Exception {
        Workout updatedWorkout = Workout.builder()
                .id(106L)
                .name("Evening Workout")
                .start(LocalDateTime.now().minusMonths(3).minusHours(6))
                .durationInMinutes(45)
                .build();

        RequestBuilder rb = MockMvcRequestBuilders
                .put("/api/workout/update")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedWorkout))
                .contentType(MediaType.APPLICATION_JSON);

        when(workoutService.updateWorkout(eq(updatedWorkout), eq(user)))
                .thenThrow(new WorkoutNotFoundException(updatedWorkout.getId()));

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    assertInstanceOf(WorkoutNotFoundException.class, result.getResolvedException());
                    String message = result.getResolvedException().getMessage();
                    assertEquals(String.format("Workout with id %d does not exist", updatedWorkout.getId()),
                            message);
                });


    }

    @Test
    @DisplayName("Delete a workout by its ID")
    void deleteWorkout_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/workout/delete/{workoutId}", 101L))
                .andExpect(status().isNoContent());
        verify(workoutService).deleteWorkout(eq(101L), eq(user));
    }

    @Test
    @DisplayName("Delete a workout which doesn't belong to the current user")
    void deleteWorkout_ShouldReturn404NotFound() throws Exception {
        doThrow(new WorkoutNotFoundException(1L))
                .when(workoutService).deleteWorkout(eq(1L), eq(user));
        mockMvc.perform(delete("/api/workout/delete/{workoutId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    assertInstanceOf(WorkoutNotFoundException.class, result.getResolvedException());
                    String message = result.getResolvedException().getMessage();
                    assertEquals(String.format("Workout with id %d does not exist", 1L),
                            message);
                });

    }









}