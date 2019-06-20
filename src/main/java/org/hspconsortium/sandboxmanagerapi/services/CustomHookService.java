package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.CustomHook;

import java.util.List;

public interface CustomHookService {

    Iterable<CustomHook> createCustomHooks(List<CustomHook> customHooks);

    void delete(int id);

    void delete(CustomHook customHook);

    CustomHook getById(int id);

    List<CustomHook> findBySandboxId(String sandboxId);

}
