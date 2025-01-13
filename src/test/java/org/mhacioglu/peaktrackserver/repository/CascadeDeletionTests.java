package org.mhacioglu.peaktrackserver.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mhacioglu.peaktrackserver.model.RegisteredUser;
import org.mhacioglu.peaktrackserver.model.Workout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
public class CascadeDeletionTests {
    @Autowired
    private TestEntityManager entityManager;

    private RegisteredUser testUser;
    private Workout testWorkout;

    @BeforeEach
    void setUp() {

        // Create test user
        testUser = new RegisteredUser();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setAge(25);
        testUser.setGender(RegisteredUser.Gender.MALE);
        testUser.setHeight(175);
        testUser.setWeight(70);

        // Create test workout
        testWorkout = Workout.builder()
                .name("Full Body Workout")
                .start(LocalDateTime.now())
                .durationInMinutes(60)
                .build();

        // Associate workout with user
        testUser.addWorkout(testWorkout);
    }

    @DisplayName("If the user is deleted, then all associated workouts to the user" +
            "should be deleted as well")
    @Test
    void whenUserIsDeleted_thenAssociatedWorkoutsAreDeleted() {

        entityManager.persist(testUser);
        Long userId = testUser.getId();
        Long workoutId = testWorkout.getId();

        entityManager.flush();

        entityManager.clear();



        // Delete the user
        RegisteredUser managedUser = entityManager.find(RegisteredUser.class, userId);
        assertNotNull(managedUser, "User should be present in the database");
        entityManager.remove(managedUser);
        entityManager.flush();
        assertNull(entityManager.find(RegisteredUser.class, userId), "User should be deleted");
        // Verify the workout is also deleted
        Workout deletedWorkout = entityManager.find(Workout.class, workoutId);
        assertNull(deletedWorkout, "Workout should be deleted when user is deleted");
    }

}
