package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.FhirProfile;
import org.logicahealth.sandboxmanagerapi.repositories.FhirProfileRepository;
import org.logicahealth.sandboxmanagerapi.services.FhirProfileService;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FhirProfileServiceImpl implements FhirProfileService {
    private static Logger LOGGER = LoggerFactory.getLogger(FhirProfileServiceImpl.class.getName());

    private FhirProfileRepository repository;

    @Inject
    public FhirProfileServiceImpl(FhirProfileRepository repository) { this.repository = repository; }

    @Override
    @Transactional
    public void save(FhirProfile fhirProfile) { 
        
        LOGGER.info("save");

        repository.save(fhirProfile);

        LOGGER.debug("save: "
        +"Parameters: fhirProfile = "+fhirProfile+"; No return value");

    }

    @Override
    public List<FhirProfile> getAllResourcesForGivenProfileId(Integer fhirProfileId) {
        
        LOGGER.info("getAllResourcesForGivenProfileId");

        LOGGER.debug("getAllResourcesForGivenProfileId: "
        +"Parameters: fhirProfileId = "+fhirProfileId
        +"; Return value = "+repository.findByFhirProfileId(fhirProfileId));

        return repository.findByFhirProfileId(fhirProfileId);
    }

    @Override
    public List<FhirProfile> getAllSDsForGivenProfileId(Integer fhirProfileId){
        
        LOGGER.info("getAllSDsForGivenProfileId");

        LOGGER.debug("getAllSDsForGivenProfileId: "
        +"Parameters: fhirProfileId = "+fhirProfileId
        +"Return value = "+repository.findAllSDsforAProfileByFhirProfileId(fhirProfileId));

        return repository.findAllSDsforAProfileByFhirProfileId(fhirProfileId);
    }

    @Override
    @Transactional
    public void delete(Integer fhirProfileId) {
        
        LOGGER.info("delete");

        List<FhirProfile> fhirProfiles = repository.findByFhirProfileId(fhirProfileId);
        for (FhirProfile fhirProfile : fhirProfiles) {
            repository.delete(fhirProfile);
        }

        LOGGER.debug("delete: "
        +"Parameters: fhirProfileId = "+fhirProfileId+"; No return value");

    }

    @Override
    public List<FhirProfile> getFhirProfileWithASpecificTypeForAGivenSandbox(Integer fhirProfileId, String profileType) {
        LOGGER.info("getFhirProfileWithASpecificTypeForAGivenSandbox");

        LOGGER.debug("getFhirProfileWithASpecificTypeForAGivenSandbox: "
        +"Parameters: fhirProfileId = "+fhirProfileId+", profileType = "+profileType
        +"; Return value = "+repository.findFhirProfileWithASpecificTypeForAGivenSandbox(fhirProfileId, profileType));

        return repository.findFhirProfileWithASpecificTypeForAGivenSandbox(fhirProfileId, profileType);
    }

    @Override
    public List<String> getAllProfileTypesForAGivenProfileId(Integer fhirProfileId) {
        
        LOGGER.info("getAllProfileTypesForAGivenProfileId");

        LOGGER.debug("getAllProfileTypesForAGivenProfileId: "
        +"Parameters: fhirProfileId = "+fhirProfileId
        +"Return value = "+repository.findAllProfileTypeForAGivenProfileId(fhirProfileId));

        return repository.findAllProfileTypeForAGivenProfileId(fhirProfileId);
    }
}
