package org.logicahealth.sandboxmanagerapi.services.impl;

import org.apache.commons.codec.binary.Base64;
import org.logicahealth.sandboxmanagerapi.services.SandboxEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Service
public class SandboxEncryptionServiceImpl implements SandboxEncryptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxEncryptionServiceImpl.class.getName());
    private static final String KEY_PAIR_ALGORITHM = "RSA";
    private static final int KEY_LENGTH = 1024;
    private static final String KEY_STORAGE_PATH = "/etc";
    private static final String PRIVATE_KEY_FILE = "privateKey";
    private static final String PUBLIC_KEY_FILE = "publicKey";
    private static final String PUBLIC_KEY_FILE_PATH = KEY_STORAGE_PATH + "/" + PUBLIC_KEY_FILE;
    private static final String PRIVATE_KEY_FILE_PATH = KEY_STORAGE_PATH + "/" + PRIVATE_KEY_FILE;

    @Override
    public void generateKeyPair() {
        if (keysExist()) {
            LOGGER.info("Key pair already exists");
            return;
        }
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
            keyPairGenerator.initialize(KEY_LENGTH);
            var keyPair = keyPairGenerator.generateKeyPair();
            storeKey(PRIVATE_KEY_FILE_PATH, keyPair.getPrivate().getEncoded());
            storeKey(PUBLIC_KEY_FILE_PATH, keyPair.getPublic().getEncoded());
            LOGGER.info("Key pair created");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Exception while generating key pair", e);
        }
    }

    @Override
    public String encrypt(String key) {
        try {
            var privateKey = retrievePrivateKey();
            var cipher = Cipher.getInstance(KEY_PAIR_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return Base64.encodeBase64String(cipher.doFinal(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Exception while signing key", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while signing key", e);
        }
    }

    private PrivateKey retrievePrivateKey() {
        try {
            var keyBytes = Files.readAllBytes(new File(PRIVATE_KEY_FILE_PATH).toPath());
            var spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(KEY_PAIR_ALGORITHM)
                             .generatePrivate(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("Exception while retrieving private key for signing key", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while retrieving private key for signing key", e);
        }
    }
    @Override
    public String decryptSignature(String signature) {
        try {
            var publicKey = retrievePublicKey();
            var cipher = Cipher.getInstance(KEY_PAIR_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return new String(cipher.doFinal(Base64.decodeBase64(signature)), StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while decrypting signature", e);
        }
    }

    private PublicKey retrievePublicKey() {
        try {
            var keyBytes = Files.readAllBytes(new File(PUBLIC_KEY_FILE_PATH).toPath());
            var spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(KEY_PAIR_ALGORITHM)
                             .generatePublic(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("Exception while retrieving public key for decryption", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while retrieving public key for decryption", e);
        }
    }

    private boolean keysExist() {
        var keysDirectory = new File(KEY_STORAGE_PATH);
        if (keysDirectory.isDirectory() && keysDirectory.exists()) {
            var privateKeyFile = new File(PRIVATE_KEY_FILE_PATH);
            var publicKeyFile = new File(PUBLIC_KEY_FILE_PATH);
            return privateKeyFile.isFile() && privateKeyFile.exists() && publicKeyFile.isFile() && publicKeyFile.exists();
        }
        return false;
    }

    private void storeKey(String keyFile, byte[] key) {
        new File(KEY_STORAGE_PATH).mkdir();
        try (var fileOutputStream = new FileOutputStream(new File(keyFile))) {
            fileOutputStream.write(key);
            fileOutputStream.flush();
        } catch (IOException e) {
            LOGGER.error("Exception while storing key pair", e);
        }
    }

}
