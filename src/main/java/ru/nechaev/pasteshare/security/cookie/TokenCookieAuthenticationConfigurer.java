package ru.nechaev.pasteshare.security.cookie;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.stereotype.Component;
import ru.nechaev.pasteshare.entitity.DeactivatedToken;
import ru.nechaev.pasteshare.repository.DeactivatedTokenRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.security.Token;
import ru.nechaev.pasteshare.security.TokenUser;

import java.sql.Date;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class TokenCookieAuthenticationConfigurer extends AbstractHttpConfigurer<TokenCookieAuthenticationConfigurer, HttpSecurity> {
    private Function<String, Token> tokenCookieStringDeserializer;
    private final DeactivatedTokenRepository deactivatedTokenRepository;
    private final UserRepository userRepository;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        builder.logout(logout -> logout.addLogoutHandler(
                new CookieClearingLogoutHandler("__Host-auth-token")
        ).addLogoutHandler(((request, response, authentication) -> {
            if (authentication != null &&
                    authentication.getPrincipal() instanceof TokenUser user) {
                DeactivatedToken deactivatedToken = new DeactivatedToken(user.getToken().id(), Date.from(user.getToken().expiresAt()));
                deactivatedTokenRepository.save(deactivatedToken);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        })));
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        AuthenticationFilter cookieAuthenticationFilter = new AuthenticationFilter(
                builder.getSharedObject(AuthenticationManager.class),
                new TokenCookieAuthenticationConvertor(tokenCookieStringDeserializer));
        cookieAuthenticationFilter.setSuccessHandler(((request, response, authentication) -> {
        }));
        cookieAuthenticationFilter.setFailureHandler(new AuthenticationEntryPointFailureHandler(
                new Http403ForbiddenEntryPoint()
        ));

        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(
                new TokenAuthenticationUserDetailsService(userRepository, deactivatedTokenRepository));

        builder.addFilterAfter(cookieAuthenticationFilter, CsrfFilter.class)
                .authenticationProvider(authenticationProvider);
    }

    public TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer(Function<String, Token> tokenCookieStringDeserializer) {
        this.tokenCookieStringDeserializer = tokenCookieStringDeserializer;
        return this;
    }

}
