package com.example.todo.service;

import com.example.todo.entity.ToDoItemEntityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Transactional(readOnly = true)
public class ToDoService {

    @Value("${suggest.uri}")
    private String suggestServerUri;
    private final ToDoItemEntityRepository toDoItemEntityRepository;
    private final WebClient webClient;

    public ToDoService(ToDoItemEntityRepository toDoItemEntityRepository, WebClient webClient) {
        this.toDoItemEntityRepository = toDoItemEntityRepository;
        this.webClient = webClient;
    }

    public Mono<SuggestedToDoItem> suggestToDoItem() {
        return webClient
                .get()
                .uri(suggestServerUri + "/api/suggest")
                .retrieve()
                .onStatus(
                        s -> s.equals(HttpStatus.UNAUTHORIZED),
                        cr -> Mono.just(new BadCredentialsException("Not authenticated")))
                .onStatus(
                        HttpStatus::is4xxClientError,
                        cr -> Mono.just(new IllegalArgumentException(cr.statusCode().getReasonPhrase())))
                .onStatus(
                        HttpStatus::is5xxServerError,
                        cr -> Mono.just(new Exception(cr.statusCode().getReasonPhrase())))
                .bodyToMono(SuggestedToDoItem.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
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
