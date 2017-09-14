package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Patient;

import java.util.List;

public interface PatientService {

    Patient save(final Patient patient);

    void delete(final int id);

    void delete(final Patient patient);

    Patient findByFhirIdAndSandboxId(final String fhirId, final String sandboxId);

    List<Patient> findBySandboxId(final String sandboxId);
}
