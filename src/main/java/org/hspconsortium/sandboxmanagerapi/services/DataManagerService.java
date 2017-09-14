package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;

import java.io.UnsupportedEncodingException;

/**
 */
public interface DataManagerService {

    String importPatientData(final Sandbox sandbox, final String bearerToken, final String endpoint, final String patientId, final String fhirIdPrefix) throws UnsupportedEncodingException;

    String reset(final Sandbox sandbox, final String bearerToken) throws UnsupportedEncodingException;
}
