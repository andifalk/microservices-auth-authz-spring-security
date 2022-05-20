package com.example.todo.service;

import com.example.todo.entity.UserEntityRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserEntityRepository userEntityRepository;

    public UserService(UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    public Optional<User> findOneByUsername(String username) {
        return userEntityRepository.findOneByUsername(username).map(User::new);
    }

    public Optional<User> findOneByIdentifier(UUID identifier) {
        return userEntityRepository.findOneByIdentifier(identifier).map(User::new);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> findAll() {
        return userEntityRepository.findAll().stream().map(User::new).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public User create(User user) {
        return new User(userEntityRepository.save(user.toUserEntity()));
    }
}
