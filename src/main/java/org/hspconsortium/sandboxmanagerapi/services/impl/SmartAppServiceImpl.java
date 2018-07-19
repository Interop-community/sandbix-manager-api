package org.hspconsortium.sandboxmanagerapi.services.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SmartApp;
import org.hspconsortium.sandboxmanagerapi.model.Visibility2;
import org.hspconsortium.sandboxmanagerapi.repositories.SmartAppRepository;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.SmartAppService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class SmartAppServiceImpl implements SmartAppService {

    @Inject
    private SmartAppRepository smartAppRepository;

    private SandboxService sandboxService;

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Override
    public SmartApp save(@NonNull final SmartApp smartApp, @NonNull final String performedBy) {
        // todo see if performedBy has rights

        // todo need to process the manifest and see if any scopes or anything are changing

        if (smartApp.getCreatedTimestamp() == null) {
            smartApp.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }

        // unless we manage the client id separately
        return smartAppRepository.save(smartApp);
    }

    @Override
    @Transactional
    public void delete(@NonNull final String id, @NonNull final String performedBy) {
        // todo see if performedBy has rights
        // todo make sure nothing is using this app, fail if something is using it
        smartAppRepository.delete(id);
    }

    @Override
    public void delete(@NonNull final SmartApp smartApp, @NonNull final String performedBy) {
        // todo see if performedBy has rights
        // todo make sure nothing is using this app, fail if something is using it
        smartAppRepository.delete(smartApp);
    }

    @Override
    public SmartApp getById(@NonNull final String id, @NonNull final String performedBy) {
        // todo see if performedBy has rights
        return smartAppRepository.findOne(id);
    }

    @Override
    public List<SmartApp> findByOwnerId(@NonNull final int ownerId, @NonNull final String performedBy) {
        // todo see if performedBy has rights
        return smartAppRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<SmartApp> findPublic() {
        return smartAppRepository.findByVisibility(Visibility2.PUBLIC);
    }

    @Override
    public SmartApp findById(final String id) {
        return smartAppRepository.findById(id);
    }

}
