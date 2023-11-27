package ru.nechaev.pasteshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nechaev.pasteshare.entitity.DeactivatedToken;

import java.util.UUID;

public interface DeactivatedTokenRepository extends JpaRepository<DeactivatedToken, UUID> {

}
