package com.example.todo.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class SuggestToDoService {

    private final List<SuggestedToDoItem> suggestedToDoItemList = new ArrayList<>();
    private final Random random = new Random();

    @PostConstruct
    void initializeSuggestToDoItems() {
        suggestedToDoItemList.add(new SuggestedToDoItem("Watch a movie", "Go to cinema to watch a great movie"));
        suggestedToDoItemList.add(new SuggestedToDoItem("Weekend vacation", "Go on a weekend trip"));
        suggestedToDoItemList.add(new SuggestedToDoItem("Meet friends", "Go out and meet some old friends again"));
        suggestedToDoItemList.add(new SuggestedToDoItem("Learn a language", "Try learning a new language"));
        suggestedToDoItemList.add(new SuggestedToDoItem("Build something", "Build something by your hands"));
        suggestedToDoItemList.add(new SuggestedToDoItem("Redecorate apartment", "Redecorate the apartment with some new furniture"));
        suggestedToDoItemList.add(new SuggestedToDoItem("Cook dinner", "Cooking a delicious dinner"));
        suggestedToDoItemList.add(new SuggestedToDoItem("Visit museum", "Visit a new museum you have never been to"));
        suggestedToDoItemList.add(new SuggestedToDoItem("Explore new places", "Explore new places in your city you have never been to"));
        suggestedToDoItemList.add(new SuggestedToDoItem("Go swimming", "Go to a thermal bath or have some action in an adventure pool"));
    }

    public SuggestedToDoItem suggestToDoItem() {
        return suggestedToDoItemList.get(random.nextInt(9));
    }
}
