package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.repositories.ConcurrentSandboxNamesRepository;
import org.hspconsortium.sandboxmanagerapi.services.ConcurrentSandboxNameService;

public class ConcurrentSandboxNameServiceImpl implements ConcurrentSandboxNameService {

    private ConcurrentSandboxNamesRepository concurrentSandboxNamesRepository;

    @Override
    public void save(String concurrentSandboxName) {
        concurrentSandboxNamesRepository.save(concurrentSandboxName);
    }

}
