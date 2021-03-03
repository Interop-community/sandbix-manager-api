package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.ContextParams;
import org.logicahealth.sandboxmanagerapi.model.LaunchScenario;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.model.Visibility;

import java.util.List;

public interface LaunchScenarioService {

    LaunchScenario save(final LaunchScenario launchScenario);

    void delete(final int id);

    void delete(final LaunchScenario launchScenario);

    void deleteAssociatedLaunchScenarios(List<LaunchScenario> launchScenarios);

    LaunchScenario create(final LaunchScenario launchScenario);

    LaunchScenario update(final LaunchScenario launchScenario);

    LaunchScenario updateContextParams(final LaunchScenario launchScenario, final List<ContextParams> contextParams);

    Iterable<LaunchScenario> findAll();

    LaunchScenario getById(final int id);

    List<LaunchScenario> findBySandboxId(final String sandboxId);

    List<LaunchScenario> findByAppIdAndSandboxId(final int appId, final String sandboxId);

    List<LaunchScenario> findByUserPersonaIdAndSandboxId(final int userPersonaId, final String sandboxId);

    List<LaunchScenario> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility);

    List<LaunchScenario> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy);

    List<LaunchScenario> updateLastLaunchForCurrentUser(final List<LaunchScenario> launchScenarios, final User user);

    List<LaunchScenario> findByCdsHookIdAndSandboxId(final int cdsHookId, final String sandboxId);

}
