package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.Patient;
import org.hspconsortium.sandboxmanagerapi.repositories.PatientRepository;
import org.hspconsortium.sandboxmanagerapi.services.PatientService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repository;

    @Inject
    public PatientServiceImpl(final PatientRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Patient save(final Patient patient) {
        return repository.save(patient);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final Patient patient) {
        delete(patient.getId());
    }

    @Override
    public Patient findByFhirIdAndSandboxId(final String fhirId, final String sandboxId) {
        return  repository.findByFhirIdAndSandboxId(fhirId, sandboxId);
    }

    @Override
    public List<Patient> findBySandboxId(final String sandboxId) {
        return  repository.findBySandboxId(sandboxId);
    }

}
