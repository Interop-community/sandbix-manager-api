package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.UserLaunchCdsServiceEndpoint;
import org.hspconsortium.sandboxmanagerapi.repositories.UserLaunchCdsServiceEndpointRepository;
import org.hspconsortium.sandboxmanagerapi.services.UserLaunchCdsServiceEndpointService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class UserLaunchCdsServiceEndpointServiceImpl implements UserLaunchCdsServiceEndpointService {

    private final UserLaunchCdsServiceEndpointRepository repository;

    @Inject
    public UserLaunchCdsServiceEndpointServiceImpl(final UserLaunchCdsServiceEndpointRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserLaunchCdsServiceEndpoint save(final UserLaunchCdsServiceEndpoint userLaunchCdsServiceEndpoint) {
        return repository.save(userLaunchCdsServiceEndpoint);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final UserLaunchCdsServiceEndpoint userLaunchCdsServiceEndpoint) {
        delete(userLaunchCdsServiceEndpoint.getId());
    }

    @Override
    @Transactional
    public UserLaunchCdsServiceEndpoint create(final UserLaunchCdsServiceEndpoint userLaunchCdsServiceEndpoint) {
        return save(userLaunchCdsServiceEndpoint);
    }

    @Override
    public UserLaunchCdsServiceEndpoint getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    @Transactional
    public UserLaunchCdsServiceEndpoint update(final UserLaunchCdsServiceEndpoint userLaunchCdsServiceEndpoint) {
        UserLaunchCdsServiceEndpoint updateUserLaunchCdsServiceEndpoint = getById(userLaunchCdsServiceEndpoint.getId());
        if (updateUserLaunchCdsServiceEndpoint != null) {
            updateUserLaunchCdsServiceEndpoint.setLastLaunch(new Timestamp(new Date().getTime()));
            return save(updateUserLaunchCdsServiceEndpoint);
        }
        return null;
    }

    @Override
    public UserLaunchCdsServiceEndpoint findByUserIdAndLaunchScenarioCdsServiceEndpointId(String sbmUserId, int launchScenarioCdsServiceEndpointId) {
        return repository.findByUserIdAndLaunchScenarioCdsServiceEndpointId(sbmUserId, launchScenarioCdsServiceEndpointId);
    }

    @Override
    public List<UserLaunchCdsServiceEndpoint> findByUserId(String sbmUserId) {
        return repository.findByUserId(sbmUserId);
    }

    @Override
    public List<UserLaunchCdsServiceEndpoint> findByLaunchScenarioCdsServiceEndpointId(int launchScenarioCdsServiceEndpointId) {
        return repository.findByLaunchScenarioCdsServiceEndpointId(launchScenarioCdsServiceEndpointId);
    }

}
