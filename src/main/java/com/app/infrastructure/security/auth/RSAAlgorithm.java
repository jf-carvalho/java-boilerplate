package com.app.infrastructure.security.auth;

import com.app.infrastructure.security.auth.exception.AlgorithmException;
import com.auth0.jwt.algorithms.Algorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAAlgorithm {
    private final Algorithm algorithm;
    private final String keysDir;
    private final KeyFactory keyFactory;

    public RSAAlgorithm(KeyFactory keyFactory, String keysDir) {
        this.keysDir = keysDir;
        this.keyFactory = keyFactory;

        RSAPublicKey publicKey = this.getPublicKey();
        RSAPrivateKey privateKey = this.getPrivateKey();

        this.algorithm = Algorithm.RSA256(publicKey, privateKey);
    }

    private RSAPublicKey getPublicKey() {
        String publicKeyPath = this.keysDir + File.separator + "public-key.pem";

        byte[] publicKeyBytes = null;

        try {
            publicKeyBytes = this.extractPublicKeyBytes(publicKeyPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

        RSAPublicKey publicKey = null;

        try {
            publicKey = (RSAPublicKey) this.keyFactory.generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new AlgorithmException("Failed trying to generate public key: " + e.getMessage());
        }

        return publicKey;
    }

    private RSAPrivateKey getPrivateKey() {
        String privateKeyPath = this.keysDir + File.separator + "private-key.pem";

        byte[] privateKeyBytes = new byte[0];

        try {
            privateKeyBytes = this.extractPrivateKeyBytes(privateKeyPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        RSAPrivateKey privateKey = null;

        try {
            privateKey = (RSAPrivateKey) this.keyFactory.generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new AlgorithmException("Failed trying to generate private key: " + e.getMessage());
        }

        return privateKey;
    }

    private byte[] extractPrivateKeyBytes(String keyPath) throws IOException {
        byte[] keyPEMBytes = Files.readAllBytes(Paths.get(keyPath));

        String keyPEM = new String(keyPEMBytes);
        String pemHeader = "-----BEGIN PRIVATE KEY-----";
        String pemFooter = "-----END PRIVATE KEY-----";
        int start = keyPEM.indexOf(pemHeader) + pemHeader.length();
        int end = keyPEM.indexOf(pemFooter);
        keyPEM = keyPEM.substring(start, end);
        keyPEM = keyPEM.replaceAll("\\s+", "");

        return Base64.getDecoder().decode(keyPEM);
    }

    private byte[] extractPublicKeyBytes(String keyPath) throws IOException {
        byte[] keyPEMBytes = Files.readAllBytes(Paths.get(keyPath));

        String keyPEM = new String(keyPEMBytes);
        String pemHeader = "-----BEGIN PUBLIC KEY-----";
        String pemFooter = "-----END PUBLIC KEY-----";
        int start = keyPEM.indexOf(pemHeader) + pemHeader.length();
        int end = keyPEM.indexOf(pemFooter);
        keyPEM = keyPEM.substring(start, end);
        keyPEM = keyPEM.replaceAll("\\s+", "");

        return Base64.getDecoder().decode(keyPEM);
    }

    public Algorithm getAlgorithm() {
        return this.algorithm;
    }
}
