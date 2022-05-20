package com.example.library.client.web;

import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Controller
public class ToDoItemController {

  private final WebClient webClient;

  @Value("${library.server}")
  private String libraryServer;

  public ToDoItemController(WebClient webClient) {
    this.webClient = webClient;
  }

  @GetMapping("/index")
  Mono<String> index(Model model) {
    return Mono.just("index");
  }

  @GetMapping("/main")
  Mono<String> main(@AuthenticationPrincipal OidcUser oidcUser, @RegisteredOAuth2AuthorizedClient("spring-authz-server") OAuth2AuthorizedClient oAuth2AuthorizedClient, Model model) {

    model.addAttribute("fullname", oidcUser.getName());
    model.addAttribute(
            "isAdmin",
            ((ArrayList<?>) oidcUser.getClaim("roles")).contains("ADMIN"));
    model.addAttribute("idtoken", oidcUser.getIdToken().getTokenValue());
    model.addAttribute("accesstoken", oAuth2AuthorizedClient.getAccessToken().getTokenValue());
    return Mono.just("main");
  }

  @GetMapping("/todos")
  Mono<String> todos(@AuthenticationPrincipal OidcUser oidcUser, @RegisteredOAuth2AuthorizedClient("spring-authz-server") OAuth2AuthorizedClient oAuth2AuthorizedClient, Model model) {

    model.addAttribute("fullname", oidcUser.getName());
    model.addAttribute(
        "isAdmin",
            ((ArrayList<?>) oidcUser.getClaim("roles")).contains("ADMIN"));
    model.addAttribute("jwt", oAuth2AuthorizedClient.getAccessToken().getTokenValue());
    return webClient
        .get()
        .uri(libraryServer + "/api/todos?user=" + oidcUser.getUserInfo().getSubject())
        .retrieve()
        .onStatus(
            s -> s.equals(HttpStatus.UNAUTHORIZED),
            cr -> Mono.just(new BadCredentialsException("Not authenticated")))
        .onStatus(
            HttpStatus::is4xxClientError,
            cr -> Mono.just(new IllegalArgumentException(cr.statusCode().getReasonPhrase())))
        .onStatus(
            HttpStatus::is5xxServerError,
            cr -> Mono.just(new Exception(cr.statusCode().getReasonPhrase())))
        .bodyToMono(ToDoItemListResource.class)
        .log()
        .map(ToDoItemListResource::get_embedded)
        .map(EmbeddedToDoItemListResource::getToDoItemResourceList)
        .map(
            c -> {
              model.addAttribute("todos", c);
              return "todos";
            });
  }
}
