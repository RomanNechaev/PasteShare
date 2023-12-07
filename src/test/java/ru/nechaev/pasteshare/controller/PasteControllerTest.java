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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.nechaev.pasteshare.WIthMockCustomUser;
import ru.nechaev.pasteshare.dto.PasteRequest;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.Role;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.service.PasteService;
import ru.nechaev.pasteshare.service.S3Service;

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
class PasteControllerTest {
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
    private S3Service s3Service;
    @Autowired
    private PasteService pasteService;

    private User testUser;

    private PasteRequest testPasteRequest;

    @BeforeEach
    void setUp() {
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
    void canCreatePasteWithValidDataShouldReturnEmptyWithStatusCreated() throws Exception {
        assertThat(pasteRepository.findAll()).hasSize(0);
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isCreated()
                );
        assertThat(pasteRepository.findAll()).hasSize(1);
        Paste paste = pasteRepository.findAll().get(0);
        Assertions.assertAll(
                () -> assertThat(paste.getTitle()).isEqualTo(testPasteRequest.getTitle()),
                () -> assertThat(paste.getVisibility().toString()).isEqualTo(testPasteRequest.getVisibility()),
                () -> assertThat(paste.getVersion()).isEqualTo(1L)
        );
    }

    @Test
    @WIthMockCustomUser
    void cantCreatePasteWithEmptyTitleShouldReturnValidationExceptionWithStatusBadRequest() throws Exception {
        assertThat(pasteRepository.findAll()).hasSize(0);
        testPasteRequest.setTitle("");
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.paste.title")),
                        jsonPath("$.violations[0].message", is("must not be blank"))
                );
        assertThat(pasteRepository.findAll()).hasSize(0);
    }

    @Test
    @WIthMockCustomUser
    void cantCreatePasteWithEmptyVisibilityShouldReturnValidationExceptionWithStatusBadRequest() throws Exception {
        assertThat(pasteRepository.findAll()).hasSize(0);
        testPasteRequest.setVisibility("");
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(2)),
                        jsonPath("$.violations[0].fieldName", is("create.paste.visibility")),
                        jsonPath("$.violations[0].message", is("must not be blank")),
                        jsonPath("$.violations[1].fieldName", is("create.paste.visibility")),
                        jsonPath("$.violations[1].message", is("The visibility should be PRIVATE or PUBLIC"))
                );
        assertThat(pasteRepository.findAll()).hasSize(0);
    }

    @Test
    @WIthMockCustomUser
    void cantCreatePasteWithInvalidExpiredAtFormatShouldReturnValidationExceptionWithStatusBadRequest() throws Exception {
        assertThat(pasteRepository.findAll()).hasSize(0);
        testPasteRequest.setExpiredAt("03-04-2023");
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.paste.expiredAt")),
                        jsonPath("$.violations[0].message", is("The date format is incorrect, should be -> year-mounts-day"))
                );
        assertThat(pasteRepository.findAll()).hasSize(0);
    }

    @Test
    @WIthMockCustomUser
    void cantCreatePasteWithInvalidVisibilityTypeShouldReturnValidationExceptionWithStatusBadRequest() throws Exception {
        assertThat(pasteRepository.findAll()).hasSize(0);
        testPasteRequest.setVisibility("tratara");
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.paste.visibility")),
                        jsonPath("$.violations[0].message", is("The visibility should be PRIVATE or PUBLIC"))
                );
        assertThat(pasteRepository.findAll()).hasSize(0);
    }

    @Test
    @WIthMockCustomUser
    void cantCreatePasteWithEmptyTextShouldReturnValidationExceptionWithStatusBadRequest() throws Exception {
        assertThat(pasteRepository.findAll()).hasSize(0);
        testPasteRequest.setText("");
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("create.paste.text")),
                        jsonPath("$.violations[0].message", is("must not be blank"))
                );
        assertThat(pasteRepository.findAll()).hasSize(0);
    }

    @Test
    @WIthMockCustomUser
    void canUpdatePasteWithValidDataShouldReturnEmptyWithStatusOK() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);

        String publicId = pasteRepository.findAll().get(0).getContentLocation();
        testPasteRequest.setText("lol");
        testPasteRequest.setPublicPasteUrl(publicId);
        assertThat(pasteRepository.findAll()).hasSize(1);
        String jsonRequestUpdate = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = put("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestUpdate)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk()
                );
        assertThat(pasteRepository.findAll()).hasSize(1);
        Paste paste = pasteRepository.findAll().get(0);
        Assertions.assertAll(
                () -> assertThat(paste.getTitle()).isEqualTo(testPasteRequest.getTitle()),
                () -> assertThat(paste.getVisibility().toString()).isEqualTo(testPasteRequest.getVisibility()),
                () -> assertThat(paste.getVersion()).isEqualTo(2L)
        );

    }

    @Test
    @WIthMockCustomUser
    void cantUpdateWithInvalidExpiredAtFormatShouldReturnValidationExceptionWithStatusBadRequest() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        String publicId = paste.getContentLocation();
        String expireAtBeforeUpdate = paste.getExpiredAt().toLocalDate().toString();
        testPasteRequest.setExpiredAt("04-03-2029");
        testPasteRequest.setPublicPasteUrl(publicId);

        assertThat(pasteRepository.findAll()).hasSize(1);
        String jsonRequestUpdate = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = put("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestUpdate)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("update.paste.expiredAt")),
                        jsonPath("$.violations[0].message", is("The date format is incorrect, should be -> year-mounts-day"))
                );
        String expireAtAfterUpdate = pasteRepository.findAll().get(0).getExpiredAt().toLocalDate().toString();
        assertThat(expireAtAfterUpdate).isEqualTo(expireAtBeforeUpdate);
    }

    @Test
    @WIthMockCustomUser
    void cantUpdateWithNullExpiredAtShouldUpdatedAllParamWithoutNullParam() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);

        Paste paste = pasteRepository.findAll().get(0);
        String publicId = paste.getContentLocation();
        String expireAtBeforeUpdate = paste.getExpiredAt().toLocalDate().toString();
        testPasteRequest.setPublicPasteUrl(publicId);
        testPasteRequest.setExpiredAt(null);
        assertThat(pasteRepository.findAll()).hasSize(1);
        String jsonRequestUpdate = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = put("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestUpdate)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk()
                );
        String expireAtAfterUpdate = pasteRepository.findAll().get(0).getExpiredAt().toLocalDate().toString();
        assertThat(expireAtAfterUpdate).isEqualTo(expireAtBeforeUpdate);
    }

    @Test
    @WIthMockCustomUser
    void cantUpdateWithEmptyPublicIdShouldReturnValidationExceptionWithStatusBadRequest() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        testPasteRequest.setPublicPasteUrl("");

        String jsonRequestUpdate = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = put("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestUpdate)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].fieldName", is("update.paste.publicPasteUrl")),
                        jsonPath("$.violations[0].message", is("must not be blank"))
                );
        assertThat(pasteRepository.findAll().get(0).getId()).isEqualTo(paste.getId());
    }

    @Test
    @WIthMockCustomUser
    void cantUpdateIfPasteNonExistentShouldReturnNotFoundExceptionWithStatusBadRequest() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        testPasteRequest.setPublicPasteUrl(paste.getContentLocation());
        permissionRepository.deleteAll();
        pasteRepository.deleteAll();

        String jsonRequestUpdate = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilder = put("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestUpdate)
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Paste not found"))
                );
    }

    @Test
    @WIthMockCustomUser
    void canGetPasteWithPublicIdAndExistentPasteShouldReturnPasteWithStatusOk() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        testPasteRequest.setPublicPasteUrl(paste.getContentLocation());
        String content = pasteService.getPasteContent(paste);

        var requestBuilder = get("/api/v1/paste" + "/{publicId}", paste.getContentLocation())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(paste.getId().toString())),
                        jsonPath("$.user_id", is(paste.getUser().getId().toString())),
                        jsonPath("$.title", is(paste.getTitle())),
                        jsonPath("$.publicId", is(paste.getContentLocation())),
                        jsonPath("$.createdAt", is(paste.getCreatedAt().toString())),
                        jsonPath("$.expiredAt", is(paste.getExpiredAt().toString())),
                        jsonPath("$.visibility", is(paste.getVisibility().toString())),
                        jsonPath("$.version", is(paste.getVersion().intValue())),
                        jsonPath("$.content", is(content))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetPasteWithPublicIdAndNonExistentPasteShouldReturnExceptionWithStatusNotFound() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();
        pasteRepository.deleteAll();

        var requestBuilder = get("/api/v1/paste" + "/{publicId}", paste.getContentLocation())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Entity not found!"))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetPasteWithoutPermissionShouldReturnExceptionWithStatus() throws Exception {
        testPasteRequest.setVisibility("PRIVATE");
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());
        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();

        var requestBuilder = get("/api/v1/paste" + "/{publicId}", paste.getContentLocation())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("You dont have access to view this paste"))
                );
    }

    @Test
    @WIthMockCustomUser
    void canGetPasteWithPermissionShouldReturnPasteWithStatusOk() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        String content = pasteService.getPasteContent(paste);

        var requestBuilder = get("/api/v1/paste" + "/{publicId}", paste.getContentLocation())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(paste.getId().toString())),
                        jsonPath("$.user_id", is(paste.getUser().getId().toString())),
                        jsonPath("$.title", is(paste.getTitle())),
                        jsonPath("$.publicId", is(paste.getContentLocation())),
                        jsonPath("$.createdAt", is(paste.getCreatedAt().toString())),
                        jsonPath("$.expiredAt", is(paste.getExpiredAt().toString())),
                        jsonPath("$.visibility", is(paste.getVisibility().toString())),
                        jsonPath("$.version", is(paste.getVersion().intValue())),
                        jsonPath("$.content", is(content))
                );
    }

    @Test
    @WIthMockCustomUser
    void canGetPasteWithPublicIdAndVersionWithExistentPasteShouldReturnPasteWithStatusOk() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        testPasteRequest.setPublicPasteUrl(paste.getContentLocation());
        String content = pasteService.getPasteContent(paste);
        Long version = paste.getVersion();

        var requestBuilder = get("/api/v1/paste/" + "public" + "/{publicId}/" + version,
                paste.getContentLocation())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(paste.getId().toString())),
                        jsonPath("$.user_id", is(paste.getUser().getId().toString())),
                        jsonPath("$.title", is(paste.getTitle())),
                        jsonPath("$.publicId", is(paste.getContentLocation())),
                        jsonPath("$.createdAt", is(paste.getCreatedAt().toString())),
                        jsonPath("$.expiredAt", is(paste.getExpiredAt().toString())),
                        jsonPath("$.visibility", is(paste.getVisibility().toString())),
                        jsonPath("$.version", is(paste.getVersion().intValue())),
                        jsonPath("$.content", is(content))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetPasteByPublicIdAndVersionAndNonExistentPasteShouldReturnExceptionWithStatusNotFound() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();
        pasteRepository.deleteAll();

        Long version = paste.getVersion();

        var requestBuilder = get("/api/v1/paste/" + "public" + "/{publicId}/" + version,
                paste.getContentLocation())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Entity not found!"))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetPasteByPublicIdAndVersionAndNonExistentVersionShouldReturnExceptionWithStatusNotFound() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);

        Long version = paste.getVersion() + 10;

        var requestBuilder = get("/api/v1/paste/" + "public" + "/{publicId}/" + version,
                paste.getContentLocation())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is(String.format("Entity with version %d not found", version)))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetPasteByPublicIdAndVersionWithoutPermissionShouldReturnExceptionWithStatusForbidden() throws Exception {
        testPasteRequest.setVisibility("PRIVATE");
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());
        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();
        Long version = paste.getVersion();

        var requestBuilder = get("/api/v1/paste/" + "public" + "/{publicId}/" + version,
                paste.getContentLocation())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("You dont have access to view this paste"))
                );
    }

    @Test
    @WIthMockCustomUser
    void canGetPasteByPublicIdAndVersionWithPermissionShouldReturnPasteWithStatusOk() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        String content = pasteService.getPasteContent(paste);

        Long version = paste.getVersion();

        var requestBuilder = get("/api/v1/paste/" + "public" + "/{publicId}/" + version,
                paste.getContentLocation())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(paste.getId().toString())),
                        jsonPath("$.user_id", is(paste.getUser().getId().toString())),
                        jsonPath("$.title", is(paste.getTitle())),
                        jsonPath("$.publicId", is(paste.getContentLocation())),
                        jsonPath("$.createdAt", is(paste.getCreatedAt().toString())),
                        jsonPath("$.expiredAt", is(paste.getExpiredAt().toString())),
                        jsonPath("$.visibility", is(paste.getVisibility().toString())),
                        jsonPath("$.version", is(paste.getVersion().intValue())),
                        jsonPath("$.content", is(content))
                );
    }

    @Test
    @WIthMockCustomUser
    void canGetPasteWithUUIDAndVersionWithExistentPasteShouldReturnPasteWithStatusOk() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        testPasteRequest.setPublicPasteUrl(paste.getContentLocation());
        String content = pasteService.getPasteContent(paste);
        Long version = paste.getVersion();

        var requestBuilder = get("/api/v1/paste/" + "/{uuid}/" + version,
                paste.getId())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(paste.getId().toString())),
                        jsonPath("$.user_id", is(paste.getUser().getId().toString())),
                        jsonPath("$.title", is(paste.getTitle())),
                        jsonPath("$.publicId", is(paste.getContentLocation())),
                        jsonPath("$.createdAt", is(paste.getCreatedAt().toString())),
                        jsonPath("$.expiredAt", is(paste.getExpiredAt().toString())),
                        jsonPath("$.visibility", is(paste.getVisibility().toString())),
                        jsonPath("$.version", is(paste.getVersion().intValue())),
                        jsonPath("$.content", is(content))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetPasteByUUIDAndVersionAndNonExistentPasteShouldReturnExceptionWithStatusNotFound() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();
        pasteRepository.deleteAll();

        Long version = paste.getVersion();

        var requestBuilder = get("/api/v1/paste/" + "/{uuid}/" + version,
                paste.getId())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Entity not found!"))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetPasteByUUIDAndVersionAndNonExistentVersionShouldReturnExceptionWithStatusNotFound() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);

        Long version = paste.getVersion() + 10;

        var requestBuilder = get("/api/v1/paste/" + "/{uuid}/" + version,
                paste.getId())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is(String.format("Entity with version %d not found", version)))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetPasteByUUIDAndVersionWithoutPermissionShouldReturnExceptionWithStatusForbidden() throws Exception {
        testPasteRequest.setVisibility("PRIVATE");
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());
        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();
        Long version = paste.getVersion();

        var requestBuilder = get("/api/v1/paste/" + "/{uuid}/" + version,
                paste.getId())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("You dont have access to view this paste"))
                );
    }

    @Test
    @WIthMockCustomUser
    void canGetPasteByUUIDAndVersionWithPermissionShouldReturnPasteWithStatusOk() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        String content = pasteService.getPasteContent(paste);

        Long version = paste.getVersion();

        var requestBuilder = get("/api/v1/paste/" + "/{uuid}/" + version,
                paste.getId())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(paste.getId().toString())),
                        jsonPath("$.user_id", is(paste.getUser().getId().toString())),
                        jsonPath("$.title", is(paste.getTitle())),
                        jsonPath("$.publicId", is(paste.getContentLocation())),
                        jsonPath("$.createdAt", is(paste.getCreatedAt().toString())),
                        jsonPath("$.expiredAt", is(paste.getExpiredAt().toString())),
                        jsonPath("$.visibility", is(paste.getVisibility().toString())),
                        jsonPath("$.version", is(paste.getVersion().intValue())),
                        jsonPath("$.content", is(content))
                );
    }

    @Test
    @WIthMockCustomUser
    void canGetAllPasteByUserIdShouldReturnPastesWithStatusOk() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());
        //addFirstPaste
        mockMvc.perform(requestBuilderCreate);
        testPasteRequest.setTitle("test2");
        //addSecondPaste
        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        String content = pasteService.getPasteContent(paste);
        User user = userRepository.findAll().get(0);

        var requestBuilder = get("/api/v1/paste/all/" + "{userId}", testUser.getId())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].id", is(paste.getId().toString())),
                        jsonPath("$[0].user_id", is(paste.getUser().getId().toString())),
                        jsonPath("$[0].title", is(paste.getTitle())),
                        jsonPath("$[0].publicId", is(paste.getContentLocation())),
                        jsonPath("$[0].createdAt", is(paste.getCreatedAt().toString())),
                        jsonPath("$[0].expiredAt", is(paste.getExpiredAt().toString())),
                        jsonPath("$[0].visibility", is(paste.getVisibility().toString())),
                        jsonPath("$[0].version", is(paste.getVersion().intValue())),
                        jsonPath("$[0].content", is(content))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetAllPasteByUserIdIfUserNotExistsShouldReturnExceptionWithStatusNotFound() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());
        //addFirstPaste
        mockMvc.perform(requestBuilderCreate);
        testPasteRequest.setTitle("test2");
        //addSecondPaste
        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);

        var requestBuilder = get("/api/v1/paste/all/" + "{userId}", UUID.randomUUID())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("User not found!"))
                );
    }

    @Test
    @WIthMockCustomUser
    void cantGetAllPastesByUserIdWithoutPermissionShouldReturnExceptionWithStatusForbidden() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        User testUser2 = new User(
                UUID.randomUUID(),
                "test2",
                "test2@gmail.com",
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER,
                "123");

        userRepository.save(testUser2);

        //addFirstPaste
        mockMvc.perform(requestBuilderCreate);
        //addSecondPaste
        mockMvc.perform(requestBuilderCreate);

        permissionRepository.deleteAll();

        var requestBuilder = get("/api/v1/paste/all/" + "{userId}", testUser2.getId())
                .with(csrf());
        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("You dont have access to view this pastes"))
                );
    }

    @Test
    @WIthMockCustomUser
    void canGetAllPasteVersionsShouldReturnPasteHistoriesWithStatusOk() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        String publicId = paste.getContentLocation();
        testPasteRequest.setText("lol");
        testPasteRequest.setPublicPasteUrl(publicId);
        String jsonRequestUpdate = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderUpdated = put("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestUpdate)
                .with(csrf());

        mockMvc.perform(requestBuilderUpdated)
                .andExpectAll(
                        status().isOk()
                );

        var requestBuilder = get("/api/v1/paste/all/versions/" + "{pasteId}", paste.getId())
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].id", is(paste.getId().toString())),
                        jsonPath("$[0].user_id", is(paste.getUser().getId().toString())),
                        jsonPath("$[0].title", is(paste.getTitle())),
                        jsonPath("$[0].publicId", is(paste.getContentLocation())),
                        jsonPath("$[0].createdAt", is(paste.getCreatedAt().toString())),
                        jsonPath("$[0].expiredAt", is(paste.getExpiredAt().toString())),
                        jsonPath("$[0].visibility", is(paste.getVisibility().toString())),
                        jsonPath("$[0].version", is(paste.getVersion().intValue()))
                );
    }

    @Test
    @WIthMockCustomUser
    void canGetAllPasteVersionsIfPasteIsNotExistsShouldReturnExceptionsWithStatusNotFound() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        String publicId = paste.getContentLocation();
        testPasteRequest.setText("lol");
        testPasteRequest.setPublicPasteUrl(publicId);
        String jsonRequestUpdate = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderUpdated = put("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestUpdate)
                .with(csrf());

        mockMvc.perform(requestBuilderUpdated)
                .andExpectAll(
                        status().isOk()
                );
        permissionRepository.deleteAll();
        pasteRepository.deleteAll();

        var requestBuilder = get("/api/v1/paste/all/versions/" + "{pasteId}", paste.getId())
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Paste not found"))
                );
    }

    @Test
    @WIthMockCustomUser
    void canDeletePasteShouldReturnEmptyResponseWithStatusNoContent() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);

        var requestBuilder = delete("/api/v1/paste/" + "{uuid}", paste.getId())
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNoContent());

        assertThat(pasteRepository.findAll()).hasSize(0);
    }

    @Test
    @WIthMockCustomUser
    void canDeletePasteIfPasteNotExistsShouldReturnExceptionWithStatusNotFound() throws Exception {
        String jsonRequest = new ObjectMapper().writeValueAsString(testPasteRequest);
        var requestBuilderCreate = post("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf());

        mockMvc.perform(requestBuilderCreate);
        Paste paste = pasteRepository.findAll().get(0);
        permissionRepository.deleteAll();
        pasteRepository.deleteAll();

        var requestBuilder = delete("/api/v1/paste/" + "{uuid}", paste.getId())
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message", is("Paste not found")));

    }
}
