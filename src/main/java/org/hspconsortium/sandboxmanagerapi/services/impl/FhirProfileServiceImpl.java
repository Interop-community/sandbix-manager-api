package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfile;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirProfileRepository;
import org.hspconsortium.sandboxmanagerapi.services.FhirProfileService;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

@Service
public class FhirProfileServiceImpl implements FhirProfileService {

    private FhirProfileRepository repository;

    @Inject
    public FhirProfileServiceImpl(FhirProfileRepository repository) { this.repository = repository; }

    @Override
    @Transactional
    public void save(FhirProfile fhirProfile) { repository.save(fhirProfile); }

    @Override
    public List<FhirProfile> getAllResourcesForGivenProfileId(Integer fhirProfileId) {
        return repository.findByFhirProfileId(fhirProfileId);
    }

    @Override
    public List<FhirProfile> getAllSDsForGivenProfileId(Integer fhirProfileId){
        return repository.findAllSDsforAProfileByFhirProfileId(fhirProfileId);
    }

    @Override
    @Transactional
    public void delete(Integer fhirProfileId) {
        List<FhirProfile> fhirProfiles = repository.findByFhirProfileId(fhirProfileId);
        for (FhirProfile fhirProfile : fhirProfiles) {
            repository.delete(fhirProfile);
        }
    }

    @Override
    public FhirProfile findByFullUrlAndFhirProfileId(String fullUrl, Integer fhirProfileId) {
        return repository.findByFullUrlAndFhirProfileId(fullUrl, fhirProfileId);
    }

}
