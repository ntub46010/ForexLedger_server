package com.vincent.forexledger.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

@Component
public class FirebaseTokenParser implements IAccessTokenParser {

    @Override
    public SpringUser parse(String token) {
        try {
            var firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
            return convertToSpringUser(firebaseToken);
        } catch (FirebaseAuthException e) {
            throw new TokenParseFailedException(e);
        }
    }

    private SpringUser convertToSpringUser(FirebaseToken token) {
        var springUser = new SpringUser();
        springUser.setId(token.getUid());
        springUser.setName(token.getName());
        springUser.setEmail(token.getEmail());

        return springUser;
    }
}
