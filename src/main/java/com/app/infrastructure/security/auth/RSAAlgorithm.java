package com.app.infrastructure.security.auth;

import com.app.infrastructure.security.auth.exception.AuthException;
import com.auth0.jwt.algorithms.Algorithm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAAlgorithm {
    private final Algorithm algorithm;

    public RSAAlgorithm() {
        KeyFactory keyFactory = null;

        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new AuthException("Failed trying to instantiate key factory: " + e.getMessage());
        }

        RSAPublicKey publicKey = this.getPublicKey(keyFactory);
        RSAPrivateKey privateKey = this.getPrivateKey(keyFactory);

        this.algorithm = Algorithm.RSA256(publicKey, privateKey);
    }

    public Algorithm getAlgorithm() {
        return this.algorithm;
    }

    private String getKeysPath() {
        URL keyFilesURL = Auth0JWTHandler.class.getClassLoader().getResource("keys");

        if (keyFilesURL == null) {
            throw new AuthException("No keys found in path");
        }

        File keysDirectory = new File(keyFilesURL.getFile());

        return keysDirectory.getAbsolutePath();
    }

    private RSAPublicKey getPublicKey(KeyFactory keyFactory) {
        String publicKeyPath = this.getKeysPath() + File.separator + "public_key.pem";

        byte[] publicKeyBytes = new byte[0];

        try {
            publicKeyBytes = Files.readAllBytes(Paths.get(publicKeyPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

        RSAPublicKey publicKey = null;

        try {
            publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        return publicKey;
    }

    private RSAPrivateKey getPrivateKey(KeyFactory keyFactory) {
        String privateKeyPath = this.getKeysPath() + File.separator + "private_key.pem";

        byte[] privateKeyBytes = new byte[0];

        try {
            privateKeyBytes = Files.readAllBytes(Paths.get(privateKeyPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        RSAPrivateKey privateKey = null;

        try {
            privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        return privateKey;
    }
}
