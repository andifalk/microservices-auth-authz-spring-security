package com.example.todo;

import com.example.todo.entity.ToDoItemEntity;
import com.example.todo.entity.ToDoItemEntityRepository;
import com.example.todo.entity.UserEntity;
import com.example.todo.entity.UserEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DataInitializer implements ApplicationRunner {

    public static final String WAYNE_ID = "c52bf7db-db55-4f89-ac53-82b40e8c57c2";
    public static final String BANNER_ID = "52a14872-ba6b-488f-aa4d-453b11f9ddce";
    public static final String PARKER_ID = "3a73ef49-c671-4d66-b6f2-7725ccde5c2b";

    private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

    private final UserEntityRepository userEntityRepository;
    private final ToDoItemEntityRepository toDoItemEntityRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserEntityRepository userEntityRepository, ToDoItemEntityRepository toDoItemEntityRepository, PasswordEncoder passwordEncoder) {
        this.userEntityRepository = userEntityRepository;
        this.toDoItemEntityRepository = toDoItemEntityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) {

        List<UserEntity> users = Stream.of(
                new UserEntity(UUID.fromString(WAYNE_ID), "Bruce", "Wayne", "bwayne", passwordEncoder.encode("wayne"), Set.of("USER")),
                new UserEntity(UUID.fromString(BANNER_ID), "Bruce", "Banner", "bbanner", passwordEncoder.encode("banner"), Set.of("USER")),
                new UserEntity(UUID.fromString(PARKER_ID), "Peter", "Parker", "pparker", passwordEncoder.encode("parker"), Set.of("USER", "ADMIN"))
        ).map(userEntityRepository::save).collect(Collectors.toList());

        LOG.info("Created {} users", users.size());

        List<ToDoItemEntity> todos = userEntityRepository.findOneByUsername("bwayne").map(u -> Stream.of(
                new ToDoItemEntity(UUID.randomUUID(), "Shopping", "Doing weekend shopping", LocalDate.now().plusDays(2), u),
                new ToDoItemEntity(UUID.randomUUID(), "Reading book", "Read the new book", null, u),
                new ToDoItemEntity(UUID.randomUUID(), "Call mom", "Call my mom", LocalDate.now().plusDays(5), u)
        ).map(toDoItemEntityRepository::save).collect(Collectors.toList())).orElse(List.of());

        LOG.info("Created {} todos for user bwayne", todos.size());

    }
}
