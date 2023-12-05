package ru.nechaev.pasteshare.service;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.nechaev.pasteshare.dto.PermissionRequest;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.Permission;
import ru.nechaev.pasteshare.entitity.User;

import java.util.UUID;

public interface PermissionService {

    Permission create(@Valid PermissionRequest permissionRequest);

    void delete(UUID uuid);

    Permission getById(UUID uuid);

    boolean confirm(Paste paste, User user);

}
