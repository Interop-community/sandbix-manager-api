package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.AuthClient;

public interface AuthClientService {

    AuthClient save(final AuthClient authClient);

    void delete(final int id);

}
