package org.logicahealth.sandboxmanagerapi.services;

public interface SandboxEncryptionService {
    void generateKeyPair();
    String sign(String key);
    String decryptSignature(String signature);
}
