package ru.nechaev.pasteshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.Permission;
import ru.nechaev.pasteshare.entitity.User;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    boolean existsByPasteAndUser(Paste paste, User user);

    void deleteAllByPasteId(UUID pasteId);

    void deleteAllByUserId(UUID pasteId);
}
