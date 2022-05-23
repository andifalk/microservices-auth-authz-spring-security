package com.example.todo.service;

import com.example.todo.entity.ToDoItemEntityRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
@Transactional(readOnly = true)
public class ToDoService {

    private final ToDoItemEntityRepository toDoItemEntityRepository;

    public ToDoService(ToDoItemEntityRepository toDoItemEntityRepository) {
        this.toDoItemEntityRepository = toDoItemEntityRepository;
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public List<ToDoItem> findAll() {
        return toDoItemEntityRepository.findAll()
                .stream().map(ToDoItem::new).collect(Collectors.toList());
    }

    public List<ToDoItem> findAllForUser(UUID userIdentifier, UUID authenticatedUserIdentification) {
        if (!authenticatedUserIdentification.equals(userIdentifier)) {
            throw new AccessDeniedException("Current user is not allowed to access todo items for given user id");
        }
        return toDoItemEntityRepository.findAllByUserIdentifier(userIdentifier)
                .stream().map(ToDoItem::new).collect(Collectors.toList());
    }

    public Optional<ToDoItem> findToDoItemForUser(UUID toDoItemIdentifier, UUID authenticatedUserIdentification) {
        return toDoItemEntityRepository.findOneByIdentifierAndUserIdentifier(
                toDoItemIdentifier, authenticatedUserIdentification).map(ToDoItem::new);
    }

    @Transactional
    public ToDoItem create(ToDoItem toDoItem) {
        if (toDoItem.getIdentifier() == null) {
            toDoItem.setIdentifier(UUID.randomUUID());
        }
        return new ToDoItem(toDoItemEntityRepository.save(toDoItem.toTodoItemEntity()));
    }
}
