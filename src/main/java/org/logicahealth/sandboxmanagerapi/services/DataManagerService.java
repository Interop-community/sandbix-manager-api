package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Sandbox;

import java.io.UnsupportedEncodingException;

/**
 */
public interface DataManagerService {

    String importPatientData(final Sandbox sandbox, final String bearerToken, final String endpoint, final String patientId, final String fhirIdPrefix) throws UnsupportedEncodingException;

    String reset(final Sandbox sandbox, final String bearerToken) throws UnsupportedEncodingException;
}
