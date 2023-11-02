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
        
        LOGGER.info("Inside FhirProfileServiceImpl - save");

        repository.save(fhirProfile);

        LOGGER.debug("Inside FhirProfileServiceImpl - save: "
        +"Parameters: fhirProfile = "+fhirProfile+"; No return value");

    }

    @Override
    public List<FhirProfile> getAllResourcesForGivenProfileId(Integer fhirProfileId) {
        
        LOGGER.info("Inside FhirProfileServiceImpl - getAllResourcesForGivenProfileId");

        LOGGER.debug("Inside FhirProfileServiceImpl - getAllResourcesForGivenProfileId: "
        +"Parameters: fhirProfileId = "+fhirProfileId
        +"; Return value = "+repository.findByFhirProfileId(fhirProfileId));

        return repository.findByFhirProfileId(fhirProfileId);
    }

    @Override
    public List<FhirProfile> getAllSDsForGivenProfileId(Integer fhirProfileId){
        
        LOGGER.info("Inside FhirProfileServiceImpl - getAllSDsForGivenProfileId");

        LOGGER.debug("Inside FhirProfileServiceImpl - getAllSDsForGivenProfileId: "
        +"Parameters: fhirProfileId = "+fhirProfileId
        +"Return value = "+repository.findAllSDsforAProfileByFhirProfileId(fhirProfileId));

        return repository.findAllSDsforAProfileByFhirProfileId(fhirProfileId);
    }

    @Override
    @Transactional
    public void delete(Integer fhirProfileId) {
        
        LOGGER.info("Inside FhirProfileServiceImpl - delete");

        List<FhirProfile> fhirProfiles = repository.findByFhirProfileId(fhirProfileId);
        for (FhirProfile fhirProfile : fhirProfiles) {
            repository.delete(fhirProfile);
        }

        LOGGER.debug("Inside FhirProfileServiceImpl - delete: "
        +"Parameters: fhirProfileId = "+fhirProfileId+"; No return value");

    }

    @Override
    public List<FhirProfile> getFhirProfileWithASpecificTypeForAGivenSandbox(Integer fhirProfileId, String profileType) {
        LOGGER.info("Inside FhirProfileServiceImpl - getFhirProfileWithASpecificTypeForAGivenSandbox");

        LOGGER.debug("Inside FhirProfileServiceImpl - getFhirProfileWithASpecificTypeForAGivenSandbox: "
        +"Parameters: fhirProfileId = "+fhirProfileId+", profileType = "+profileType
        +"; Return value = "+repository.findFhirProfileWithASpecificTypeForAGivenSandbox(fhirProfileId, profileType));

        return repository.findFhirProfileWithASpecificTypeForAGivenSandbox(fhirProfileId, profileType);
    }

    @Override
    public List<String> getAllProfileTypesForAGivenProfileId(Integer fhirProfileId) {
        
        LOGGER.info("Inside FhirProfileServiceImpl - getAllProfileTypesForAGivenProfileId");

        LOGGER.debug("Inside FhirProfileServiceImpl - getAllProfileTypesForAGivenProfileId: "
        +"Parameters: fhirProfileId = "+fhirProfileId
        +"Return value = "+repository.findAllProfileTypeForAGivenProfileId(fhirProfileId));

        return repository.findAllProfileTypeForAGivenProfileId(fhirProfileId);
    }
}
