package org.mhacioglu.peaktrackserver.controller;

import org.mhacioglu.peaktrackserver.dto.LoginResponse;
import org.mhacioglu.peaktrackserver.dto.LoginUserDto;
import org.mhacioglu.peaktrackserver.dto.RegisterUserDto;
import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.service.AuthenticationService;
import org.mhacioglu.peaktrackserver.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationService authService;

    public AuthController(JwtService jwtService, AuthenticationService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authService.signUp(registerUserDto);
        if (registeredUser == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }


}
