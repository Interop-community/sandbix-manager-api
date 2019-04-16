package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FhirProfileRepository extends CrudRepository<FhirProfile, Integer> {

}
