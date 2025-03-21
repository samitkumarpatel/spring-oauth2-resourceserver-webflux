package net.samitkumar.spring_oauth2_resource_api;

import lombok.Builder;
import lombok.ToString;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class SpringOauth2ResourceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringOauth2ResourceApiApplication.class, args);
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
						.customerCode(getValueAsString(claims.get("")))
						.roles(getValueAsList(claims.get("roles")))
						.scope(getValueAsList(claims.get("scope")))
						.personId(getValueAsString(claims.get("")))
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
class User {
	private List<String> roles;
	private List<String> scope;
	@ToString.Exclude
	private String userName;
	private String customerCode;
	private List<String> customerCodes;
	private String carrierCode;
	private String personId;

	public String rolesAsString() {
		if (!CollectionUtils.isEmpty(roles)) {
			return String.join(",", roles);
		}
		return "";
	}
}