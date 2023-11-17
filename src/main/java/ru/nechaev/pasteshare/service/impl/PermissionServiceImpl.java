package ru.nechaev.pasteshare.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.Permission;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.service.PermissionService;
import ru.nechaev.pasteshare.service.UserService;

import java.util.UUID;

@Service
@AllArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final UserService userService;
    private final PermissionRepository permissionRepository;

    @Override
    public Permission create(User user, Paste paste) {
        Permission permission = new Permission(user,paste);
        permissionRepository.save(permission);
        return permission;
    }


    @Override
    public Permission update(User user, Paste paste) {
        return null;
    }

    @Override
    public void delete(UUID uuid) {

    }

    @Override
    public Permission getById(UUID uuid) {
        return null;
    }
}
