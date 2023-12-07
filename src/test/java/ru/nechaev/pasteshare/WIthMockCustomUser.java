package ru.nechaev.pasteshare;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)

public @interface WIthMockCustomUser {
    String username() default "user";

    String name() default "test";

    String[] roles() default {"USER"};
}
