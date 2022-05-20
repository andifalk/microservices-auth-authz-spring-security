package com.example.library.client.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class WebSecurityConfiguration {

  @Bean
  public SecurityFilterChain otherRequests(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .mvcMatchers("/").permitAll()
            .anyRequest().authenticated()
            .and().logout().logoutSuccessUrl("/")
            .and().oauth2Client()
            .and()
            .oauth2Login().failureUrl("/error")
            .userInfoEndpoint()
            .userAuthoritiesMapper(userAuthoritiesMapper());
    return http.build();
  }

  private GrantedAuthoritiesMapper userAuthoritiesMapper() {
    return (authorities) -> {
      Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

      authorities.forEach(
          authority -> {
            if (authority instanceof OidcUserAuthority) {
              OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;

              OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

              List<SimpleGrantedAuthority> groupAuthorities =
                  userInfo.getClaimAsStringList("roles").stream()
                      .map(g -> new SimpleGrantedAuthority("ROLE_" + g.toUpperCase()))
                      .collect(Collectors.toList());
              mappedAuthorities.addAll(groupAuthorities);
            }
          });

      return mappedAuthorities;
    };
  }
}
