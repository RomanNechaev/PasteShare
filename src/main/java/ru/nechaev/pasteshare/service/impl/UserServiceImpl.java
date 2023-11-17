package ru.nechaev.pasteshare.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nechaev.pasteshare.entitity.Role;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.service.UserService;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @Override
    public User getCurrentUser() {
        User user = new User("test","test", LocalDateTime.now(), Role.USER);
        userRepository.save(user);
        return user;
    }
}
