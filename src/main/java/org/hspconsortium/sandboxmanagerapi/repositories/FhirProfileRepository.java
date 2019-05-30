package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FhirProfileRepository extends CrudRepository<FhirProfile, Integer> {

    List<FhirProfile> findAllSDsforAProfileByFhirProfileId(@Param("fhirProfileId") Integer fhirProfileId);

    List<FhirProfile> findByFhirProfileId(@Param("fhirProfileId") Integer fhirProfileId);

    FhirProfile findFhirProfileWithASpecificTypeForAGivenSandbox(@Param("fhirProfileId") Integer fhirProfileId,
                                                                       @Param("profileType") String profileType);

}
