package org.mhacioglu.peaktrackserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;

import static org.mockito.Mockito.when;
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
    private Workout workout;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");

        workout = Workout.builder()
                .id(101L)
                .name("Morning Workout")
                .start(LocalDateTime.of(2024, 4, 12, 8, 30))
                .durationInMinutes(60)
                .user(user)
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    @DisplayName("Get all workouts for the current user")
    void getAllWorkouts_ShouldReturnWorkoutList() throws Exception {
        List<Workout> workouts = List.of(workout);
        when(workoutService.getAllWorkouts(user)).thenReturn(workouts);

        mockMvc.perform(get("/api/workout/getAll")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(workout.getId()))
                .andExpect(jsonPath("$[0].name").value(workout.getName()));
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
    @DisplayName("Delete a workout by its ID")
    void deleteWorkout_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/workout/delete/{workoutId}", 101L))
                .andExpect(status().isNoContent());
        verify(workoutService).deleteWorkout(eq(101L), eq(user));
    }









}