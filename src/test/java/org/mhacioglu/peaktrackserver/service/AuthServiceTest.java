package org.mhacioglu.peaktrackserver.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mhacioglu.peaktrackserver.dto.LoginUserDto;
import org.mhacioglu.peaktrackserver.dto.RegisterUserDto;
import org.mhacioglu.peaktrackserver.exceptions.UsernameAlreadyExistsException;
import org.mhacioglu.peaktrackserver.model.User;
import org.mhacioglu.peaktrackserver.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authService;

    private RegisterUserDto registerUserDto;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @BeforeEach
    public void setUp() {
        registerUserDto = RegisterUserDto.builder()
                .username("testUser")
                .password("testPassword")
                .build();



    }

    @Test
    @DisplayName("Signing up with success")
    public void signUpWithSuccess_ShouldReturnTheUser() {
        when(userRepository.findByUsername(registerUserDto.getUsername()))
                .thenReturn(Optional.empty());

        String encodedPassword = "encodedTestPassword";
        when(passwordEncoder.encode(registerUserDto.getPassword()))
                .thenReturn(encodedPassword);

        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setUsername(registerUserDto.getUsername());
        expectedUser.setPassword(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User resultUser = authService.signUp(registerUserDto);

        assertNotNull(resultUser);
        assertEquals(registerUserDto.getUsername(), expectedUser.getUsername());
        assertEquals(encodedPassword, resultUser.getPassword());

        verify(userRepository, times(1))
                .findByUsername(registerUserDto.getUsername());
        verify(passwordEncoder, times(1))
                .encode(registerUserDto.getPassword());
        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    @DisplayName("Signing up with already existing username")
    public void signUpWithFailure_ShouldReturnUsernameAlreadyExistsException() {
        User registeredUser = new User();
        registeredUser.setId(2L);
        registeredUser.setUsername(registerUserDto.getUsername());
        registeredUser.setPassword("p4$$w0rd");

        when(userRepository.findByUsername(registerUserDto.getUsername()))
                .thenReturn(Optional.of(registeredUser));

        assertThrows(UsernameAlreadyExistsException.class,
                () -> authService.signUp(registerUserDto));

        verify(userRepository, times(1))
                .findByUsername(registerUserDto.getUsername());

        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));

    }


    @Test
    @DisplayName("Authenticate the user successfully with correct credentials")
    public void authenticateUserWithSuccess_ShouldReturnTheUser() {
        LoginUserDto loginUserDto = new LoginUserDto
                ("testUser", "testPassword");

        User expectedUser = new User();
        expectedUser.setUsername(loginUserDto.getUsername());
        expectedUser.setPassword("encodedTestPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        loginUserDto.getUsername(),
                        loginUserDto.getPassword()
                ));

        when(userRepository.findByUsername(loginUserDto.getUsername()))
                .thenReturn(Optional.of(expectedUser));

        User authenticatedUser = authService.authenticate(loginUserDto);

        assertNotNull(authenticatedUser);
        assertEquals(expectedUser.getUsername(), authenticatedUser.getUsername());
        assertEquals(expectedUser.getPassword(), authenticatedUser.getPassword());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(userRepository, times(1))
                .findByUsername(loginUserDto.getUsername());
    }


    @Test
    @DisplayName("Authentication with incorrect credentials")
    public void authenticateUserFailScenario_ShouldThrowBadCredentialsException() {
        LoginUserDto loginUserDto = new LoginUserDto
                ("testUser", "testPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticate(loginUserDto);
        });

        verify(authenticationManager, times(1))
                .authenticate(any(Authentication.class));

        verify(userRepository, never()).findByUsername(any(String.class));
    }


}
