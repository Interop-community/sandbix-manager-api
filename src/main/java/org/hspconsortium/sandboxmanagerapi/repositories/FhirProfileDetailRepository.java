package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfileDetail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FhirProfileDetailRepository extends CrudRepository<FhirProfileDetail, Integer> {

    FhirProfileDetail findByFhirProfileId(@Param("fhirProfileId") Integer fhirProfileId);

    List<FhirProfileDetail> findBySandboxId(@Param("sandboxId") String sandboxId);

    FhirProfileDetail findByProfileIdAndSandboxId(@Param("profileId") String profileId, @Param("sandboxId") String sandboxId);
}
