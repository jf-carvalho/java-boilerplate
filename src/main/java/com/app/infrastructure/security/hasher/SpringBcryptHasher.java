package com.app.infrastructure.security.hasher;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class SpringBcryptHasher implements HasherInterface {

    @Override
    public String getSalt() {
        return BCrypt.gensalt();
    }

    @Override
    public String getHash(String stringToBeHashed, String salt) {
        return BCrypt.hashpw(stringToBeHashed, salt);
    }

    @Override
    public boolean checkHash(String hash, String rawString) {
        return BCrypt.checkpw(rawString, hash);
    }
}
