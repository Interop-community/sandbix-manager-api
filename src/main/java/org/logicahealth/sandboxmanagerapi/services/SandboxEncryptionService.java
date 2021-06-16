package org.logicahealth.sandboxmanagerapi.services;

public interface SandboxEncryptionService {
    void generateKeyPair();
    String encrypt(String key);
    String decryptSignature(String signature);
}
