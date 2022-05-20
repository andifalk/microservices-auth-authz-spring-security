package com.example.todo.security;

import com.example.todo.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ToDoUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public ToDoUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findOneByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("No user found for [%s]", username))
        );
    }
}
