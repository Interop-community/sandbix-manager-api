package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Sandbox;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public interface SandboxExportService {
    Runnable createZippedSandboxExport(Sandbox sandbox, String sbmUserId, String bearerToken, String apiUrl, PipedOutputStream pipedOutputStream);
    Runnable sendToS3Bucket(PipedInputStream pipedInputStream, String sandboxExportFileName);
}
