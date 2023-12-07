package ru.nechaev.pasteshare.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nechaev.pasteshare.dto.PermissionRequest;
import ru.nechaev.pasteshare.entitity.*;
import ru.nechaev.pasteshare.exception.db.EntityNotFoundException;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.service.PermissionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PasteRepository pasteRepository;
    private PermissionService permissionService;
    private User testUser;
    private Paste fistPaste;
    private PermissionRequest testPermissionRequest;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(
                userRepository,
                permissionRepository,
                pasteRepository);

        testUser = new User(UUID.randomUUID(),
                "test",
                "test@gmail.com",
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER,
                "123");

        fistPaste = new Paste(
                testUser,
                "test",
                "test",
                Visibility.PUBLIC,
                LocalDate.parse("2024-12-12", DateTimeFormatter.ISO_DATE).atStartOfDay(),
                1L);

        testPermissionRequest = new PermissionRequest(
                "test",
                "test",
                UUID.randomUUID());
        testPermission = new Permission(
                testUser,
                fistPaste
        );
    }

    @Test
    void canCreatePermissionWithValidData() {
        given(userRepository.findUserByName("test")).willReturn(Optional.of(testUser));
        given(pasteRepository.findPasteByContentLocation("test")).willReturn(Optional.of(fistPaste));

        Permission permission = permissionService.create(testPermissionRequest);

        verify(permissionRepository, times(1)).save(permission);
        assertThat(permission.getPaste()).isEqualTo(fistPaste);
        assertThat(permission.getUser()).isEqualTo(testUser);
    }

    @Test
    void cantCreatePermissionWithNonExistentUserShouldThrowException() {
        given(userRepository.findUserByName(testPermissionRequest.getUsername())).willReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.create(testPermissionRequest)).isInstanceOf(EntityNotFoundException.class);
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    void cantCreatePermissionWithNonExistentPasteShouldThrowException() {
        given(userRepository.findUserByName(testPermissionRequest.getUsername())).willReturn(Optional.of(testUser));
        given(pasteRepository.findPasteByContentLocation(testPermissionRequest.getPublicPasteId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.create(testPermissionRequest)).isInstanceOf(EntityNotFoundException.class);
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    void canDeletePermissionById() {
        UUID permissionId = UUID.randomUUID();
        given(permissionRepository.existsById(permissionId)).willReturn(true);
        ArgumentCaptor<UUID> argumentCaptor = ArgumentCaptor.forClass(UUID.class);

        permissionService.delete(permissionId);

        verify(permissionRepository, times(1)).deleteById(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(permissionId);
    }

    @Test
    void cantDeletePermissionByIdWithNonExistentPermissionShouldThrowException() {
        UUID permissionId = UUID.randomUUID();
        given(permissionRepository.existsById(permissionId)).willReturn(false);

        assertThatThrownBy(() -> permissionService.delete(permissionId));

        verify(permissionRepository, never()).deleteById(permissionId);
    }

    @Test
    void canGetPermissionByIdWithExistentPermission() {
        UUID permissionId = UUID.randomUUID();
        testPermission.setId(permissionId);
        given(permissionRepository.findById(permissionId)).willReturn(Optional.of(testPermission));

        Permission permission = permissionService.getById(permissionId);

        verify(permissionRepository, times(1)).findById(permissionId);
        assertThat(permission).isEqualTo(testPermission);
    }

    @Test
    void cantGetPermissionByIdWithNonExistentPermissionShouldThrowExeception() {
        UUID permissionId = UUID.randomUUID();
        testPermission.setId(permissionId);
        given(permissionRepository.findById(permissionId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getById(permissionId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void canConfirmWithPermission() {
        given(permissionRepository.existsByPasteAndUser(fistPaste, testUser)).willReturn(true);

        assertThat(permissionService.confirm(fistPaste, testUser)).isTrue();
    }

    @Test
    void canConfirmWithNonPermission() {
        given(permissionRepository.existsByPasteAndUser(fistPaste, testUser)).willReturn(false);

        assertThat(permissionService.confirm(fistPaste, testUser)).isFalse();
    }
}