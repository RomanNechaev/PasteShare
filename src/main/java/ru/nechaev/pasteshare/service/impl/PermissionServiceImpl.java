package ru.nechaev.pasteshare.service.impl;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.nechaev.pasteshare.dto.PermissionRequest;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.Permission;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.exception.db.EntityNotFoundException;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.service.PermissionService;
import ru.nechaev.pasteshare.util.Marker;

import java.util.UUID;

@Service
@AllArgsConstructor
@Validated
public class PermissionServiceImpl implements PermissionService {
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final PasteRepository pasteRepository;

    @Override
    @Validated({Marker.OnCreate.class})
    public Permission create(@Valid PermissionRequest permissionRequest) {
        User user = userRepository.findUserByName(permissionRequest.getUsername()).orElseThrow(
                () -> new EntityNotFoundException("User not found!")
        );
        Paste paste = pasteRepository.findPasteByContentLocation(permissionRequest.getPublicPasteId()).orElseThrow(
                () -> new EntityNotFoundException("Paste not found!")
        );
        Permission permission = new Permission(user, paste);
        permissionRepository.save(permission);
        return permission;
    }

    @Override
    public void delete(UUID uuid) {
        permissionRepository.deleteById(uuid);
    }

    @Override
    public Permission getById(UUID uuid) {
        return permissionRepository.findById(uuid).orElseThrow(
                () -> new EntityNotFoundException("Permission not found!")
        );
    }

    @Override
    public boolean confirm(Paste paste, User user) {
        return permissionRepository.existsByPasteAndUser(paste, user);
    }

}
