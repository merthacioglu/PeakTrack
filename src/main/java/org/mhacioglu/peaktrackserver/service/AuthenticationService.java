package org.mhacioglu.peaktrackserver.service;

import org.mhacioglu.peaktrackserver.dto.LoginUserDto;
import org.mhacioglu.peaktrackserver.dto.RegisterUserDto;
import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User signUp(RegisterUserDto registerUserDto) {
        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Another user with that username already exists.");
        }
        String pass = registerUserDto.getPassword();
        if (pass.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        if (!UPPERCASE_PATTERN.matcher(pass).find()) {
            throw new IllegalArgumentException("Password must contain at least 1 uppercase letter.");
        }

        // Check for at least one lowercase letter
        if (!LOWERCASE_PATTERN.matcher(pass).find()) {
            throw new IllegalArgumentException("Password must contain at least 1 lowercase letter.");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(pass).find()) {
            throw new IllegalArgumentException("Password must contain at least 1 special character.");
        }


        User user = new User();
        user.setUsername(registerUserDto.getUsername());
        user.setPassword(passwordEncoder.encode(pass));
        user.setName(registerUserDto.getFirstName());
        user.setLastName(registerUserDto.getLastName());
        user.setAge(registerUserDto.getAge());
        user.setGender(registerUserDto.getGender());
        user.setWeight(registerUserDto.getWeight());
        user.setHeight(registerUserDto.getHeight());

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto loginUserDto) {
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
