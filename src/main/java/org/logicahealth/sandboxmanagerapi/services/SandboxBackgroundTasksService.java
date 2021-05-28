package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.zip.ZipInputStream;

public interface SandboxBackgroundTasksService {
    void cloneSandboxSchema(final Sandbox newSandbox, final Sandbox clonedSandbox, final User user, final String bearerToken, final String sandboxApiURL) throws UnsupportedEncodingException;
    void exportSandbox(Sandbox sandbox, String sbmUserId, String bearerToken, String apiUrl);
    void importSandbox(ZipInputStream zipInputStream, Sandbox newSandbox, Map sandboxVersions, User requestingUser, String sandboxApiUrl, String bearerToken);
}
