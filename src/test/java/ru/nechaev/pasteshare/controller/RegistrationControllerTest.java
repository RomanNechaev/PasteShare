package ru.nechaev.pasteshare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.nechaev.pasteshare.dto.AuthenticationRequest;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationControllerTest {
    @LocalServerPort
    private Integer port;
    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15.1-alpine")
    );
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private PasteRepository pasteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private AuthenticationRequest authenticationRequest;

    @BeforeEach
    void seUp() {
        permissionRepository.deleteAll();
        pasteRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void canRegisterShouldReturnEmptyResponseWithStatusCreated() throws Exception {
        authenticationRequest = AuthenticationRequest.builder()
                .username("userTest")
                .email("test@gmail.com")
                .password("123")
                .build();

        String jsonRequest = new ObjectMapper().writeValueAsString(authenticationRequest);
        var requestBuilder = post("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isCreated()
                );
        assertThat(userRepository.findAll()).hasSize(1);
        User user = userRepository.findAll().get(0);
        Assertions.assertAll(
                () -> assertThat(user.getName()).isEqualTo(authenticationRequest.getUsername()),
                () -> assertThat(user.getEmail()).isEqualTo(authenticationRequest.getEmail())
        );
    }

    @Test
    void cantRegisterIfUsernameIsExistsShouldReturnExceptionWithStatusConflict() throws Exception {
        authenticationRequest = AuthenticationRequest.builder()
                .username("userTest")
                .email("test@gmail.com")
                .password("123")
                .build();

        String jsonRequest = new ObjectMapper().writeValueAsString(authenticationRequest);
        var requestBuilder = post("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.message", is("Username already used! Input another name"))
                );

        assertThat(userRepository.findAll()).hasSize(1);
        User user = userRepository.findAll().get(0);
        Assertions.assertAll(
                () -> assertThat(user.getName()).isEqualTo(authenticationRequest.getUsername()),
                () -> assertThat(user.getEmail()).isEqualTo(authenticationRequest.getEmail())
        );
    }

    @Test
    void cantRegisterIfUsernameIsEmptyShouldReturnExceptionWithStatusBadRequest() throws Exception {
        authenticationRequest = AuthenticationRequest.builder()
                .username("")
                .email("test@gmail.com")
                .password("123")
                .build();

        String jsonRequest = new ObjectMapper().writeValueAsString(authenticationRequest);
        var requestBuilder = post("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.authenticationRequest.username")),
                        jsonPath("$.violations[0].message", is("must not be blank"))
                );

        assertThat(userRepository.findAll()).hasSize(0);
    }

    @Test
    void cantRegisterIfUsernameIsMoreThan30SymbolsReturnExceptionWithStatusBadRequest() throws Exception {
        authenticationRequest = AuthenticationRequest.builder()
                .username("a".repeat(31))
                .email("test@gmail.com")
                .password("123")
                .build();

        String jsonRequest = new ObjectMapper().writeValueAsString(authenticationRequest);
        var requestBuilder = post("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.authenticationRequest.username")),
                        jsonPath("$.violations[0].message", is("Name length must be at most 20 characters"))
                );

        assertThat(userRepository.findAll()).hasSize(0);
    }

    @Test
    void cantRegisterIfPasswordIsEmptyShouldReturnExceptionWithStatusBadRequest() throws Exception {
        authenticationRequest = AuthenticationRequest.builder()
                .username("test")
                .email("test@gmail.com")
                .password("")
                .build();

        String jsonRequest = new ObjectMapper().writeValueAsString(authenticationRequest);
        var requestBuilder = post("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.authenticationRequest.password")),
                        jsonPath("$.violations[0].message", is("must not be blank"))
                );

        assertThat(userRepository.findAll()).hasSize(0);
    }

    @Test
    void cantRegisterIfEmailIsEmptyShouldReturnExceptionWithStatusBadRequest() throws Exception {
        authenticationRequest = AuthenticationRequest.builder()
                .username("test")
                .email("")
                .password("123")
                .build();

        String jsonRequest = new ObjectMapper().writeValueAsString(authenticationRequest);
        var requestBuilder = post("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.authenticationRequest.email")),
                        jsonPath("$.violations[0].message", is("must not be blank"))
                );

        assertThat(userRepository.findAll()).hasSize(0);
    }

    @Test
    void cantRegisterIfEmailInInvalidFormatShouldReturnExceptionWithStatusBadRequest() throws Exception {
        authenticationRequest = AuthenticationRequest.builder()
                .username("test")
                .email("123@@gmail")
                .password("123")
                .build();

        String jsonRequest = new ObjectMapper().writeValueAsString(authenticationRequest);
        var requestBuilder = post("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.authenticationRequest.email")),
                        jsonPath("$.violations[0].message", is("must be a well-formed email address"))
                );

        assertThat(userRepository.findAll()).hasSize(0);
    }

    @Test
    void cantRegisterIfEmailLengthMoreThan50SymbolsShouldReturnExceptionWithStatusBadRequest() throws Exception {
        authenticationRequest = AuthenticationRequest.builder()
                .username("test")
                .email("test".repeat(15) + "@gmail.com")
                .password("123")
                .build();

        String jsonRequest = new ObjectMapper().writeValueAsString(authenticationRequest);
        var requestBuilder = post("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.authenticationRequest.email")),
                        jsonPath("$.violations[0].message", is("Email length must be at most 50 characters"))
                );

        assertThat(userRepository.findAll()).hasSize(0);
    }

}
