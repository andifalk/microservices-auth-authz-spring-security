# Lab 1: Creating an OAuth 2.0/OIDC compliant Resource Server (with JWT)

In this first part we extend an existing Microservice to an OAuth 2.0 and OpenID Connect 1.0 compliant Resource Server.

See [Spring Security 5 Resource Server reference doc](https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#oauth2resourceserver)
for all details on how to build and configure a resource server.

__Please check out the [complete documentation](../application-architecture) for the sample application before
starting with the first hands-on lab (especially the server side parts)__.

## Lab Contents

* [Learning Targets](#learning-targets)
* [Folder Contents](#folder-contents)
* [Tutorial: Implement a resource server with custom user/authorities mapping](#start-the-lab)
    * [Explore the initial server application](#explore-the-initial-application)
    * [Step 1: Configure as resource server](#step-1-basic-configuration)
    * [Step 2: Run and test basic resource server](#step-2-run-and-test-basic-resource-server)
    * [Step 3: Implement a custom JWT authorities mapper](#step-3-custom-jwt-authorities-mapper)
    * [(Optional) Step 4: Implement a custom JWT converter](#optional-step-4-custom-jwt-converter)
    * [(Optional) Step 5: An additional JWT validator for 'audience' claim](#optional-step-5-jwt-validation-for-the-audience-claim)
    * [Opaque Tokens](#opaque-tokens)

## Learning Targets

In this lab we will build an OAuth2/OIDC compliant resource server and look at various mapping options for converting the JWT contents into spring security native objects for authentication and authorization.

![Resource_Server](images/resource_server.png)

We will use Spring Authorization Server as identity provider.  
Please again make sure you have set up Spring Authorization Server as described in [setup](../setup/README.md)

In lab 1 you will learn how to:

1. Implement a basic resource server requiring bearer token authentication using JSON web tokens (JWT)
2. Customize the resource server with __user & authorities mapping__
3. Implement additional recommended validation of the _audience_ claim of the access token
4. Have a small look into alternative token type: Opaque tokens

## Folder Contents

In the lab 1 folder you find 2 applications:

* __initial__: This is the application we will use as starting point for this lab
* __final-automatic__:  This is the completed application for this lab using automatic standard authorization mapping of scopes to JWT principal 
* __final-jwt__: This is the completed application for this lab using custom authorization mapping with JWT principal
* __final-user__: This is the completed application for this lab using full custom user mapping with _User_ class as principal

## Start the Lab

Now, let's start with this lab.

### Explore the initial application

Please navigate your Java IDE to the __lab1/initial__ project and at first explore this project a bit.  
Then start the application by running the class _com.example.todo.ToDoApplicationLab1Initial_ inside your IDE
or by issuing a `gradlew bootRun` command.

As already described in the [application architecture](../application-architecture) section the initial application
is secured using basic authentication.

There are two target user roles for this application:

* USER: Standard user who can list and add todo items
* ADMIN: An administrator user who can list, add or remove users and can see all todo items (of all users)

| Username | Email                    | Identifier                            | Password  | Role   |
| ---------| ------------------------ |---------------------------------------|-----------|--------|
| bwayne   | bruce.wayne@example.com  | c52bf7db-db55-4f89-ac53-82b40e8c57c2  | wayne     | USER   |
| ckent    | clark.kent@example.com   | 52a14872-ba6b-488f-aa4d-453b11f9ddce  | kent      | USER   |
| pparker  | peter.parker@example.com | 3a73ef49-c671-4d66-b6f2-7725ccde5c2b  | parker    | ADMIN  |

To test if the application works as expected, either

* open a web browser and navigate to [localhost:9090/api/todos](http://localhost:9090/api/todos)
  and use _bwayne_ and _wayne_ as login credentials
* or use a command line like curl or httpie or postman (if you like a UI)

Httpie:
```shell
http localhost:9090/api/todos user==c52bf7db-db55-4f89-ac53-82b40e8c57c2 --auth 'bwayne:wayne'
``` 

Curl:
```shell
curl "http://localhost:9090/api/todos?user=c52bf7db-db55-4f89-ac53-82b40e8c57c2" -u bwayne:wayne
```

If this succeeds you should see a list of ToDo items in JSON format.

Try the same request without specifying any user:

```shell
http localhost:9090/api/todos
``` 

Then you should see the following response:

```http
HTTP/1.1 401 
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
...
WWW-Authenticate: Basic realm="Realm"
``` 

Also, try to request the list of users with same user credentials of 'bruce.wayne@example.com / wayne'.

Httpie:

```shell
http localhost:9090/api/users --auth 'bwayne:wayne'
``` 

Curl:

```shell
curl -i http://localhost:9090/api/users -u bwayne:wayne
```

__Question:__ What response would you expect here?

To answer this question have a look again at the user roles and what are the permissions associated with these roles.
You might try again to get the list of users this way (with Peter Parker):

Httpie:

```shell
http localhost:9090/api/users --auth 'pparker:parker'
``` 

Curl:

```shell
curl http://localhost:9090/api/users -u pparker:parker
``` 

This time it should work, and you should see the list of users.

<hr>

### Step 1: Basic Configuration

In the first step we will perform tha basic steps to transform the server application into
a basic OAuth2 & OIDC compliant resource server.

#### Add Gradle Dependencies

To change this application into a resource server you have to make changes in the dependencies
of the gradle build file _build.gradle_:

Remove this dependency:
```groovy
implementation('org.springframework.boot:spring-boot-starter-security')
```
and add this dependency instead:
```groovy
implementation('org.springframework.boot:spring-boot-starter-oauth2-resource-server')
```

__Note__: If you still get compilation errors after replacing dependencies please trigger a gradle update
(check how this is done in your IDE, e.g. in Eclipse there is an option in project context menu, in IntelliJ
click the refresh toolbar button in the gradle tool window).

#### Configure The Resource Server

Spring security 5 uses the
[OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig) specification
to completely configure the resource server to use our Spring Authorization Server instance.

__Make sure Spring Authorization Server has been started as described in the [setup](../setup/README.md) section.__

Navigate your web browser to the url [localhost:9000/.well-known/openid-configuration](http://localhost:9000/.well-known/openid-configuration).  
Then you should see the public discovery information that Spring Authorization Server provides
(like the following).

```json
{
  "authorization_endpoint": "http://localhost:9000/oauth2/authorize",
  "grant_types_supported": [
    "authorization_code",
    "client_credentials",
    "refresh_token"
  ],
  "id_token_signing_alg_values_supported": [
    "RS256"
  ],
  "issuer": "http://localhost:9000",
  "jwks_uri": "http://localhost:9000/oauth2/jwks",
  "response_types_supported": [
    "code"
  ],
  "scopes_supported": [
    "openid"
  ],
  "subject_types_supported": [
    "public"
  ],
  "token_endpoint": "http://localhost:9000/oauth2/token",
  "token_endpoint_auth_methods_supported": [
    "client_secret_basic",
    "client_secret_post",
    "client_secret_jwt",
    "private_key_jwt"
  ],
  "userinfo_endpoint": "http://localhost:9000/userinfo"
} 
```

For configuring a resource server the important entries are _issuer-uri_ and _jwk-set-uri_.
For a resource server only the correct validation of a JWT token is significant, so it only needs to know where to load
the public key from to validate the token signature.
In case of the Spring Authorization Server it is the entry for ```"jwks_uri": "http://localhost:9000/oauth2/jwks"```.

If you specify the _spring.security.oauth2.resourceserver.jwt.issuer-uri_ instead then when starting the server application 
it reads the _jwk-set-uri_ from the provided openid configuration. If you do not want to check this on application start just use the _jwk-set-uri_ property.

Spring Security 5 automatically configures a resource server by specifying the _jwk-set_ uri value
as part of the predefined spring property _spring.security.oauth2.resourceserver.jwt.set-uri_

To perform this step, open _application.yml__ and add the jwk set uri property to the end of the _spring_ entry.
After adding this it should look like this:

```yaml
spring:
  application:
    name: ToDoApp
  datasource:
    embedded-database-connection: h2
    hikari:
      jdbc-url: jdbc:h2:mem:todo
  jpa:
    open-in-view: false
    generate-ddl: on
    hibernate:
      ddl-auto: create-drop
  jackson:
    default-property-inclusion: non_null
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:9000/oauth2/jwks
```

**Hint: An error you get very often with files in yaml format is that the indents are not correct.
This can lead to unexpected errors later when you try to run all this stuff.**

With this configuration in place we have already a working resource server
that can handle JWT access tokens transmitted via http bearer token header.
Spring Security then validates by default:

* the JWT signature against the queried public key(s) from specified _jwks_url_
* the _issuer_ claim of the JWT
* that the JWT is not expired

Usually this configuration would be sufficient to configure a resource server (by autoconfiguring all settings using spring boot).
As there is already a security configuration for basic authentication in place (_com.example.toto.config.ToDoWebSecurityConfiguration_),
this disables the spring boot autoconfiguration.

__Please note__: The security configuration already uses the new approach of configuring beans of type _SecurityFilterChain_ instead of extending _WebSecurityConfigurerAdapter_ class.

So we have to change the existing security configuration to enable token based authentication instead of basic authentication.
We also want to make sure, our resource server is working with stateless token authentication, so we have to configure stateless
sessions (i.e. prevent _JSESSION_ cookies and CSRF attack surface).

Open the class _com.example.todo.config.ToDoWebSecurityConfiguration_ and change the
existing configuration like this (only the security configuration block for the API):

```java
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class ToDoWebSecurityConfiguration {

  // ...  

  /*
   * Security configuration for user and todos Rest API.
   */
  @Bean
  @Order(4)
  public SecurityFilterChain api(HttpSecurity http) throws Exception {
    http.mvcMatcher("/api/**")
            .authorizeRequests()
            .mvcMatchers("/api/users/me").hasAnyAuthority("SCOPE_USER", "SCOPE_ADMIN")
            .mvcMatchers("/api/users/**").hasAuthority("SCOPE_ADMIN")
            .anyRequest().hasAnyAuthority("SCOPE_USER", "SCOPE_ADMIN")
            .and()
            // only disable CSRF for demo purposes or when NOT using session cookies for auth
            .csrf().disable() // (2)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 1
            .and()
            .oauth2ResourceServer().jwt(withDefaults()); // (3)
    return http.build();
  }

  // ...
}
```

This configuration above:
* configures stateless sessions (i.e. no _JSESSION_ cookies anymore) (1)
* disables CSRF protection (with stateless sessions, i.e. without session cookies this kind of attack does not work anymore, 
so we do not need this anymore). As a benefit this also enables us to even make post requests on the command line. (2)
* protects any request (i.e. requires authentication for any endpoint)
* enables this application to switch authentication to OAuth2/OIDC resource server with expecting access tokens in JWT format 
(as of spring security 5.2 you may also configure this to use opaque - aka reference - tokens instead) (3)

Also, the _PasswordEncoder_ bean defined in this configuration is not required anymore as we now stopped storing passwords
in our resource server, so you can also delete that bean definition. Please make sure that you also remove the _PasswordEncoder_ 
from the _com.example.todo.DataInitializer_ class, just replace the encoder calls here with the default string _"n/a"_ as the password 
is not relevant anymore.
So instead of ```passwordEncoder.encode("wayne")``` just replace it with ```"n/a"``` or remove the password attribute completely from
the _ToDoItemEntity_ and _ToDoItem_ and remove it as constructor parameter for _User_ in the _DataInitializer_ class.

### Step 2: Change the Authenticated Principal

In the following table you can see the corresponding spring security core classes like _Authentication_ and _Principle_ 
that are used for the different authentication types.

| Authentication Type   | AuthenticationToken                 | Principal (@AuthenticationPrincipal)                                  |
|-----------------------|-------------------------------------|-----------------------------------------------------------------------|
| Basic Authentication  | UsernamePasswordAuthenticationToken | com.example.todo.service.User (UserDetails)                           |
| Bearer Token (JWT)    | JwtAuthenticationToken              | org.springframework.security.oauth2.jwt.Jwt                           |
| Bearer Token (Opaque) | BearerTokenAuthentication           | org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal |

After changing the authentication mechanism to JWT bearer tokens it is also required to replace the existing
_User_ principle annotated with _@AuthenticationPrincipal_ in the rest controllers _ToDoRestController_ and _UserRestController_.

Please replace all references to _@AuthenticationPrincipal User authenticatedUser_ in the _ToDoRestController_ with
_@AuthenticationPrincipal Jwt authenticatedUser_. Then do the same for the other rest controller _UserRestController_ class.

ToDoRestController:

```
package com.example.todo.api;

import com.example.todo.DataInitializer;
import com.example.todo.service.ToDoItem;
import com.example.todo.service.ToDoService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
@Validated
@OpenAPIDefinition(tags = @Tag(name = "todo"), info = @Info(title = "ToDo", description = "API for ToDo Items", version = "1"), security = {@SecurityRequirement(name = "bearer")})
public class ToDoRestController {

    private final ToDoService toDoService;

    public ToDoRestController(ToDoService toDoService) {
        this.toDoService = toDoService;
    }

    @Operation(tags = "todo", summary = "ToDo API", description = "Finds all ToDo items for given user identifier", parameters = @Parameter(name = "user", example = DataInitializer.WAYNE_ID))
    @GetMapping
    public List<ToDoItem> findAllForUser(@RequestParam(name = "user") UUID userIdentifier, @AuthenticationPrincipal Jwt authenticatedUser) {
        if (authenticatedUser.getClaimAsStringList("roles").contains("ADMIN")) {
            return toDoService.findAll();
        } else {
            return toDoService.findAllForUser(userIdentifier, UUID.fromString(authenticatedUser.getSubject()));
        }
    }

    @Operation(tags = "todo", summary = "ToDo API", description = "Finds one ToDo item for given todo item identifier")
    @GetMapping("/{todoItemIdentifier}")
    public ResponseEntity<ToDoItem> findOneForUser(
            @PathVariable("todoItemIdentifier") UUID todoItemIdentifier,
            @AuthenticationPrincipal Jwt authenticatedUser) {
        return toDoService.findToDoItemForUser(todoItemIdentifier, UUID.fromString(authenticatedUser.getSubject()))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(tags = "todo", summary = "ToDo API", description = "Creates a new ToDo item for current user")
    @PostMapping
    public ToDoItem create(@RequestBody @Valid ToDoItem toDoItem, @AuthenticationPrincipal Jwt authenticatedUser) {
        toDoItem.setUserIdentifier(UUID.fromString(authenticatedUser.getSubject()));
        return toDoService.create(toDoItem);
    }
}
```

UserRestController:

```
package com.example.todo.api;

import com.example.todo.service.CreateUser;
import com.example.todo.service.User;
import com.example.todo.service.UserService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/users")
@Validated
@OpenAPIDefinition(tags = @Tag(name = "user"), info = @Info(title = "User", description = "API for Users", version = "1"), security = {@SecurityRequirement(name = "bearer")})
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @Operation(tags = "user", summary = "User API", description = "Finds all registered users")
    @GetMapping
    public List<User> allUsers() {
        return userService.findAll();
    }

    @Operation(tags = "user", summary = "User API", description = "Finds user specified by user identifier")
    @GetMapping("/{userIdentifier}")
    public ResponseEntity<User> findUser(@PathVariable UUID userIdentifier) {
        return userService.findOneByIdentifier(userIdentifier).map(
                ResponseEntity::ok
        ).orElse(ResponseEntity.notFound().build());
    }

    @Operation(tags = "user", summary = "User API", description = "Retrieves the currently authenticated user")
    @GetMapping("/me")
    public String getAuthenticatedUser(@AuthenticationPrincipal Jwt authenticatedUser) {
        return String.format("%s %s", authenticatedUser.getClaimAsString("given_name"), authenticatedUser.getClaimAsString("family_name"));
    }

    @Operation(tags = "user", summary = "User API", description = "Creates a new user")
    @PostMapping
    @ResponseStatus(CREATED)
    public User createUser(@RequestBody @Valid CreateUser user) {
        return userService.create(user);
    }

}
```

### Step 3: Run and test basic resource server

Now it should be possible to re-start the reconfigured application _com.example.todo.ToDoApplicationLab1Initial_.
Or just use the `gradlew bootRun` command.

Now, the requests you have tried when starting this lab using basic authentication won't work anymore
as we now require bearer tokens in JWT format to authenticate at our resource server.

Just to memorize: With basic authentication when omitting the credentials you got this response:

```http
HTTP/1.1 401 
WWW-Authenticate: Basic realm="Realm"
```

Now try again with this request:

httpie:

```shell
http localhost:9090/api/users
``` 

curl:

```shell
curl -i http://localhost:9090/api/users
```

You now will get this answer:

```http
HTTP/1.1 401 
...
WWW-Authenticate: Bearer
```

So what is needed here is a bearer token to authenticate, in our case in fact it is a JSON Web Token (JWT).
First we need to get such token, and then we can try to call this API again.

For convenience, in the past we have been able to use the _resource owner password grant_ to directly obtain an access token
from Spring Authorization Server via the command line by specifying our credentials as part of the request.

__You may argue now: "This is just like doing basic authentication??"__

Yes, you're right. This grant flow completely bypasses the base concepts of OAuth 2. This is why in OAuth 2.1 this grant flow 
is deprecated and will be removed from the standard.
And because of this, the Spring Authorization Server does and will not support the password grant flow.

So, how to get a token now?
You basically have two options as part of this workshop:

1. If you have Postman installed (or just install it now from [https://www.postman.com/downloads](https://www.postman.com/downloads/)) you
can use the authorization code flow (+ PKCE) as built in functionality (even in the free edition)
2. You can use the provided test client (see [lab3](../lab3)) to get a token. Just follow instruction in the [readme](../lab3/README.md)

For both options please log in as _bwayne/wayne_ to get a token authorized to perform the request we will execute below.

After you have received a token by either way above you can make the same request for a list of todos (like in the beginning of this lab). This time we have to present the access token as part of the _Authorization_ header of type _Bearer_ like this:

httpie:

```shell
http localhost:9090/api/todos user==c52bf7db-db55-4f89-ac53-82b40e8c57c2 --auth-type=bearer --auth=[access_token]
```

curl:

```shell
curl -H 'Authorization: Bearer [access_token]' -v "localhost:9090/api/todos user==c52bf7db-db55-4f89-ac53-82b40e8c57c2"
```

You have to replace _[access_token]_ with the one you have obtained in previous request.  
Now the user authenticates by the given token, but even with using the correct user Bruce Wayne you get a _"403"_ response (_Forbidden_).

This is due to the fact that Spring Security 5 automatically maps all scopes that are part of the
JWT token to the corresponding authorities.
For example the scopes _"openid profile"_ will be mapped automatically to the authorities _SCOPE_openid_ and _SCOPE_profile_. And for sure
that does not map to our requirement of authorities to include _ROLE_USER_ and/or _ROLE_ADMIN_. 

Navigate your web browser to [jwt.io](https://jwt.io) and paste your access token into the
_Encoded_ text field.

![JWT IO Decoded](../docs/images/jwt_io_decoded.png)

If you scroll down a bit on the right hand side then you will see the following block:

```json
{
  "sub": "c52bf7db-db55-4f89-ac53-82b40e8c57c2",
  "aud": "demo-client-pkce",
  "nbf": 1653471685,
  "scope": [
    "openid",
    "USER"
  ],
  "roles": [
    "USER"
  ],
  "iss": "http://localhost:9000",
  "exp": 1653471985,
  "given_name": "Bruce",
  "iat": 1653471685,
  "family_name": "Wayne",
  "email": "bruce.wayne@example.com"
}
```

As you can see our user has the scopes _openid_, and _USER_.
Spring Security maps these scopes to the Spring Security authorities _SCOPE_openid_ and _SCOPE_USER_ by default.

If you have a look inside the _com.example.todo.service.ToDoService_ class
you will notice that this has the following authorization checks on method security layer:

```java
package com.example.todo.service;

import com.example.todo.entity.ToDoItemEntityRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Transactional(readOnly = true)
public class ToDoService {

    //...

    @PreAuthorize("hasRole('ADMIN')")
    public List<ToDoItem> findAll() {
        return toDoItemEntityRepository.findAll()
                .stream().map(ToDoItem::new).collect(Collectors.toList());
    }

    // ...
}
``` 

The required authorities _ROLE_ADMIN_ and _ROLE_USER_ do not match the automatically mapped authorities of _SCOPE_xxx_.
The same problem applies to our web security configuration on the web layer:

```java
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class ToDoWebSecurityConfiguration {

  // ...
  
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

  // ...
}
```

In summary this leads to authorization errors in the log like these:

```syslog
Sending JwtAuthenticationToken [Principal=org.springframework.security.oauth2.jwt.Jwt@9dd3cf4a, Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=127.0.0.1, SessionId=null], Granted Authorities=[SCOPE_openid, SCOPE_profile, SCOPE_email]] to access denied handler since access is denied
```

To fix this you basically have 3 options:

1. Adapt our configuration in _com.example.todo.config.ToDoWebSecurityConfiguration_ and the _@PreAuthorize_ annotations with the _SCOPE_xx_ authorities
in the service classes. You can imagine what effort this would be especially for big applications using lots of authorizations. 
So usually only makes sense for small applications with only a few authorities to be replaced.
2. Implement a simple mapping that reads the authorities from the intended _roles_ claim and not from the scopes and also uses
   the _ROLE__ prefix again instead of _SCOPE__.
3. Implement a full conversion that maps all contents (like firstname and lastname in addition to the roles and authorities) 
of the JWT to our _User_ object

The following table also shows details for each option:

| Option  | Approach                   | Principal Object | Authorities Claim | Authorities                  |
|---------|----------------------------|------------------|-------------------|------------------------------|
| 1       | Automatic mapping          | JWT              | scope             | SCOPE_USER, SCOPE_ADMIN, ... |
| 2       | Custom authorities mapping | JWT              | roles             | ROLE_USER, ROLE_ADMIN, ...   |
| 3       | Full JWT conversion        | User             | roles             | ROLE_USER, ROLE_ADMIN, ...   |

As part of the workshop we will follow along option number 2. Option 3 is an optional step if there still is time left.
Before we head to the next step, here you see the adapted security configuration for option 1:

```
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class ToDoWebSecurityConfiguration {

    // ...

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
                                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority("SCOPE_ADMIN")
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
                .mvcMatchers("/api/users/me").hasAnyAuthority("SCOPE_USER", "SCOPE_ADMIN")
                .mvcMatchers("/api/users/**").hasAuthority("SCOPE_ADMIN")
                .anyRequest().hasAnyAuthority("SCOPE_USER", "SCOPE_ADMIN")
                .and()
                // only disable CSRF for demo purposes or when NOT using session cookies for auth
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .oauth2ResourceServer().jwt(withDefaults());
        return http.build();
    }

    /...
}
```

You can find this kind of _automatic' standard solution in the corresponding final reference application located in the [lab1/final-automatic](final-automatic) folder.
In the following sections we will look into the other two mapping options.
Let's start with option 2.

<hr>

### Step 4: Custom JWT authorities mapper

In this step we would like to add a custom mapping for authorizations, so we can get rid of the automatic _SCOPE_xx_ authorities back again to
ROLE_xx authorities.
To achieve this, simply add the following new bean to our web security configuration class:

```
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class ToDoWebSecurityConfiguration {

    // ...

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); // 1
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // 2

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
```

The new bean above

* reads the spring security authorities from the _roles_ claim of the incoming JWT (1)
* maps it to spring security authorities using the _ROLE__ prefix (2)

You can find this kind of solution in the corresponding final reference solution located in the [lab1/final-jwt](final-jwt) folder.

### (Optional) Step 5: Custom JWT converter

The third option is the one with the most effort. Here we completely map token claim values int a _User_ object.

To implement a full custom mapping for a JWT access token Spring Security requires us to implement
the interface _Converter<Jwt, AbstractAuthenticationToken>_.

```java
package org.springframework.core.convert.converter;

import org.springframework.lang.Nullable;

public interface Converter<S, T> {

	/**
	 * Convert the source object of type {@code S} to target type {@code T}.
	 * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
	 * @return the converted object, which must be an instance of {@code T} (potentially {@code null})
	 * @throws IllegalArgumentException if the source cannot be converted to the desired target type
	 */
	@Nullable
	T convert(S source);

}
```

In general, you have again two choices here:

* Map the JWT token user data to the corresponding _User_ and read the
  authorization data from the token and map it to Spring Security authorities
* Identify the locally stored _User_ by the _subject_ claim in the token and 
map the corresponding _User_ from the JWT token user data but map the
  persistent roles of the _User_ to Spring Security authorities.

In this workshop we will use the second option...

* ...read the authorization data from the _scope_ claim inside the JWT token
* ...map to our local _LibraryUser_ by reusing the _LibraryUserDetailsService_ to search
  for a user having the same email as the _email_ claim inside the JWT token

To achieve this please go ahead and create a new class _JwtUserAuthenticationConverter_
in package _com.example.todo.security_ with the following contents:

```java
package com.example.library.server.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/** JWT converter that takes the roles from 'groups' claim of JWT token. */
@SuppressWarnings("unused")
public class JwtUserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final UserService userService;

  public JwtUserAuthenticationConverter(UserService userService) {
    this.userService = userService;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    UUID userIdentifier = UUID.fromString(jwt.getSubject());
    return userService.findOneByIdentifier(userIdentifier).map(u ->
            new UsernamePasswordAuthenticationToken(u, jwt.getTokenValue(), u.getAuthorities())
    ).orElse(null);
  }
}
```

This converter maps the JWT token information to a _User_ by associating
these via the _email_ claim. It reads the authorities from _groups_ claim in the JWT token and maps these
to the corresponding authorities.  
This way we can map these groups again to our original authorities, e.g. _ROLE_ADMIN_.

No open again the class _com.example.todo.config.ToDoWebSecurityConfiguration_ and add this new JWT
converter to the JWT configuration:

```java
package com.example.todo.config;

import com.example.todo.security.JwtUserAuthenticationConverter;
import com.example.todo.service.UserService;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class ToDoWebSecurityConfiguration {

  //...
  
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
            .oauth2ResourceServer().jwt().jwtAuthenticationConverter(customJwtUserAuthenticationConverter()); //1
    return http.build();
  }

  // ...

  public Converter<Jwt, AbstractAuthenticationToken> customJwtUserAuthenticationConverter() {
    return new JwtUserAuthenticationConverter(userService);
  }

}
```

<hr>

### (Optional) Step 6: JWT validation for the 'audience' claim

Implementing an additional custom token validator is quite easy, you just have to implement the
provided interface _OAuth2TokenValidator_ and add your custom validator on top of the mandatory validators..

In this step we will also validate the audience (aud) claim of the access token.

According to the current [draft for OAuth 2.1](https://www.ietf.org/archive/id/draft-ietf-oauth-v2-1-05.html) the audience claim should be used to restrict token usage for specific resource servers:

<blockquote cite=https://www.ietf.org/archive/id/draft-ietf-oauth-v2-1-05.html#name-access-token-privilege-rest">
The privileges associated with an access token SHOULD be restricted to the minimum required for the particular application or use case. 
[...] In particular, access tokens SHOULD be restricted to certain resource servers (audience restriction)
</blockquote>

Recently the OAuth working group published the new RFC 9068 standard called [JSON Web Token (JWT) Profile for OAuth 2.0 Access Tokens](https://datatracker.ietf.org/doc/html/rfc9068) that also describes the same audience claim as mandatory for access tokens.

So we should also validate that our resource server only successfully authenticates those requests bearing access tokens
containing the expected value of _http://localhost:9090/api/todos_ in the _audience_ claim.

So let's create a new class _AudienceValidator_ in package _com.example.todo.security_
with the following contents:

```java
package com.example.todo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AudienceValidator.class);

  private final OAuth2Error error =
          new OAuth2Error("invalid_token", "The required audience 'http://localhost:9090/api/todos' is missing", null);

  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    if (jwt.getAudience().contains("http://localhost:9090/api/todos")) {
      LOGGER.info("Successfully validate audience");
      return OAuth2TokenValidatorResult.success();
    } else {
      LOGGER.warn(error.getDescription());
      return OAuth2TokenValidatorResult.failure(error);
    }
  }
}
```

Adding such validator is a bit more effort as we have to replace the previously autoconfigured JwtDecoder
with our own bean definition.

To achieve this, open again the class _com.example.todo.config.ToDoWebSecurityConfiguration_
one more time and add the customized JwtDecoder with the validator.

```java
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
public class ToDoWebSecurityConfiguration {

  // ...

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
```

Now we can re-start the application and test again the same request we had retrieved an '403' error before.
For this just use the `gradlew bootRun` command.

Now it is time to test again using the well known request for the ToDo list.
Try to change the validator and check if the validator works as expected and leads to an authentication error when validation fails for the expected audience.

## Opaque Tokens

Changing the resource server to use opaque tokens for authentication is quite easy.

Just replace the _jwt()_ reference in the _ToDoWebSecurityConfiguration_ by the _opaque()_ reference like this:

```
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

    // ...
    
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
                .oauth2ResourceServer().opaqueToken(withDefaults());
        return http.build();
    }

    // ...
}
```

and then replace the jwt entries in the _application.yml_ by the new _opaquetoken_ entries:

```
spring:
  application:
    name: ToDoApp
  datasource:
    embedded-database-connection: h2
    hikari:
      jdbc-url: jdbc:h2:mem:todo
  jpa:
    open-in-view: false
    generate-ddl: on
    hibernate:
      ddl-auto: create-drop
  jackson:
    default-property-inclusion: non_null
  security:
    oauth2:
      resourceserver:
        opaquetoken:
          introspection-uri: http://localhost:9000/oauth2/introspect
```

And for testing please retrieve an opaque token from the Spring Authorization Server by switching the client_id to _demo-client-opaque_ (client_secret is still _secret_) or _demo-client-opaque-pkce_.

<hr>

This ends lab 1. In the next [lab 2](../lab2) we will see how to test for OAuth and JWT authentication.

__<u>Important Note</u>__: If you could not finish part 1, then just use the
project __lab1/final-jwt__ to start into the next labs or use the __lab2/initial__ project as new starting point.

<hr>

To continue with testing the OAuth2/OIDC resource server application please head over to [Lab 2](../lab2).