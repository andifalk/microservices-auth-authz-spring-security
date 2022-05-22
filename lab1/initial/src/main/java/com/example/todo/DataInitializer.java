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
    public static final String KENT_ID = "52a14872-ba6b-488f-aa4d-453b11f9ddce";
    public static final String PARKER_ID = "3a73ef49-c671-4d66-b6f2-7725ccde5c2b";

    private static final UUID WANE_TODO_1 = UUID.fromString("6cbe3232-6250-4ea9-9812-4788a6880e2f");
    private static final UUID WANE_TODO_2 = UUID.fromString("3ba17654-1879-4e8d-b255-87ff22253c88");
    private static final UUID WANE_TODO_3 = UUID.fromString("4e7e8c4c-2c82-42ca-a14b-afe584a908d3");

    private static final UUID KENT_TODO_1 = UUID.fromString("28613d35-4e0b-4b33-b977-9098a828a580");
    private static final UUID KENT_TODO_2 = UUID.fromString("e449b5d0-b287-4492-b797-fa2163449e80");
    private static final UUID KENT_TODO_3 = UUID.fromString("17458d5b-fcae-446e-a11c-e866d805e761");

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
                new UserEntity(UUID.fromString(WAYNE_ID), "Bruce", "Wayne",
                        "bwayne", "bruce.wayne@example.com",
                        passwordEncoder.encode("wayne"), Set.of("USER")),
                new UserEntity(UUID.fromString(KENT_ID), "Clark", "Kent",
                        "ckent", "clark.kent@example.com",
                        passwordEncoder.encode("kent"), Set.of("USER")),
                new UserEntity(UUID.fromString(PARKER_ID), "Peter", "Parker",
                        "pparker", "peter.parker@example.com",
                        passwordEncoder.encode("parker"), Set.of("USER", "ADMIN"))
        ).map(userEntityRepository::save).collect(Collectors.toList());

        LOG.info("Created users: {}", users);

        List<ToDoItemEntity> todosForWayne = userEntityRepository.findOneByUsername("bwayne").map(u -> Stream.of(
                new ToDoItemEntity(WANE_TODO_1, "Shopping", "Doing weekend shopping",
                        LocalDate.now().plusDays(2), UUID.fromString(WAYNE_ID)),
                new ToDoItemEntity(WANE_TODO_2, "Reading book", "Read the new book", null, UUID.fromString(WAYNE_ID)),
                new ToDoItemEntity(WANE_TODO_3, "Call mom", "Call my mom",
                        LocalDate.now().plusDays(5), UUID.fromString(WAYNE_ID))
        ).map(toDoItemEntityRepository::save).collect(Collectors.toList())).orElse(List.of());

        LOG.info("Created {} todos for user bwayne", todosForWayne.size());

        List<ToDoItemEntity> todosForKent = userEntityRepository.findOneByUsername("ckent").map(u -> Stream.of(
                new ToDoItemEntity(KENT_TODO_1, "Contact customer", "Make phone call with my customer",
                        LocalDate.now().plusDays(5), UUID.fromString(KENT_ID)),
                new ToDoItemEntity(KENT_TODO_2, "Plan holiday", "Plan my next long vacation",
                        LocalDate.now().plusMonths(1), UUID.fromString(KENT_ID)),
                new ToDoItemEntity(KENT_TODO_3, "Clean up", "Clean up my apartment",
                        LocalDate.now().plusDays(5), UUID.fromString(KENT_ID))
        ).map(toDoItemEntityRepository::save).collect(Collectors.toList())).orElse(List.of());

        LOG.info("Created {} todos for user ckent", todosForKent.size());
    }
}
