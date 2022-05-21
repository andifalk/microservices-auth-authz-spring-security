package com.example.todo.service;

import com.example.todo.entity.UserEntity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CreateUser implements Serializable {

    private UUID identifier;

    @Size(min = 1, max = 30)
    private String firstName;

    @Size(min = 1, max = 30)
    private String lastName;

    @Size(min = 1, max = 30)
    private String username;

    @Email
    @Size(min = 1, max = 50)
    private String email;

    @Size(max = 100)
    protected String password;

    @NotNull
    private Set<String> roles = new HashSet<>();

    public CreateUser() {
    }

    public CreateUser(UUID identifier, String firstName, String lastName, String username, String email, String password, Set<String> roles) {
        this.identifier = identifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "CreateUser{" +
                "identifier=" + identifier +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                '}';
    }

    UserEntity toUserEntity() {
        return new UserEntity(
                this.getIdentifier(), this.getFirstName(), this.getLastName(),
                this.getUsername(), this.getEmail(), this.getPassword(), this.getRoles());
    }
}
