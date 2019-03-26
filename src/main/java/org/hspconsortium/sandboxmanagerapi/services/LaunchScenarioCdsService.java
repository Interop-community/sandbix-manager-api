package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.ContextParams;
import org.hspconsortium.sandboxmanagerapi.model.LaunchScenarioCds;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;
import org.json.JSONObject;

import java.util.List;

public interface LaunchScenarioCdsService {

    LaunchScenarioCds save(final LaunchScenarioCds launchScenarioCds);

    void delete(final int id);

    void delete(final LaunchScenarioCds launchScenarioCds);

    LaunchScenarioCds create(final LaunchScenarioCds launchScenarioCds);

    LaunchScenarioCds update(final LaunchScenarioCds launchScenarioCds);

    LaunchScenarioCds updateContext(final LaunchScenarioCds launchScenarioCds, final JSONObject context);

    Iterable<LaunchScenarioCds> findAll();

    LaunchScenarioCds getById(final int id);

    List<LaunchScenarioCds> findBySandboxId(final String sandboxId);

    List<LaunchScenarioCds> findByCdsIdAndSandboxId(final int cdsId, final String sandboxId);

    List<LaunchScenarioCds> findByUserPersonaIdAndSandboxId(final int userPersonaId, final String sandboxId);

    List<LaunchScenarioCds> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility);

    List<LaunchScenarioCds> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy);

    List<LaunchScenarioCds> updateLastLaunchForCurrentUser(final List<LaunchScenarioCds> launchScenariosCds, final User user);
}
