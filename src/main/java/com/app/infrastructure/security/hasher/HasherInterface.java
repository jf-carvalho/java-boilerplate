package com.app.infrastructure.security.hasher;

public interface HasherInterface {
    String getSalt();

    String getHash(String stringToBeHashed, String salt);

    boolean checkHash(String hash, String rawString);
}
