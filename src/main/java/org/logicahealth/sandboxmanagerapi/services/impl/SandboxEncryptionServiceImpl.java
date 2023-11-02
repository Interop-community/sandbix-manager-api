package org.logicahealth.sandboxmanagerapi.services.impl;

import org.apache.commons.codec.binary.Base64;
import org.logicahealth.sandboxmanagerapi.services.SandboxEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
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

    @Value("${hspc.platform.asymmetricKeysFolder}")
    private String asymmetricKeysFolder;

    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxEncryptionServiceImpl.class.getName());
    private static final String KEY_PAIR_ALGORITHM = "RSA";
    private static final int KEY_LENGTH = 1024;
    private static final String PRIVATE_KEY_FILE = "privateKey";
    private static final String PUBLIC_KEY_FILE = "publicKey";
    private String publicKeyFilePath;
    private String privateKeyFilePath;

    @PostConstruct
    private void setFilePaths() {
        this.publicKeyFilePath = this.asymmetricKeysFolder + "/" + PUBLIC_KEY_FILE;
        this.privateKeyFilePath = this.asymmetricKeysFolder + "/" + PRIVATE_KEY_FILE;
    }

    @Override
    public void generateKeyPair() {
        
        LOGGER.info("Inside SandboxEncryptionServiceImpl - generateKeyPair");

        if (keysExist()) {
            LOGGER.info("Key pair already exists");

            LOGGER.debug("Inside SandboxEncryptionServiceImpl - generateKeyPair: "
            +"No input parameters; No return value");

            return;
        }
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
            keyPairGenerator.initialize(KEY_LENGTH);
            var keyPair = keyPairGenerator.generateKeyPair();
            storeKey(this.privateKeyFilePath, keyPair.getPrivate().getEncoded());
            storeKey(this.publicKeyFilePath, keyPair.getPublic().getEncoded());
            LOGGER.info("Key pair created");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Exception while generating key pair", e);
        }

        LOGGER.debug("Inside SandboxEncryptionServiceImpl - generateKeyPair: "
        +"No input parameters; No return value");
    }

    @Override
    public String encrypt(String key) {
        
        LOGGER.info("Inside SandboxEncryptionServiceImpl - encrypt");

        try {
            var privateKey = retrievePrivateKey();
            var cipher = Cipher.getInstance(KEY_PAIR_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            String retVal = Base64.encodeBase64String(cipher.doFinal(key.getBytes(StandardCharsets.UTF_8)));

            LOGGER.debug("Inside SandboxEncryptionServiceImpl - encrypt: "
            +"Parameters: key = "+key+"; Return value = "+retVal);

            return retVal;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Exception while signing key", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while signing key", e);
        }
    }

    private PrivateKey retrievePrivateKey() {
        
        LOGGER.info("Inside SandboxEncryptionServiceImpl - retrievePrivateKey");

        try {
            var keyBytes = Files.readAllBytes(new File(this.privateKeyFilePath).toPath());
            var spec = new PKCS8EncodedKeySpec(keyBytes);
            
            PrivateKey retVal = KeyFactory.getInstance(KEY_PAIR_ALGORITHM)
                             .generatePrivate(spec);

            LOGGER.debug("Inside SandboxEncryptionServiceImpl - retrievePrivateKey: "
            +"No input parameters; Return value = "+retVal);

            return retVal;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("Exception while retrieving private key for signing key", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while retrieving private key for signing key", e);
        }
    }

    @Override
    public String decryptSignature(String signature) {
        
        LOGGER.info("Inside SandboxEncryptionServiceImpl - decryptSignature");

        try {
            var publicKey = retrievePublicKey();
            var cipher = Cipher.getInstance(KEY_PAIR_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            
            LOGGER.debug("Inside SandboxEncryptionServiceImpl - decryptSignature: "
            +"Parameters: signature = "+signature
            +"; Return value = "+new String(cipher.doFinal(Base64.decodeBase64(signature)), StandardCharsets.UTF_8));

            return new String(cipher.doFinal(Base64.decodeBase64(signature)), StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while decrypting signature", e);
        }
    }

    private PublicKey retrievePublicKey() {
        
        LOGGER.info("Inside SandboxEncryptionServiceImpl - retrievePublicKey");

        try {
            var keyBytes = Files.readAllBytes(new File(this.publicKeyFilePath).toPath());
            var spec = new X509EncodedKeySpec(keyBytes);

            PublicKey retVal = KeyFactory.getInstance(KEY_PAIR_ALGORITHM)
                             .generatePublic(spec);

            LOGGER.debug("Inside SandboxEncryptionServiceImpl - retrievePublicKey: "
            +"No input parameters; Return value = "+retVal);

            return retVal;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("Exception while retrieving public key for decryption", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while retrieving public key for decryption", e);
        }
    }

    private boolean keysExist() {
        
        LOGGER.info("Inside SandboxEncryptionServiceImpl - keysExist");

        var keysDirectory = new File(this.asymmetricKeysFolder);
        if (keysDirectory.isDirectory() && keysDirectory.exists()) {
            var privateKeyFile = new File(privateKeyFilePath);
            var publicKeyFile = new File(this.publicKeyFilePath);

            LOGGER.debug("Inside SandboxEncryptionServiceImpl - keysExist: "
            +"No input parameters; Return value = "
            +(privateKeyFile.isFile() && privateKeyFile.exists() && publicKeyFile.isFile() && publicKeyFile.exists()));

            return privateKeyFile.isFile() && privateKeyFile.exists() && publicKeyFile.isFile() && publicKeyFile.exists();
        }

        LOGGER.debug("Inside SandboxEncryptionServiceImpl - keysExist: "
            +"No input parameters; Return value = false");

        return false;
    }

    private void storeKey(String keyFile, byte[] key) {
        
        LOGGER.info("Inside SandboxEncryptionServiceImpl - storeKey");

        new File(this.asymmetricKeysFolder).mkdir();
        try (var fileOutputStream = new FileOutputStream(new File(keyFile))) {
            fileOutputStream.write(key);
            fileOutputStream.flush();
        } catch (IOException e) {
            LOGGER.error("Exception while storing key pair", e);
        }

        LOGGER.debug("Inside SandboxEncryptionServiceImpl - storeKey: "
        +"Parameters: keyFile = "+keyFile+", key = "+key+"; No return value");

    }

}
