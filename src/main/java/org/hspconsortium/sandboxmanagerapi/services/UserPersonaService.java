package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.UserPersona;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;

import java.util.List;

public interface UserPersonaService {

    UserPersona save(final UserPersona userPersona);

    UserPersona getById(final int id);

    void delete(final int id);

    void delete(final UserPersona userPersona);

    UserPersona findByPersonaUserId(final String personaUserId);

    UserPersona findByPersonaUserIdAndSandboxId(final String personaUserId, final String sandboxId);

    List<UserPersona> findBySandboxId(final String sandboxId);

    UserPersona create(final UserPersona userPersona);

    UserPersona update(final UserPersona userPersona);

    List<UserPersona> findBySandboxIdAndCreatedByOrVisibility(String sandboxId, String createdBy, Visibility visibility);

    UserPersona findDefaultBySandboxId(String sandboxId, String createdBy, Visibility visibility);

    List<UserPersona> findBySandboxIdAndCreatedBy(String sandboxId, String createdBy);
}
