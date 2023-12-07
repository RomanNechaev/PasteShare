package ru.nechaev.pasteshare.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.nechaev.pasteshare.dto.AuthenticationRequest;
import ru.nechaev.pasteshare.entitity.Role;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.exception.db.EntityExistsException;
import ru.nechaev.pasteshare.exception.db.EntityNotFoundException;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.security.Token;
import ru.nechaev.pasteshare.security.TokenUser;
import ru.nechaev.pasteshare.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasteRepository pasteRepository;
    @Mock
    private PermissionRepository permissionRepository;

    private AuthenticationRequest testRequest;

    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                passwordEncoder,
                pasteRepository,
                permissionRepository
        );
        testUser = new User(UUID.randomUUID(),
                "test",
                "test@gmail.com",
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER,
                "123");
        testRequest = AuthenticationRequest.builder()
                .username("test")
                .password("123")
                .email("test@test.test")
                .build();
    }

    @Test
    void canCreateUserWithValidAuthenticationData() {
        given(userRepository.existsByName("test")).willReturn(false);

        userService.create(testRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void cantCreateUserWithValidAuthenticationData() {
        given(userRepository.existsByName("test")).willReturn(true);

        assertThatThrownBy(() -> userService.create(testRequest)).isInstanceOf(EntityExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void canDeleteUserByID() {
        UUID userId = testUser.getId();
        given(userRepository.existsById(userId)).willReturn(true);
        ArgumentCaptor<UUID> argumentCaptor = ArgumentCaptor.forClass(UUID.class);

        userService.delete(userId);

        verify(pasteRepository, times(1)).deleteByUserId(argumentCaptor.capture());
        verify(userRepository, times(1)).deleteById(argumentCaptor.capture());
        verify(permissionRepository, times(1)).deleteAllByUserId(argumentCaptor.capture());
    }

    @Test
    void cantDeleteUserByIDWithExistentUser() {
        UUID userId = testUser.getId();
        given(userRepository.existsById(userId)).willReturn(false);
        ArgumentCaptor<UUID> argumentCaptor = ArgumentCaptor.forClass(UUID.class);

        assertThatThrownBy(() -> userService.delete(userId)).isInstanceOf(EntityNotFoundException.class);

        verify(pasteRepository, never()).deleteByUserId(argumentCaptor.capture());
        verify(userRepository, never()).deleteById(argumentCaptor.capture());
        verify(permissionRepository, never()).deleteAllByUserId(argumentCaptor.capture());
    }

    @Test
    void canGetCurrentUser() {
        TokenUser tokenUser = new TokenUser("test", "password", List.of(), new Token(
                UUID.randomUUID(),
                "test",
                List.of(),
                Instant.now(),
                Instant.now().plusSeconds(84000)
        ));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(tokenUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        given(userRepository.findUserByName("test")).willReturn(Optional.of(testUser));

        User currentUser = userService.getCurrentUser();

        assertThat(currentUser).isEqualTo(testUser);
    }

    @Test
    void cantGetCurrentUserWithNonExistentUser() {
        TokenUser tokenUser = new TokenUser("test", "password", List.of(), new Token(
                UUID.randomUUID(),
                "test",
                List.of(),
                Instant.now(),
                Instant.now().plusSeconds(84000)
        ));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(tokenUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        given(userRepository.findUserByName("test")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser()).isInstanceOf(EntityNotFoundException.class);

    }
}