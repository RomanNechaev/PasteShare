package ru.nechaev.pasteshare.security.config;

import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.nechaev.pasteshare.entitity.Role;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.exception.db.EntityNotFoundException;
import ru.nechaev.pasteshare.repository.DeactivatedTokenRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.security.cookie.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Configuration
public class SecurityConfiguration {
    private final DeactivatedTokenRepository deactivatedTokenRepository;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer,
            TokenCookieJweStringSerializer tokenCookieJweStringSerializer) throws Exception {
        TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy = new TokenCookieSessionAuthenticationStrategy();
        tokenCookieSessionAuthenticationStrategy.setTokenStringSerializer(tokenCookieJweStringSerializer);
        http.httpBasic(Customizer.withDefaults())
                .addFilterAfter(new GetCsrfTokenFilter(), ExceptionTranslationFilter.class)
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/registration").permitAll()
                                .requestMatchers("/swagger-ui/index.html").permitAll()
                                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/v1/user/**")).hasRole(Role.ADMIN.name())
                                .requestMatchers(AntPathRequestMatcher.antMatcher("/v3/api-docs/**")).permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**")).permitAll()
                                .anyRequest().authenticated())
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionAuthenticationStrategy(tokenCookieSessionAuthenticationStrategy))
                .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .sessionAuthenticationStrategy((authentication, request, response) -> {
                        }));

        http.apply(tokenCookieAuthenticationConfigurer);

        return http.build();
    }


    @Bean
    TokenCookieJweStringSerializer tokenCookieJweStringSerializer(@Value("${spring.security.jwt.cookie-token-key}") String cookieTokenKey) throws Exception {
        return new TokenCookieJweStringSerializer(new DirectEncrypter(OctetSequenceKey.parse(cookieTokenKey)));
    }

    @Bean
    public TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer(@Value("${spring.security.jwt.cookie-token-key}") String cookieTokenKey) throws Exception {
        return new TokenCookieAuthenticationConfigurer(deactivatedTokenRepository, userRepository)
                .tokenCookieAuthenticationConfigurer(new TokenCookieJweStringDeserializer(
                        new DirectDecrypter(OctetSequenceKey.parse(cookieTokenKey))
                ));
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository
                    .findUserByName(username)
                    .orElseThrow(() -> new EntityNotFoundException("User not found!"));
            return org.springframework.security.core.userdetails.User
                    .builder()
                    .username(user.getName())
                    .password(user.getPassword())
                    .authorities(getAuthorities(List.of(user.getRole().name())))
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static List<GrantedAuthority> getAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return authorities;
    }

}
