package com.example.todo.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class ToDoWebSecurityConfiguration {

    @Bean
    @Order(1)
    public WebSecurityCustomizer ignoringCustomizer() {
        return (web) -> web.ignoring()
                .antMatchers(
                "/some-url/**"
                );
    }

    @Bean
    @Order(2)
    public SecurityFilterChain h2console(HttpSecurity http) throws Exception {
        http.requestMatcher(PathRequest.toH2Console())
                .authorizeRequests().anyRequest().permitAll()
                .and().csrf().disable().headers().frameOptions().disable();
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain actuator(HttpSecurity http) throws Exception {
        http.requestMatcher(EndpointRequest.toAnyEndpoint())
                .authorizeRequests(
                        authorizeRequests ->
                                authorizeRequests
                                        .requestMatchers(EndpointRequest.to(
                                                HealthEndpoint.class,
                                                InfoEndpoint.class,
                                                PrometheusScrapeEndpoint.class))
                                        .permitAll()
                                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                )
                .httpBasic(withDefaults()).formLogin(withDefaults());
        return http.build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.mvcMatcher("/api/*")
                .authorizeRequests()
                .mvcMatchers("/api/users").hasRole("ADMIN")
                .anyRequest().hasAnyRole("USER", "ADMIN")
                .and().httpBasic(withDefaults()).formLogin(withDefaults());
        return http.build();
    }

    @Bean
    public SecurityFilterChain otherRequests(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .antMatchers("/v3/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
                .and().httpBasic(withDefaults()).formLogin(withDefaults());
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
