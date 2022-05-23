package com.example.todo.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findOneByUsername(String username);

    Optional<UserEntity> findOneByIdentifier(UUID identifier);
}
