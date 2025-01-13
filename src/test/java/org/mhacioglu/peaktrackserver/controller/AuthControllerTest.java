package org.mhacioglu.peaktrackserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mhacioglu.peaktrackserver.config.TestSecurityConfig;
import org.mhacioglu.peaktrackserver.dto.LoginResponse;
import org.mhacioglu.peaktrackserver.dto.LoginUserDto;
import org.mhacioglu.peaktrackserver.dto.RegisterUserDto;
import org.mhacioglu.peaktrackserver.exceptions.UsernameAlreadyExistsException;
import org.mhacioglu.peaktrackserver.model.RegisteredUser;
import org.mhacioglu.peaktrackserver.service.AuthenticationService;
import org.mhacioglu.peaktrackserver.service.JwtService;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationService authenticationService;


    private RegisterUserDto registerUserDto;
    private LoginUserDto loginUserDto;
    private RegisteredUser registeredUser;

    @BeforeEach
    public void setUp() {
        registerUserDto = RegisterUserDto.builder()
                .username("testuser")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .age(25)
                .gender(RegisteredUser.Gender.MALE)
                .weight(80)
                .height(182)
                .build();

        loginUserDto = new LoginUserDto("testuser", "password123");

        registeredUser = new RegisteredUser();
        registeredUser.setUsername("testuser");
        registeredUser.setPassword("encoded_password");

    }

    @Test
    @WithMockUser
    @DisplayName("Create a new user with success")
    public void signup_success_ShouldReturnTheRegisteredUser() throws Exception {

        when(authenticationService.signUp(registerUserDto)).thenReturn(registeredUser);

        RequestBuilder rb = MockMvcRequestBuilders
                .post("/auth/signup")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerUserDto))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(registerUserDto.getUsername()))
                .andExpect(jsonPath("$.password").value("encoded_password"));

        verify(authenticationService, times(1)).signUp(any(RegisterUserDto.class));

    }

    @Test
    @WithMockUser
    @DisplayName("Create a new user with an existing username")
    public void signup_success_ShouldThrowUsernameAlreadyExistsException() throws Exception {

        when(authenticationService.signUp(registerUserDto))
                .thenThrow(new UsernameAlreadyExistsException(registerUserDto.getUsername()));

        RequestBuilder rb = MockMvcRequestBuilders
                .post("/auth/signup")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerUserDto))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(res -> {
                    assertInstanceOf(UsernameAlreadyExistsException.class, res.getResolvedException());
                });

        verify(authenticationService, times(1)).signUp(any(RegisterUserDto.class));

    }


    @Test
    @WithMockUser
    @DisplayName("Authenticate user with valid credentials")
    public void login_success_ShouldReturnTheAuthenticatedUser() throws Exception {
        String expectedToken = "test.jwt.token";
        long expectedExpiration = 360000;
        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setToken(expectedToken);
        expectedResponse.setExpiresIn(expectedExpiration);

        when(authenticationService.authenticate(loginUserDto)).thenReturn(registeredUser);
        when(jwtService.generateToken(registeredUser)).thenReturn(expectedToken);
        when(jwtService.getExpirationTime()).thenReturn(expectedExpiration);

        RequestBuilder rb = MockMvcRequestBuilders
                .post("/auth/login")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUserDto))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(expectedToken))
                .andExpect(jsonPath("$.expiresIn").value(expectedExpiration));


        verify(authenticationService, times(1)).authenticate(any(LoginUserDto.class));
        verify(jwtService, times(1)).generateToken(any(RegisteredUser.class));
        verify(jwtService, times(1)).getExpirationTime();
    }

    @Test
    @WithMockUser
    @DisplayName("Authenticate user with invalid credentials")
    public void login_failure() throws Exception {
        LoginUserDto anotherUser = new LoginUserDto("wronguser", "p4$$w0rd");

        loginUserDto.setUsername("wrongUsername");
        // Set up mock behavior to simulate authentication failure
        when(authenticationService.authenticate(anotherUser))
                .thenThrow(new BadCredentialsException("Username or password is incorrect"));

        // Build and execute the request
        RequestBuilder rb = MockMvcRequestBuilders
                .post("/auth/login")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(anotherUser))
                .contentType(MediaType.APPLICATION_JSON);

        // Verify the response
        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(res -> {
                    assertInstanceOf(BadCredentialsException.class, res.getResolvedException());
                });

        verify(authenticationService, times(1)).authenticate(any(LoginUserDto.class));
        verify(jwtService, never()).generateToken(any(RegisteredUser.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Logout user with success")
    public void logout_success_ShouldReturnNothing() throws Exception {
        String expectedToken = "test.jwt.token";
        RequestBuilder rb = MockMvcRequestBuilders
                .post("/auth/logout")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", expectedToken)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(rb)
                .andDo(print())
                .andExpect(status().isOk());

        verify(jwtService, times(1)).blacklistToken(any(String.class));

    }


}
