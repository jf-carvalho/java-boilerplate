package com.app.infrastructure.security.hasher;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SpringBcryptHasherTest {

    @Test
    public void shouldGenerateAndReturnSalt() {
        SpringBcryptHasher hasher = new SpringBcryptHasher();
        assertInstanceOf(String.class, hasher.getSalt());
    }

    @Test
    public void shouldGenerateAndReturnHash() {
        SpringBcryptHasher hasher = new SpringBcryptHasher();

        String salt = hasher.getSalt();

        assertInstanceOf(String.class, hasher.getHash("123456", salt));
    }

    @Test
    public void shouldValidateHash() {
        SpringBcryptHasher hasher = new SpringBcryptHasher();

        String salt = hasher.getSalt();

        String hash = hasher.getHash("123456", salt);

        assertTrue(hasher.checkHash(hash, "123456"));
    }

    @Test
    public void shouldInvalidateHash() {
        SpringBcryptHasher hasher = new SpringBcryptHasher();

        String salt = hasher.getSalt();

        String hash = hasher.getHash("123456", salt);

        assertFalse(hasher.checkHash(hash, "1234567890"));
    }
}
