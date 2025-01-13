package org.mhacioglu.peaktrackserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.mhacioglu.peaktrackserver.model.RegisteredUser.Gender;
import org.mhacioglu.peaktrackserver.validation.ValidPassword;

@Data
@Builder
public class RegisterUserDto {
    private String username;

    @NotNull
    @ValidPassword
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private int age;
    private Gender gender;
    private int height;
    private int weight;
}
