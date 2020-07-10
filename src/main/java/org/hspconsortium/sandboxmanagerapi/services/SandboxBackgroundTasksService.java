package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;

import java.io.UnsupportedEncodingException;

public interface SandboxBackgroundTasksService {
    void cloneSandbox(final Sandbox newSandbox, final Sandbox clonedSandbox, final String bearerToken, final String sandboxApiURL) throws UnsupportedEncodingException;
}
