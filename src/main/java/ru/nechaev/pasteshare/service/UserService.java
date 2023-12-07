package ru.nechaev.pasteshare.service;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.nechaev.pasteshare.dto.AuthenticationRequest;
import ru.nechaev.pasteshare.entitity.User;

import java.util.UUID;

public interface UserService {
    User getCurrentUser();

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void create(@Valid AuthenticationRequest authenticationRequest);

    @Transactional()
    void delete(UUID userId);
}
