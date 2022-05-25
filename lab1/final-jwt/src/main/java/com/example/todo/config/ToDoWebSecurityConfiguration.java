package com.example.todo.config;

import com.example.todo.security.AudienceValidator;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class ToDoWebSecurityConfiguration {

    /*
     * Do NOT route those requests through the security filter chain.
     * Use with care, not recommended.
     */
    @Bean
    @Order(1)
    public WebSecurityCustomizer ignoringCustomizer() {
        return (web) -> web.ignoring()
                .antMatchers(
                "/some-url/**"
                );
    }

    /*
     * Allow access to open H2 console for all (do not allow this for PROD, disable H2 console completely there !!)
     * Also disables CSRF and Clickjacking protection to make H2 console work (disable security features with care).
     */
    @Bean
    @Order(2)
    public SecurityFilterChain h2console(HttpSecurity http) throws Exception {
        http.requestMatcher(PathRequest.toH2Console())
                .authorizeRequests().anyRequest().permitAll()
                .and().csrf().disable().headers().frameOptions().disable();
        return http.build();
    }

    /*
     * Configure actuator endpoint security.
     * Allow access for everyone to health, info and prometheus.
     * All other actuator endpoints require ADMIn role.
     */
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

    /*
     * Security configuration for user and todos Rest API.
     */
    @Bean
    @Order(4)
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.mvcMatcher("/api/**")
                .authorizeRequests()
                .mvcMatchers("/api/users/me").hasAnyRole("USER", "ADMIN")
                .mvcMatchers("/api/users/**").hasRole("ADMIN")
                .anyRequest().hasAnyRole("USER", "ADMIN")
                .and()
                // only disable CSRF for demo purposes or when NOT using session cookies for auth
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .oauth2ResourceServer().jwt(withDefaults());
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
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    JwtDecoder jwtDecoder(OAuth2ResourceServerProperties oAuth2ResourceServerProperties) {
        NimbusJwtDecoder jwtDecoder =
                NimbusJwtDecoder.withJwkSetUri(oAuth2ResourceServerProperties.getJwt().getJwkSetUri())
                        .build();

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator();
        OAuth2TokenValidator<Jwt> withIssuer =
                JwtValidators.createDefaultWithIssuer(
                        "http://localhost:9000");
        OAuth2TokenValidator<Jwt> withAudience =
                new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

}
