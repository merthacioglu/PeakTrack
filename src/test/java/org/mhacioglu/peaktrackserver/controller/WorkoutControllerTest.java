package org.mhacioglu.peaktrackserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mhacioglu.peaktrackserver.config.JwtAuthenticationFilter;
import org.mhacioglu.peaktrackserver.exceptions.InvalidWorkoutDataException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutNotFoundException;
import org.mhacioglu.peaktrackserver.exceptions.WorkoutTimeConflictException;
import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.mhacioglu.peaktrackserver.service.JwtService;
import org.mhacioglu.peaktrackserver.service.UserService;
import org.mhacioglu.peaktrackserver.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");

        Workout pastWorkout = Workout.builder()
                .id(1L)
                .name("Workout 1")
                .start(LocalDateTime.now().minusDays(1))
                .durationInMinutes(60)
                .user(user)
                .build();


        Workout ongoingWorkout = Workout.builder()
                .id(2L)
                .name("Workout 2")
                .start(LocalDateTime.now().minusMinutes(30))
                .durationInMinutes(90)
                .user(user)
                .build();

        Workout futureWorkout = Workout.builder()
                .id(3L)
                .name("Workout 3")
                .start(LocalDateTime.now().plusDays(1))
                .durationInMinutes(60)
                .user(user)
                .build();


        user.setWorkouts(new ArrayList<>(List.of(ongoingWorkout, futureWorkout, pastWorkout)));


        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    @DisplayName("Get all workouts for the current user")
    void getAllWorkouts_ShouldReturnWorkoutList() throws Exception {


        mockMvc.perform(get("/api/workout/getAll")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[2].id").value(3));

        verify(userService, times(1)).getCurrentUser();

    }

    @Test
    @DisplayName("Get all active workouts which will end " +
            "in future sorted from soonest to latest")
    public void getAllActiveWorkouts_ShouldReturnWorkoutsThatEndAfterNow() throws Exception {

        mockMvc.perform(get("/api/workout/getActiveWorkouts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[1].id").value(3L));

        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Create a new workout and return the created workout")
    void createWorkout_ShouldReturnCreatedWorkout() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        Workout request = Workout.builder()
                .name("Another workout")
                .start(start)
                .durationInMinutes(45)
                .build();

        Workout response = Workout.builder()
                .name("Another workout")
                .start(start)
                .durationInMinutes(45)
                .user(user)
                .id(4L)
                .build();

        when(workoutService.addWorkout(request, user)).thenReturn(response);

        RequestBuilder rb = MockMvcRequestBuilders
                .post("/api/workout/create")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(4L));

        verify(workoutService, times(1)).addWorkout(request, user);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Create a new workout whose time period conflicts with another workout")
    void createWorkout_ShouldReturn400BadRequestErrorBecauseOfWorkoutTimeConflictException() throws Exception {
        Workout update = Workout.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .build();


        when(workoutService.addWorkout(eq(update), eq(user)))
                .thenThrow(new WorkoutTimeConflictException(
                        "Timing conflict detected!"
                ));

        RequestBuilder rb = MockMvcRequestBuilders
                .post("/api/workout/create")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertInstanceOf(WorkoutTimeConflictException.class, result.getResolvedException());
                    String message = result.getResolvedException().getMessage();
                    assertTrue(message.contains("Timing conflict detected"));
                });

        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Create a new workout which misses a start date")
    void createWorkout_ShouldReturn400BadRequestBecauseOfInvalidWorkoutDataException() throws Exception {
        Workout w2 = Workout.builder()
                .name("Another morning Workout")
                .durationInMinutes(100)
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

        verify(userService, times(1)).getCurrentUser();
    }


    @Test
    @DisplayName("Update an existing workout and return the updated workout")
    void updateWorkout_ShouldReturnUpdatedWorkout() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        Workout req = Workout.builder()
                .name("Updated workout")
                .start(start)
                .durationInMinutes(100)
                .build();

        Workout res = Workout.builder()
                .id(4L)
                .name("Updated workout")
                .start(start)
                .durationInMinutes(100)
                .user(user)
                .build();

        when(workoutService.updateWorkout(eq(req), eq(user)))
                .thenReturn(res);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .put("/api/workout/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4L))
                .andExpect(jsonPath("$.name").value("Updated workout"));


        verify(workoutService, times(1)).updateWorkout(eq(req), eq(user));
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
                .thenThrow(new WorkoutTimeConflictException("Timing conflict detected"));


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
        Workout update = Workout.builder()
                .name("New Workout")
                .start(LocalDateTime.now().plusDays(10))
                .durationInMinutes(45)
                .build();

        when(workoutService.updateWorkout(eq(update), eq(user)))
                .thenThrow(new InvalidWorkoutDataException("A workout must have a valid workout ID"));

        RequestBuilder rb = MockMvcRequestBuilders
                .put("/api/workout/update")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
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
                .id(4L)
                .name("Another Workout")
                .start(LocalDateTime.now().plusDays(23).plusHours(10))
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

        verify(workoutService, times(1)).updateWorkout(eq(updatedWorkout), eq(user));
    }

    @Test
    @DisplayName("Delete a workout by its ID")
    void deleteWorkout_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/workout/delete/{workoutId}", 1L))
                .andExpect(status().isNoContent());
        verify(workoutService, times(1)).deleteWorkout(eq(1L), eq(user));
    }

    @Test
    @DisplayName("Delete a workout which doesn't belong to the current user")
    void deleteWorkout_ShouldReturn404NotFound() throws Exception {
        doThrow(new WorkoutNotFoundException(4L))
                .when(workoutService).deleteWorkout(eq(4L), eq(user));

        mockMvc.perform(delete("/api/workout/delete/{workoutId}", 4L))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    assertInstanceOf(WorkoutNotFoundException.class, result.getResolvedException());
                    String message = result.getResolvedException().getMessage();
                    assertEquals(String.format("Workout with id %d does not exist", 4L),
                            message);
                });

        verify(workoutService, times(1)).deleteWorkout(eq(4L), eq(user));

    }


}