package ru.nechaev.pasteshare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.nechaev.pasteshare.WIthMockCustomUser;
import ru.nechaev.pasteshare.dto.PasteRequest;
import ru.nechaev.pasteshare.dto.PermissionRequest;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.Permission;
import ru.nechaev.pasteshare.entitity.Role;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PermissionControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private PasteRepository pasteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    private User testUser;
    private User testUser2;

    private PasteRequest testPasteRequest;

    private PermissionRequest testPermissionRequest;

    @LocalServerPort
    private Integer port;
    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15.1-alpine")
    );

    @BeforeEach
    void seUp() {
        permissionRepository.deleteAll();
        pasteRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(UUID.randomUUID(),
                "test",
                "test@gmail.com",
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER,
                "123");

        testUser2 = new User(UUID.randomUUID(),
                "test222",
                "test@gmail.com",
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER,
                "123");

        testPasteRequest = PasteRequest.builder()
                .title("test")
                .expiredAt("2024-12-13")
                .visibility("PUBLIC")
                .text("test")
                .build();
        userRepository.save(testUser);
    }

    @Test
    @WIthMockCustomUser
    void canCreatePermissionShouldReturnEmptyResponseWithStatusIsCreated() throws Exception {
        userRepository.save(testUser2);

        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderForCreatePaste = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePaste);

        Paste paste = pasteRepository.findAll().get(0);
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isFalse();

        testPermissionRequest = new PermissionRequest(testUser2.getName(), paste.getContentLocation(), null);
        String jsonRequestForCreatedPermission = new ObjectMapper().writeValueAsString(testPermissionRequest);
        var requestBuilderForCreatePermission = post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestForCreatedPermission)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePermission)
                .andExpectAll(
                        status().isCreated()
                );
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isTrue();
    }

    @Test
    @WIthMockCustomUser
    void cantCreatePermissionIfPasteNotExistentShouldReturnExceptionWithStatusIsNotFound() throws Exception {
        userRepository.save(testUser2);

        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderForCreatePaste = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePaste);

        Paste paste = pasteRepository.findAll().get(0);
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isFalse();

        testPermissionRequest = new PermissionRequest(testUser2.getName(), paste.getContentLocation(), null);
        String jsonRequestForCreatedPermission = new ObjectMapper().writeValueAsString(testPermissionRequest);
        permissionRepository.deleteAll();
        pasteRepository.deleteAll();
        var requestBuilderForCreatePermission = post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestForCreatedPermission)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePermission)
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message", is("Paste not found!"))

                );
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isFalse();
    }

    @Test
    @WIthMockCustomUser
    void cantCreatePermissionIfUserNotExistentShouldReturnExceptionWithStatusIsNotFound() throws Exception {

        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderForCreatePaste = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePaste);

        Paste paste = pasteRepository.findAll().get(0);
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isFalse();

        testPermissionRequest = new PermissionRequest(testUser2.getName(), paste.getContentLocation(), null);
        String jsonRequestForCreatedPermission = new ObjectMapper().writeValueAsString(testPermissionRequest);

        var requestBuilderForCreatePermission = post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestForCreatedPermission)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePermission)
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message", is("User not found!"))

                );
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isFalse();
    }

    @Test
    @WIthMockCustomUser
    void cantCreatePermissionWithEmptyPublicIdShouldReturnExceptionWithStatusBadRequest() throws Exception {
        userRepository.save(testUser2);
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderForCreatePaste = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePaste);

        Paste paste = pasteRepository.findAll().get(0);
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isFalse();

        testPermissionRequest = new PermissionRequest(testUser2.getName(), "", null);
        String jsonRequestForCreatedPermission = new ObjectMapper().writeValueAsString(testPermissionRequest);

        var requestBuilderForCreatePermission = post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestForCreatedPermission)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePermission)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.permissionRequest.publicPasteId")),
                        jsonPath("$.violations[0].message", is("must not be blank"))

                );
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isFalse();
    }

    @Test
    @WIthMockCustomUser
    void cantCreatePermissionWithEmptyUserNamedShouldReturnExceptionWithStatusBadRequest() throws Exception {
        userRepository.save(testUser2);
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderForCreatePaste = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePaste);

        Paste paste = pasteRepository.findAll().get(0);
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isFalse();

        testPermissionRequest = new PermissionRequest("", paste.getContentLocation(), null);
        String jsonRequestForCreatedPermission = new ObjectMapper().writeValueAsString(testPermissionRequest);

        var requestBuilderForCreatePermission = post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestForCreatedPermission)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePermission)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.permissionRequest.username")),
                        jsonPath("$.violations[0].message", is("must not be blank"))

                );
        assertThat(permissionRepository.existsByPasteAndUser(paste, testUser2)).isFalse();
    }

    @Test
    @WIthMockCustomUser
    void canGetPermissionByIdShouldReturnPermissionWithStatusOk() throws Exception {
        userRepository.save(testUser2);
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderForCreatePaste = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePaste);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();

        testPermissionRequest = new PermissionRequest(testUser2.getName(), paste.getContentLocation(), null);
        String jsonRequestForCreatedPermission = new ObjectMapper().writeValueAsString(testPermissionRequest);
        var requestBuilderForCreatePermission = post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestForCreatedPermission)
                .with(csrf());
        mockMvc.perform(requestBuilderForCreatePermission);
        Permission permission = permissionRepository.findAll().get(0);
        var requestBuilderForGetPermission = get("/api/v1/permission/" + "{uuid}", permission.getId())
                .with(csrf());
        mockMvc.perform(requestBuilderForGetPermission)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(permission.getId().toString())),
                        jsonPath("$.userId", is(testUser2.getId().toString())),
                        jsonPath("$.pasteId", is(paste.getId().toString())),
                        jsonPath("$.createdAt", is(permission.getCreatedAt().toString()))

                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetPermissionByIdIfPermissionNotExistShouldReturnExceptionWithNotFound() throws Exception {
        userRepository.save(testUser2);
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderForCreatePaste = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePaste);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();

        testPermissionRequest = new PermissionRequest(testUser2.getName(), paste.getContentLocation(), null);
        String jsonRequestForCreatedPermission = new ObjectMapper().writeValueAsString(testPermissionRequest);
        var requestBuilderForCreatePermission = post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestForCreatedPermission)
                .with(csrf());
        mockMvc.perform(requestBuilderForCreatePermission);
        Permission permission = permissionRepository.findAll().get(0);

        permissionRepository.deleteAll();
        var requestBuilderForGetPermission = get("/api/v1/permission/" + "{uuid}", permission.getId())
                .with(csrf());
        mockMvc.perform(requestBuilderForGetPermission)
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message", is("Permission not found!"))

                );
    }

    @Test
    @WIthMockCustomUser
    void canDeletePermissionByIdShouldReturnEmptyResponseWithStatusNoContent() throws Exception {
        userRepository.save(testUser2);
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderForCreatePaste = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePaste);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();

        testPermissionRequest = new PermissionRequest(testUser2.getName(), paste.getContentLocation(), null);
        String jsonRequestForCreatedPermission = new ObjectMapper().writeValueAsString(testPermissionRequest);
        var requestBuilderForCreatePermission = post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestForCreatedPermission)
                .with(csrf());
        mockMvc.perform(requestBuilderForCreatePermission);
        Permission permission = permissionRepository.findAll().get(0);
        assertThat(permissionRepository.findAll()).hasSize(1);
        var requestBuilderForGetPermission = delete("/api/v1/permission/" + "{uuid}", permission.getId())
                .with(csrf());

        mockMvc.perform(requestBuilderForGetPermission)
                .andExpectAll(
                        status().isNoContent()
                );
        assertThat(permissionRepository.findAll()).hasSize(0);
    }

    @Test
    @WIthMockCustomUser
    void cantDeletePermissionByIdIfPermissionNotExistShouldReturnExceptionWithNotFound() throws Exception {
        userRepository.save(testUser2);
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderForCreatePaste = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderForCreatePaste);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();

        testPermissionRequest = new PermissionRequest(testUser2.getName(), paste.getContentLocation(), null);
        String jsonRequestForCreatedPermission = new ObjectMapper().writeValueAsString(testPermissionRequest);
        var requestBuilderForCreatePermission = post("/api/v1/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestForCreatedPermission)
                .with(csrf());
        mockMvc.perform(requestBuilderForCreatePermission);
        Permission permission = permissionRepository.findAll().get(0);

        permissionRepository.deleteAll();
        var requestBuilderForGetPermission = delete("/api/v1/permission/" + "{uuid}", permission.getId())
                .with(csrf());
        mockMvc.perform(requestBuilderForGetPermission)
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message", is("Permission not found!"))

                );
    }

}
