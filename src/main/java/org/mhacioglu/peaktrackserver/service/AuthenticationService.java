package org.mhacioglu.peaktrackserver.service;

import jakarta.validation.Valid;
import org.mhacioglu.peaktrackserver.dto.LoginUserDto;
import org.mhacioglu.peaktrackserver.dto.RegisterUserDto;
import org.mhacioglu.peaktrackserver.exceptions.UsernameAlreadyExistsException;
import org.mhacioglu.peaktrackserver.model.RegisteredUser;
import org.mhacioglu.peaktrackserver.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Validated
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public RegisteredUser signUp(@Valid RegisterUserDto registerUserDto) {
        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        String pass = registerUserDto.getPassword();

        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setUsername(registerUserDto.getUsername());
        registeredUser.setPassword(passwordEncoder.encode(pass));
        registeredUser.setName(registerUserDto.getFirstName());
        registeredUser.setEmail(registerUserDto.getEmail());
        registeredUser.setLastName(registerUserDto.getLastName());
        registeredUser.setAge(registerUserDto.getAge());
        registeredUser.setGender(registerUserDto.getGender());
        registeredUser.setWeight(registerUserDto.getWeight());
        registeredUser.setHeight(registerUserDto.getHeight());

        return userRepository.save(registeredUser);
    }

    public RegisteredUser authenticate(LoginUserDto loginUserDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserDto.getUsername(),
                        loginUserDto.getPassword()
                )
        );

        return userRepository.findByUsername(loginUserDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


}
