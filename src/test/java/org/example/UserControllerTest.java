package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controllers.UserController;
import org.example.dto.ApiResponse;
import org.example.dto.UserRequest;
import org.example.exceptions.InvalidUsernameAndPasswordException;
import org.example.models.User;
import org.example.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateUser_Success() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", "testPass");
        User user = new User();
        user.setId("1234");
        user.setUsername("testUser");
        user.setPassword("testPass");

        when(userService.createUser(any(UserRequest.class))).thenReturn(ResponseEntity.ok(ApiResponse.builder()
                .message("User created successfully")
                .status(HttpStatus.CREATED)
                .data(Map.of("user", user))
                .build()));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("User created successfully")))
                .andExpect(jsonPath("$.status", is("CREATED")))
                .andExpect(jsonPath("$.data.user.username", is("testUser")));

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    void testCreateUser_InvalidUsernameOrPassword() throws Exception {
        UserRequest userRequest = new UserRequest("", "testPass");

        when(userService.createUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid credentials")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")));

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    void testGetUserById_Success() throws Exception {
        String userId = "1234";
        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setPassword("testPass");

        when(userService.getUserById(userId)).thenReturn(ResponseEntity.ok(ApiResponse.builder()
                .message("User fetched successfully")
                .status(HttpStatus.OK)
                .data(Map.of("user", user))
                .build()));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User fetched successfully")))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.user.username", is("testUser")));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        String userId = "9999";
        when(userService.getUserById(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());


        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testLoginUser_Success() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", "testPass");
        User user = new User();
        user.setId("1234");
        user.setUsername("testUser");
        user.setPassword("testPass");

        when(userService.loginUser(any(UserRequest.class))).thenReturn(ResponseEntity.ok(ApiResponse.builder()
                .message("Login successful")
                .status(HttpStatus.OK)
                .data(Map.of("user", user))
                .build()));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Login successful")))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.user.username", is("testUser")));

        verify(userService, times(1)).loginUser(any(UserRequest.class));
    }

    @Test
    void testLoginUser_InvalidCredentials() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", "wrongPass");

        when(userService.loginUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid username or password"));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid username or password")))
                .andExpect(jsonPath("$.status", is("UNAUTHORIZED")));

        verify(userService, times(1)).loginUser(any(UserRequest.class));
    }

    @Test
    void testCreateUser_NullUsername() throws Exception {
        UserRequest userRequest = new UserRequest(null, "testPass");

        when(userService.createUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid credentials")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")));

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    void testCreateUser_NullPassword() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", null);

        when(userService.createUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid credentials")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")));

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    void testCreateUser_EmptyUsername() throws Exception {
        UserRequest userRequest = new UserRequest("", "testPass");

        when(userService.createUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid credentials")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")));

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    void testCreateUser_EmptyPassword() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", "");

        when(userService.createUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid credentials")))
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")));

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    void testLoginUser_UserNotFound() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", "testPass");

        when(userService.loginUser(any(UserRequest.class))).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("User not found")))
                .andExpect(jsonPath("$.status", is("NOT_FOUND")));

        verify(userService, times(1)).loginUser(any(UserRequest.class));
    }
}
