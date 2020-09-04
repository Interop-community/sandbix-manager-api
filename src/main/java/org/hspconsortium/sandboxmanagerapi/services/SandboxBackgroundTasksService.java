package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;

import java.io.UnsupportedEncodingException;

public interface SandboxBackgroundTasksService {
    void cloneSandboxSchema(final Sandbox newSandbox, final Sandbox clonedSandbox, final User user, final String bearerToken, final String sandboxApiURL) throws UnsupportedEncodingException;
}
