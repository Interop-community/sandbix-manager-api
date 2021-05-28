package org.logicahealth.sandboxmanagerapi.services;

import java.io.InputStream;

public interface S3BucketService {
    void putFile(String fileName, InputStream inputStream);
    void notifyUser(String user);
    void cleanUpOldFiles();
}
