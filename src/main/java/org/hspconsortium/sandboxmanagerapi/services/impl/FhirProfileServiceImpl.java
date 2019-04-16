package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfile;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirProfileRepository;
import org.hspconsortium.sandboxmanagerapi.services.FhirProfileService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class FhirProfileServiceImpl implements FhirProfileService {

    private final FhirProfileRepository repository;

    @Inject
    public FhirProfileServiceImpl(final FhirProfileRepository repository) { this.repository = repository; }

    @Override
    @Transactional
    public void save(final List<FhirProfile> fhirProfiles) {
        for (FhirProfile fhirProfile : fhirProfiles) {
            repository.save(fhirProfile);
        }
    }
}
