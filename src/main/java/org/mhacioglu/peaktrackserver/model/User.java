package org.mhacioglu.peaktrackserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Entity
public class User implements Serializable, UserDetails {

    @Serial
    private final static long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    @Size(min = 5, max = 20)
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String name;

    @NotNull
    private String lastName;

    @NotNull
    @Column(unique = true)
    private String email;

    private String phone;


    @Min(10)
    @Max(80)
    @NotNull
    private int age;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    @NotNull
    private int height;

    @NotNull
    private int weight;

    private String additionalNotes;

    @JsonIgnore
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Workout> workouts;

    public User() {
        this.workouts = new ArrayList<>();

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void addWorkout(Workout workout) {
        workout.setUser(this);
        workouts.add(workout);
    }

    public void deleteWorkout(Workout workout) {
        workout.setUser(null);
        workouts.remove(workout);

    }

    public enum Gender {
        MALE, FEMALE, TRANSGENDER, INTERSEX
    }


}
