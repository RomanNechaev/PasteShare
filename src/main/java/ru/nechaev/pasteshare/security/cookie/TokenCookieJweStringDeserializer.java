package ru.nechaev.pasteshare.security.cookie;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import lombok.RequiredArgsConstructor;
import ru.nechaev.pasteshare.security.Token;

import java.text.ParseException;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
public class TokenCookieJweStringDeserializer implements Function<String, Token> {

    private final JWEDecrypter jweDecrypter;


    @Override
    public Token apply(String string) {
        try {
            var encryptedJWT = EncryptedJWT.parse(string);
            encryptedJWT.decrypt(this.jweDecrypter);
            var claimsSet = encryptedJWT.getJWTClaimsSet();
            return new Token(UUID.fromString(claimsSet.getJWTID()), claimsSet.getSubject(),
                    claimsSet.getStringListClaim("authorities"),
                    claimsSet.getIssueTime().toInstant(),
                    claimsSet.getExpirationTime().toInstant());
        } catch (ParseException | JOSEException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}
