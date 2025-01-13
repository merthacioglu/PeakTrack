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
import org.mhacioglu.peaktrackserver.model.RegisteredUser;
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

        RegisteredUser expectedRegisteredUser = new RegisteredUser();
        expectedRegisteredUser.setId(1L);
        expectedRegisteredUser.setUsername(registerUserDto.getUsername());
        expectedRegisteredUser.setPassword(encodedPassword);
        when(userRepository.save(any(RegisteredUser.class))).thenReturn(expectedRegisteredUser);

        RegisteredUser resultRegisteredUser = authService.signUp(registerUserDto);

        assertNotNull(resultRegisteredUser);
        assertEquals(registerUserDto.getUsername(), expectedRegisteredUser.getUsername());
        assertEquals(encodedPassword, resultRegisteredUser.getPassword());

        verify(userRepository, times(1))
                .findByUsername(registerUserDto.getUsername());
        verify(passwordEncoder, times(1))
                .encode(registerUserDto.getPassword());
        verify(userRepository, times(1)).save(any(RegisteredUser.class));

    }

    @Test
    @DisplayName("Signing up with already existing username")
    public void signUpWithFailure_ShouldReturnUsernameAlreadyExistsException() {
        RegisteredUser registeredUser = new RegisteredUser();
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
        verify(userRepository, never()).save(any(RegisteredUser.class));

    }


    @Test
    @DisplayName("Authenticate the user successfully with correct credentials")
    public void authenticateUserWithSuccess_ShouldReturnTheUser() {
        LoginUserDto loginUserDto = new LoginUserDto
                ("testUser", "testPassword");

        RegisteredUser expectedRegisteredUser = new RegisteredUser();
        expectedRegisteredUser.setUsername(loginUserDto.getUsername());
        expectedRegisteredUser.setPassword("encodedTestPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        loginUserDto.getUsername(),
                        loginUserDto.getPassword()
                ));

        when(userRepository.findByUsername(loginUserDto.getUsername()))
                .thenReturn(Optional.of(expectedRegisteredUser));

        RegisteredUser authenticatedRegisteredUser = authService.authenticate(loginUserDto);

        assertNotNull(authenticatedRegisteredUser);
        assertEquals(expectedRegisteredUser.getUsername(), authenticatedRegisteredUser.getUsername());
        assertEquals(expectedRegisteredUser.getPassword(), authenticatedRegisteredUser.getPassword());

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
