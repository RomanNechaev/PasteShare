package ru.nechaev.pasteshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nechaev.pasteshare.entitity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
