package com.example.todo.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ToDoItemEntityRepository extends JpaRepository<ToDoItemEntity, Long> {

    @Query("select ti from ToDoItemEntity ti where ti.userEntity.identifier = :userEntityIdentifier")
    List<ToDoItemEntity> findAllByUserEntityIdentifier(@Param("userEntityIdentifier") UUID userEntityIdentifier);

    Optional<ToDoItemEntity> findOneByIdentifierAndUserEntityIdentifier(UUID toDoItemIdentifier, UUID identifier);
}
