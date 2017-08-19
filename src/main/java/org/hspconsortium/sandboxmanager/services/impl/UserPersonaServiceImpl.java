package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.UserPersona;
import org.hspconsortium.sandboxmanager.model.Visibility;
import org.hspconsortium.sandboxmanager.repositories.UserPersonaRepository;
import org.hspconsortium.sandboxmanager.services.UserPersonaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
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
        return  repository.findByPersonaUserIdAndSandboxId(personaUserId, sandboxId);
    }

    @Override
    public List<UserPersona> findBySandboxIdAndCreatedByOrVisibility(String sandboxId, String createdBy, Visibility visibility) {
        return  repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public UserPersona findDefaultBySandboxId(String sandboxId, String createdBy, Visibility visibility) {
        List<UserPersona> personas = repository.findDefaultBySandboxId(sandboxId, createdBy, visibility);
        return personas.size() > 0 ? personas.get(0) : null;
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
}