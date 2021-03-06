package com.example.todo.service;

import com.example.todo.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class User implements UserDetails {
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

    public User() {
    }

    public User(UUID identifier, String firstName, String lastName, String username, String email, String password, Set<String> roles) {
        this.identifier = identifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public User(UserEntity userEntity) {
        this.identifier = userEntity.getIdentifier();
        this.firstName = userEntity.getFirstName();
        this.lastName = userEntity.getLastName();
        this.username = userEntity.getUsername();
        this.email = userEntity.getEmail();
        this.password = userEntity.getPassword();
        this.roles = userEntity.getRoles();
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

    @Override
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(
                getRoles().stream().map(r -> "ROLE_" + r).collect(Collectors.joining(","))
        );
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public Set<String> getRoles() {
        return roles;
    }

    @JsonIgnore
    public boolean isAdmin() {
        return getRoles().contains("ADMIN");
    }

    @Override
    public String toString() {
        return "User{" +
                "identifier=" + identifier +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='******'" +
                ", admin='" + isAdmin() + '\'' +
                ", roles=" + roles + '\'' +
                ", authz=" + getAuthorities() +
                "} " + super.toString();
    }

    UserEntity toUserEntity() {
        return new UserEntity(
                this.identifier, this.firstName, this.lastName,
                this.username, this.email, this.password, this.roles);
    }
}
