package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.UserPersona;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;
import org.hspconsortium.sandboxmanagerapi.repositories.UserPersonaRepository;
import org.hspconsortium.sandboxmanagerapi.services.UserPersonaService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class UserPersonaServiceImpl implements UserPersonaService {
    private final UserPersonaRepository repository;

    @Inject
    public UserPersonaServiceImpl(final UserPersonaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserPersona save(UserPersona userPersona) {
        return repository.save(userPersona);
    }

    @Override
    public UserPersona getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    public UserPersona findByPersonaUserId(String personaUserId) {
        return repository.findByPersonaUserId(personaUserId);
    }

    @Override
    public UserPersona findByPersonaUserIdAndSandboxId(final String personaUserId, final String sandboxId) {
        return repository.findByPersonaUserIdAndSandboxId(personaUserId, sandboxId);
    }

    @Override
    public List<UserPersona> findBySandboxIdAndCreatedByOrVisibility(String sandboxId, String createdBy, Visibility visibility) {
        return  repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public UserPersona findDefaultBySandboxId(String sandboxId, String createdBy, Visibility visibility) {
        List<UserPersona> personas = repository.findDefaultBySandboxId(sandboxId, createdBy, visibility);
        return !personas.isEmpty() ? personas.get(0) : null;
    }

    @Override
    public List<UserPersona> findBySandboxIdAndCreatedBy(String sandboxId, String createdBy) {
        return  repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Override
    public List<UserPersona> findBySandboxId(final String sandboxId) {
        return  repository.findBySandboxId(sandboxId);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    public void delete(UserPersona userPersona) {
        delete(userPersona.getId());
    }

    @Override
    @Transactional
    public UserPersona create(UserPersona userPersona) {
        userPersona.setCreatedTimestamp(new Timestamp(new Date().getTime()));
        checkpersonaUserId(userPersona);
        return createOrUpdate(userPersona);
    }

    @Override
    @Transactional
    public UserPersona update(UserPersona userPersona) {
        return createOrUpdate(userPersona);
    }

    private UserPersona createOrUpdate(final UserPersona userPersona) {
        return save(userPersona);
    }

    private void checkpersonaUserId(UserPersona userPersona) {
        UserPersona userPersonaExists = findByPersonaUserIdAndSandboxId(userPersona.getPersonaUserId(), userPersona.getSandbox().getSandboxId());
        if (userPersonaExists != null) {
            throw new IllegalArgumentException("Persona user " + userPersona.getPersonaUserId() + " already in use in sandbox " + userPersona.getSandbox().getSandboxId());
        }
    }
}