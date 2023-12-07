package ru.nechaev.pasteshare.security.cookie;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.exception.db.EntityNotFoundException;
import ru.nechaev.pasteshare.repository.DeactivatedTokenRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.security.Token;
import ru.nechaev.pasteshare.security.TokenUser;

import java.time.Instant;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class TokenAuthenticationUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    private final UserRepository userRepository;
    private final DeactivatedTokenRepository deactivatedTokenRepository;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken) throws UsernameNotFoundException {
        if (authenticationToken.getPrincipal() instanceof Token token) {
            String username = token.username();
            User user = userRepository.findUserByName(username)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with name: " + username));
            boolean credentialsExpired = deactivatedTokenRepository.existsById(token.id()) && token.expiresAt().isAfter(Instant.now());
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return new TokenUser(user.getName(), user.getPassword(), true, true, !credentialsExpired, true,
                    token.authorities().stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList(), token);
        }
        throw new UsernameNotFoundException("Principal must me of type Token");
    }
}
