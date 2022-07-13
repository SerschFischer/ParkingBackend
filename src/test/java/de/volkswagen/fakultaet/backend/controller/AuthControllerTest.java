package de.volkswagen.fakultaet.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.volkswagen.fakultaet.backend.domain.dto.UserLoginRequest;
import de.volkswagen.fakultaet.backend.domain.dto.UserRegisterRequest;
import de.volkswagen.fakultaet.backend.service.AuthService;
import de.volkswagen.fakultaet.backend.service.AuthService.InvalidPasswordException;
import de.volkswagen.fakultaet.backend.service.AuthService.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthService authService;

    ObjectMapper mapper = new ObjectMapper();

    UserRegisterRequest userRegisterRequest;
    UserLoginRequest userLoginRequest;
    String token;

    @BeforeEach
    void setUp() {
        userRegisterRequest = new UserRegisterRequest();
        userRegisterRequest.setEmail("dieter@dummy.de");
        userRegisterRequest.setPassword("123");
        userRegisterRequest.setFirstname("Dieter");
        userRegisterRequest.setLastname("Scherzer");
        userRegisterRequest.setStreet("Kantstr.");
        userRegisterRequest.setHouseNumber("13C");
        userRegisterRequest.setZip(10155);
        userRegisterRequest.setCity("Berlin");

        userLoginRequest = new UserLoginRequest();
        userLoginRequest.setEmail("dieter@dummy.de");
        userLoginRequest.setPassword("123");

        token = "JBJpEGFJssmVZfXpcmNteJThW21QzMmqtMnV7dT1CrBSng6z06kupcc6kTKHv7Zv4qQYMaQ32";
    }

    @Test
    void registerUser_validValue_returnToken() throws Exception {
        // GIVEN
       when(this.authService.registerUser(any(UserRegisterRequest.class))).thenReturn(this.token);
        // WHEN
        this.mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.mapper.writeValueAsString(this.userRegisterRequest)))
                .andDo(print())
                // THEN
                .andExpect(status().isAccepted())
                .andExpect(content().string(containsString(this.token)));
    }

    @Test
    void registerUser_withExistsEmail_returnToken() throws Exception {
        // GIVEN
        when(authService.registerUser(any(UserRegisterRequest.class)))
                .thenThrow(new EntityExistsException());
        // WHEN
        this.mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userRegisterRequest)))
                .andDo(print())
                // THEN
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUser_validValue_returnToken() throws Exception {
        // GIVEN
        when(this.authService.loginUser(any(UserLoginRequest.class))).thenReturn(this.token);
        // WHEN
        this.mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.mapper.writeValueAsString(this.userLoginRequest)))
                .andDo(print())
                // THEN
                .andExpect(status().isAccepted())
                .andExpect(content().string(containsString(this.token)));
    }

    @Test
    void loginUser_badRequest_returnToken() throws Exception {
        // GIVEN
        when(authService.loginUser(any(UserLoginRequest.class)))
                .thenThrow( new EntityNotFoundException(), new InvalidPasswordException());
        // WHEN
        this.mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userLoginRequest)))
                .andDo(print())
                // THEN
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutUser_validToken_returnHttpStatusAccepted() throws Exception {
        // GIVEN
        // WHEN
        this.mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", this.token))
                .andDo(print())
                // THEN
                .andExpect(status().isAccepted());
        verify(this.authService).logoutUser(anyString());

    }
    @Test
    void logoutUser_invalidOrExpiredToken_returnBadRequest() throws Exception {
        doThrow(new UnauthorizedException())
                .when(this.authService).logoutUser(anyString());
        // WHEN
        this.mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", this.token))
                .andDo(print())
                // THEN
                .andExpect(status().isBadRequest());
        verify(this.authService).logoutUser(anyString());
    }
}