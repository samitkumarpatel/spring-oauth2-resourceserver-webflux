package net.samitkumar.spring_oauth2_resourceserver_webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class SpringOauth2ResourceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringOauth2ResourceApiApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes() {
		return RouterFunctions.route()
				.GET("/who-am-i", request -> ReactiveSecurityContextHolder
						.getContext()
						.map(SecurityContext::getAuthentication)
						.map(this::toMap)
						.flatMap(user -> ServerResponse.ok().bodyValue(user))
						.switchIfEmpty(Mono.error(new ForbiddenException())))
				.build();
	}

	private Map<String, Object> toMap(Authentication authentication) {
		return Optional.ofNullable(authentication)
				.map(JwtAuthenticationToken.class::cast)
				.map(JwtAuthenticationToken::getToken)
				.map(Jwt::getClaims)
				.orElseThrow();
	}
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException extends RuntimeException {
	public ForbiddenException() {
		super();
	}
}