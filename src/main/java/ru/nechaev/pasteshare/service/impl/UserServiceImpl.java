package ru.nechaev.pasteshare.service.impl;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.nechaev.pasteshare.dto.AuthenticationRequest;
import ru.nechaev.pasteshare.entitity.Role;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.exception.db.EntityExistsException;
import ru.nechaev.pasteshare.exception.db.EntityNotFoundException;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.security.TokenUser;
import ru.nechaev.pasteshare.service.UserService;
import ru.nechaev.pasteshare.util.Marker;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Validated
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasteRepository pasteRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public User getCurrentUser() {
        TokenUser tokenUser = (TokenUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findUserByName(tokenUser.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));
    }

    @Override
    @Validated({Marker.OnCreate.class})
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void create(@Valid AuthenticationRequest request) {
        if (userRepository.existsByName(request.getUsername())) {
            throw new EntityExistsException("Username already used! Input another name");
        }
        User user = User.builder()
                .name(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .email(request.getEmail())
                .lastLogin(LocalDateTime.now())
                .build();
        userRepository.save(user);
    }
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        permissionRepository.deleteAllByUserId(userId);
        pasteRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
