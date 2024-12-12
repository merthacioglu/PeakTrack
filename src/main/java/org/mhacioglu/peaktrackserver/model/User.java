package org.mhacioglu.peaktrackserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
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
    @Size(min=5)
    private String username;

    @NotNull
    @Size(min=6)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one special character")
    private String password;

    @NotNull
    private String name;

    @NotNull
    private String lastName;

    private String email;

    private String phone;


    @Min(10)
    @Max(80)
    private int age;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    @NotNull
    private int height;

    @NotNull
    private int weight;

    private String additionalNotes;

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

    private enum Gender {
        MALE, FEMALE, TRANSGENDER, INTERSEX
    }




}
