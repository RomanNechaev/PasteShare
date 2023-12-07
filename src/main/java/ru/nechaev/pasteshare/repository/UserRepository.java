package ru.nechaev.pasteshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nechaev.pasteshare.entitity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByName(String name);

    boolean existsByName(String name);
}
