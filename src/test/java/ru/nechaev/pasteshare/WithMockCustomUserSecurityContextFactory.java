package ru.nechaev.pasteshare;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.nechaev.pasteshare.security.Token;
import ru.nechaev.pasteshare.security.TokenUser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WIthMockCustomUser> {
    //TODO
    @Override
    public SecurityContext createSecurityContext(WIthMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String role : customUser.roles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        UserDetails principal = new TokenUser(customUser.name(), "password", grantedAuthorities, new Token(
                UUID.randomUUID(),
                customUser.name(),
                List.of(customUser.roles()),
                Instant.now(),
                Instant.now().plusSeconds(84000)
        ));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(authentication);


        return context;
    }
}
