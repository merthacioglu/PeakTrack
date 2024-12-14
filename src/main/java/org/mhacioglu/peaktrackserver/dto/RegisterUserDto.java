package org.mhacioglu.peaktrackserver.dto;

import lombok.Data;
import org.mhacioglu.peaktrackserver.model.User.Gender;

@Data
public class RegisterUserDto {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private int age;
    private Gender gender;
    private int height;
    private int weight;
}
