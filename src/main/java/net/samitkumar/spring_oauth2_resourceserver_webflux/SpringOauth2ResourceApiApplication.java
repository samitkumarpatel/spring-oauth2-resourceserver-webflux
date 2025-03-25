package net.samitkumar.spring_oauth2_resourceserver_webflux;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
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
						.map(this::toUser)
						.flatMap(user -> ServerResponse.ok().bodyValue(user))
						.switchIfEmpty(Mono.error(new ForbiddenException())))
				.build();
	}


	public Mono<User> getUserDetails() {
		return ReactiveSecurityContextHolder
				.getContext()
				.map(SecurityContext::getAuthentication)
				.map(this::toUser)
				.switchIfEmpty(Mono.error(new ForbiddenException()));
	}

	private User toUser(Authentication authentication) {
		return Optional.ofNullable(authentication)
				.map(JwtAuthenticationToken.class::cast)
				.map(JwtAuthenticationToken::getToken)
				.map(Jwt::getClaims)
				.map(claims -> User.builder()
						.userName(getValueAsString(claims.get("username")))
						.email(getValueAsString(claims.get("email")))
						.roles(getValueAsList(claims.get("roles")))
						.scope(getValueAsList(claims.get("scope")))
						.id(getValueAsString(claims.get("id")))
						.build())
				.orElseThrow();
	}

	private String getValueAsString(Object o) {
		return Optional.ofNullable(o).map(Object::toString).orElse("");
	}

	private List<String> getValueAsList(Object object) {
		//noinspection unchecked
		return Optional.ofNullable((List<String>)object)
				.orElseGet(List::of)
				.stream()
				.map(Object::toString)
				.toList();
	}

}

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException extends RuntimeException {
	public ForbiddenException() {
		super();
	}

	public ForbiddenException(String message) {
		super(message);
	}
}

@Builder
@Data
class User {
	private List<String> roles;
	private List<String> scope;
	@ToString.Exclude
	private String userName;
	private String id;
	private String email;

	String rolesAsString() {
		if (!CollectionUtils.isEmpty(roles)) {
			return String.join(",", roles);
		}
		return "";
	}
}