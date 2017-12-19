package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.Patient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepository extends CrudRepository<Patient, Integer> {
    Patient findByFhirIdAndSandboxId(@Param("fhirId") String fhirId, @Param("sandboxId") String sandboxId);
    List<Patient> findBySandboxId(@Param("sandboxId") String sandboxId);
}
