package org.mhacioglu.peaktrackserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LoginUserDto {
    private String username;
    private String password;
}
