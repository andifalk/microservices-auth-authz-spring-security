package com.example.todo.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static javax.persistence.FetchType.EAGER;

@Entity
public class UserEntity extends AbstractPersistable<Long> {
    @Column(length=16)
    @NotNull
    private UUID identifier;

    @Size(min = 1, max = 30)
    private String firstName;

    @Size(min = 1, max = 30)
    private String lastName;

    @Size(min = 1, max = 30)
    private String username;

    @Size(min = 10, max = 100)
    private String password;

    @NotNull
    @ElementCollection(fetch = EAGER)
    private Set<String> roles = new HashSet<>();

    public UserEntity() {
    }

    public UserEntity(UUID identifier, String firstName, String lastName, String username, String password, Set<String> roles) {
        this.identifier = identifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public UUID getIdentifier() {
        return identifier;
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

    public String getPassword() {
        return password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "identifier=" + identifier +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                "} " + super.toString();
    }
}
