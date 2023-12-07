package ru.nechaev.pasteshare.service.impl;

import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nechaev.pasteshare.config.S3ConfigurationProperties;
import ru.nechaev.pasteshare.dto.PasteRequest;
import ru.nechaev.pasteshare.dto.PermissionRequest;
import ru.nechaev.pasteshare.entitity.*;
import ru.nechaev.pasteshare.exception.access.PermissionDeniedEntityAccessException;
import ru.nechaev.pasteshare.exception.db.EntityNotFoundException;
import ru.nechaev.pasteshare.repository.PasteHistoryRepository;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.service.PasteService;
import ru.nechaev.pasteshare.service.PermissionService;
import ru.nechaev.pasteshare.service.S3Service;
import ru.nechaev.pasteshare.service.UserService;
import ru.nechaev.pasteshare.util.Verifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PasteServiceTest {
    @Mock
    private PasteService pasteService;
    @Mock
    private PasteRepository pasteRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private S3ConfigurationProperties properties;
    private PermissionService permissionService;
    private Verifier verifier;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PasteHistoryRepository pasteHistoryRepository;
    @Mock
    private UserService userService;

    private PasteRequest testPasteRequest;
    private PasteHistory pasteHistory;
    private Paste fistPaste;
    private Paste secondPaste;
    private User testUser;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(
                userRepository,
                permissionRepository,
                pasteRepository
        );
        verifier = new Verifier();

        pasteService = new PasteServiceImpl(
                userService,
                pasteRepository,
                s3Service,
                properties,
                permissionService,
                verifier,
                userRepository,
                permissionRepository,
                pasteHistoryRepository);

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

        fistPaste = new Paste(
                testUser,
                "test",
                "test",
                Visibility.PUBLIC,
                LocalDate.parse(testPasteRequest.getExpiredAt(), DateTimeFormatter.ISO_DATE).atStartOfDay(),
                1L);
        secondPaste = new Paste(
                testUser,
                "test2",
                "test2",
                Visibility.PUBLIC,
                LocalDateTime.now(),
                1L);
        pasteHistory = new PasteHistory(
                fistPaste,
                RevisionType.ADD);
    }

    @Test
    void canCreatePasteWithValidData() {
        given(userService.getCurrentUser()).willReturn(testUser);
        ArgumentCaptor<PermissionRequest> permissionArgumentCaptor = ArgumentCaptor.forClass(PermissionRequest.class);
        ArgumentCaptor<Paste> pasteArgumentCaptor = ArgumentCaptor.forClass(Paste.class);
        Paste paste = pasteService.create(testPasteRequest);

        verify(pasteRepository, times(1)).save(pasteArgumentCaptor.capture());
        verify(permissionService, times(1)).create(permissionArgumentCaptor.capture());
        assertThat(permissionArgumentCaptor.getValue().getUsername()).isEqualTo(testUser.getName());
        assertThat(pasteArgumentCaptor.getValue().getTitle()).isEqualTo(fistPaste.getTitle());
        assertThat(paste.getTitle()).isEqualTo(testPasteRequest.getTitle());
    }

    @Test
    void canGetByIdWithValidIdShouldReturnPaste() {
        UUID id = UUID.randomUUID();
        given(pasteRepository.findById(id)).willReturn(Optional.of(fistPaste));

        Paste paste = pasteService.getById(id);

        verify(pasteRepository, times(1)).findById(id);
        assertThat(paste).isEqualTo(fistPaste);
    }

    @Test
    void cantGetByIdWithNonExistentIdShouldThrowException() {
        UUID id = UUID.randomUUID();
        given(pasteRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pasteService.getById(id)).isInstanceOf(EntityNotFoundException.class);

        verify(pasteRepository, times(1)).findById(id);
    }

    @Test
    void cantGetByIdWithWithoutPermissionShouldThrowException() {
        UUID id = UUID.randomUUID();
        fistPaste.setVisibility(Visibility.PRIVATE);
        given(pasteRepository.findById(id)).willReturn(Optional.of(fistPaste));
        given(userService.getCurrentUser()).willReturn(testUser);
        given(permissionRepository.existsByPasteAndUser(fistPaste, testUser)).willReturn(false);

        assertThatThrownBy(() -> pasteService.getById(id)).isInstanceOf(PermissionDeniedEntityAccessException.class);

        verify(pasteRepository, times(1)).findById(id);
    }

    @Test
    void canUpdateWithExistentPasteShouldReturnUpdatedPaste() {
        testPasteRequest.setPublicPasteUrl("test");
        given(pasteRepository.findPasteByContentLocation("test")).willReturn(Optional.of(fistPaste));
        Long versionAfterUpdate = fistPaste.getVersion();

        Paste paste = pasteService.update(testPasteRequest);

        verify(pasteRepository, times(1)).save(paste);
        assertThat(paste.getVersion()).isEqualTo(versionAfterUpdate + 1);
    }

    @Test
    void canUpdateWithNonExistentPasteShouldThrowException() {
        testPasteRequest.setPublicPasteUrl("test");
        given(pasteRepository.findPasteByContentLocation("test")).willReturn(Optional.empty());

        assertThatThrownBy(() -> pasteService.update(testPasteRequest)).isInstanceOf(EntityNotFoundException.class);
        verify(pasteRepository, never()).save(any(Paste.class));
    }

    @Test
    void updateShouldBeForNonNullFields() {
        testPasteRequest.setPublicPasteUrl("test");
        testPasteRequest.setTitle(null);
        testPasteRequest.setExpiredAt("2024-11-05");
        given(pasteRepository.findPasteByContentLocation("test")).willReturn(Optional.of(fistPaste));

        Paste paste = pasteService.update(testPasteRequest);

        verify(pasteRepository, times(1)).save(paste);
        assertThat(paste.getTitle()).isNotNull();
        assertThat(paste.getExpiredAt()).isEqualTo(
                LocalDate.parse(
                        testPasteRequest.getExpiredAt(),
                        DateTimeFormatter.ISO_DATE).atStartOfDay());

    }

    @Test
    void canDeleteWithExistentPaste() {
        UUID pasteId = UUID.randomUUID();
        fistPaste.setId(pasteId);
        ArgumentCaptor<UUID> argumentCaptor = ArgumentCaptor.forClass(UUID.class);
        given(pasteRepository.existsById(pasteId)).willReturn(true);

        pasteService.delete(pasteId);

        verify(permissionRepository, times(1)).deleteAllByPasteId(argumentCaptor.capture());
        verify(pasteRepository, times(1)).deleteById(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(pasteId);
    }

    @Test
    void canDeleteWithNonExistentPaste() {
        UUID pasteId = UUID.randomUUID();
        fistPaste.setId(pasteId);

        given(pasteRepository.existsById(pasteId)).willReturn(false);

        assertThatThrownBy(() -> pasteService.delete(pasteId)).isInstanceOf(EntityNotFoundException.class);
        verify(permissionRepository, never()).deleteAllByPasteId(pasteId);
        verify(pasteRepository, never()).deleteById(pasteId);
    }

    @Test
    void canGetPasteByPublicIdWithExistentPaste() {
        String publicId = "test";
        fistPaste.setContentLocation(publicId);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        given(pasteRepository.findPasteByContentLocation(publicId)).willReturn(Optional.of(fistPaste));

        Paste paste = pasteService.getPasteByPublicId(publicId);

        verify(pasteRepository, times(1)).findPasteByContentLocation(argumentCaptor.capture());
        assertThat(paste).isEqualTo(fistPaste);
        assertThat(argumentCaptor.getValue()).isEqualTo(publicId);
    }

    @Test
    void canGetPasteByPublicIdWithNonExistentPasteShouldThrowException() {
        String publicId = "test";
        given(pasteRepository.findPasteByContentLocation(publicId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pasteService.getPasteByPublicId(publicId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void canGetPasteByPublicIdWithoutPermissionShouldThrowException() {
        String publicId = "test";
        fistPaste.setVisibility(Visibility.PRIVATE);
        given(pasteRepository.findPasteByContentLocation(publicId)).willReturn(Optional.of(fistPaste));
        given(userService.getCurrentUser()).willReturn(testUser);
        given(permissionRepository.existsByPasteAndUser(fistPaste, testUser)).willReturn(false);

        assertThatThrownBy(() -> pasteService.getPasteByPublicId(publicId)).isInstanceOf(PermissionDeniedEntityAccessException.class);

        verify(pasteRepository, times(1)).findPasteByContentLocation(publicId);
    }

    @Test
    void canGetPasteByUserIdWithExistentUser() {
        given(userService.getCurrentUser()).willReturn(testUser);
        given(userRepository.existsById(testUser.getId())).willReturn(true);
        given(pasteRepository.getPastesByUserId(testUser.getId())).willReturn(List.of(fistPaste, secondPaste));

        List<Paste> pastes = pasteService.getPastesByUserId(testUser.getId());

        verify(pasteRepository, times(1)).getPastesByUserId(testUser.getId());
        assertThat(pastes).hasSize(2);
    }

    @Test
    void cantGetPasteByUserWithExistentUser() {
        given(userRepository.existsById(testUser.getId())).willReturn(false);

        assertThatThrownBy(() -> pasteService.getPastesByUserId(testUser.getId())).isInstanceOf(EntityNotFoundException.class);
        verify(pasteRepository, never()).getPastesByUserId(testUser.getId());
    }

    @Test
    void canGetPasteByVersionWithExistentPasteAndVersionShouldReturnPaste() {
        UUID pasteId = UUID.randomUUID();
        fistPaste.setId(pasteId);
        given(pasteRepository.existsById(pasteId)).willReturn(true);
        given(pasteHistoryRepository.findPasteByVersion(pasteId, 1L)).willReturn(Optional.of(pasteHistory));

        Paste paste = pasteService.getPasteByVersion(pasteId, 1L);

        verify(pasteHistoryRepository, times(1)).findPasteByVersion(pasteId, 1L);
        assertThat(paste).isEqualTo(fistPaste);
    }

    @Test
    void cantGetPasteByVersionWithNonExistentPasteShouldThrowException() {
        UUID pasteId = UUID.randomUUID();
        fistPaste.setId(pasteId);
        given(pasteRepository.existsById(pasteId)).willReturn(true);
        given(pasteHistoryRepository.findPasteByVersion(pasteId, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pasteService.getPasteByVersion(pasteId, 1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void cantGetPasteByVersionWithNonExistentVersionShouldThrowException() {
        UUID pasteId = UUID.randomUUID();
        fistPaste.setId(pasteId);
        given(pasteRepository.existsById(pasteId)).willReturn(true);
        given(pasteHistoryRepository.findPasteByVersion(pasteId, 3L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pasteService.getPasteByVersion(pasteId, 3L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void cantGetPasteByVersionWithoutPermissionShouldThrowException() {
        UUID pasteId = UUID.randomUUID();
        fistPaste.setId(pasteId);

        fistPaste.setVisibility(Visibility.PRIVATE);
        given(pasteRepository.existsById(pasteId)).willReturn(true);
        given(pasteHistoryRepository.findPasteByVersion(pasteId, 1L)).willReturn(Optional.of(pasteHistory));
        given(userService.getCurrentUser()).willReturn(testUser);
        given(permissionRepository.existsByPasteAndUser(fistPaste, testUser)).willReturn(false);

        assertThatThrownBy(() -> pasteService.getPasteByVersion(pasteId, 1L)).isInstanceOf(PermissionDeniedEntityAccessException.class);
    }

    @Test
    void cantGetPasteByVersionAndPublicIdWithExistentPasteShouldThrowException() {
        String pasteId = "test";
        fistPaste.setContentLocation(pasteId);
        given(pasteRepository.existsByContentLocation(pasteId)).willReturn(true);
        given(pasteHistoryRepository.findPasteByVersionAndPublicId(pasteId, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pasteService.getPasteByVersionAndPublicId(pasteId, 1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void cantGetPasteByVersionAndPublicIdWithExistentVersionShouldThrowException() {
        String pasteId = "test";
        fistPaste.setContentLocation(pasteId);
        given(pasteRepository.existsByContentLocation(pasteId)).willReturn(true);
        given(pasteHistoryRepository.findPasteByVersionAndPublicId(pasteId, 3L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pasteService.getPasteByVersionAndPublicId(pasteId, 3L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void cantGetPasteByVersionAndPublicIdWithoutPermissionShouldThrowException() {
        String pasteId = "test";
        fistPaste.setContentLocation(pasteId);

        fistPaste.setVisibility(Visibility.PRIVATE);
        given(pasteRepository.existsByContentLocation(pasteId)).willReturn(true);
        given(pasteHistoryRepository.findPasteByVersionAndPublicId(pasteId, 1L)).willReturn(Optional.of(pasteHistory));
        given(userService.getCurrentUser()).willReturn(testUser);
        given(permissionRepository.existsByPasteAndUser(fistPaste, testUser)).willReturn(false);

        assertThatThrownBy(() -> pasteService.getPasteByVersionAndPublicId(pasteId, 1L)).isInstanceOf(PermissionDeniedEntityAccessException.class);
    }

    @Test
    void canGetAllPasterRevisionWithExistentPaste() {
        UUID pasteId = UUID.randomUUID();
        fistPaste.setId(pasteId);
        given(pasteRepository.findById(pasteId)).willReturn(Optional.of(fistPaste));
        given(pasteHistoryRepository.getPasteRevisions(pasteId)).willReturn(List.of(new PasteHistory(fistPaste, RevisionType.ADD)));

        List<PasteHistory> pasteHistories = pasteService.getAllPasteRevision(pasteId);

        verify(pasteHistoryRepository, times(1)).getPasteRevisions(pasteId);

        assertThat(pasteHistories).hasSize(1);
    }

    @Test
    void cantGetAllPasteRevisionWithExistentPasteShouldThrowException() {
        UUID pasteId = UUID.randomUUID();
        fistPaste.setId(pasteId);
        given(pasteRepository.findById(pasteId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pasteService.getAllPasteRevision(pasteId)).isInstanceOf(EntityNotFoundException.class);

        verify(pasteHistoryRepository, never()).getPasteRevisions(pasteId);
    }

    @Test
    void cantGetAllPasteRevisionWithoutPermissionShouldThrowException() {
        UUID pasteId = UUID.randomUUID();
        fistPaste.setId(pasteId);

        fistPaste.setVisibility(Visibility.PRIVATE);
        given(pasteRepository.findById(pasteId)).willReturn(Optional.of(fistPaste));
        given(userService.getCurrentUser()).willReturn(testUser);
        given(permissionRepository.existsByPasteAndUser(fistPaste, testUser)).willReturn(false);

        assertThatThrownBy(() -> pasteService.getAllPasteRevision(pasteId)).isInstanceOf(PermissionDeniedEntityAccessException.class);
        verify(pasteHistoryRepository, never()).getPasteRevisions(pasteId);
    }


}