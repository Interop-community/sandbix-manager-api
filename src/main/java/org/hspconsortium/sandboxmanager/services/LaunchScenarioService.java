package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.LaunchScenario;

import java.util.List;

public interface LaunchScenarioService {

    LaunchScenario save(LaunchScenario launchScenario);

    void delete(LaunchScenario id);

    Iterable<LaunchScenario> findAll();

    LaunchScenario getById(int id);

    List<LaunchScenario> findByUserIdAndSandboxId(String userId, String sandboxId);

}
