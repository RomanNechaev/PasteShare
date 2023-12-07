package ru.nechaev.pasteshare.security.cookie;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import ru.nechaev.pasteshare.security.Token;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

public class DefaultTokenCookieFactory implements Function<Authentication, Token> {
    private final Duration tokenTtl = Duration.ofSeconds(86400);

    @Override
    public Token apply(Authentication authentication) {
        return new Token(UUID.randomUUID(), authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList(), Instant.now(), Instant.now().plus(tokenTtl));
    }
}
