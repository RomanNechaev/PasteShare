package ru.nechaev.pasteshare.service;

import ru.nechaev.pasteshare.dto.PasteDto;
import ru.nechaev.pasteshare.dto.UserDto;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.Permission;
import ru.nechaev.pasteshare.entitity.User;

import java.util.UUID;

public interface PermissionService {
    Permission create(User user, Paste paste);

    Permission update(User user, Paste paste);

    void delete(UUID uuid);

    Permission getById(UUID uuid);
}
