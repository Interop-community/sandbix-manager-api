package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.apache.commons.io.FilenameUtils;
import org.hspconsortium.sandboxmanagerapi.model.FhirProfile;
import org.hspconsortium.sandboxmanagerapi.model.FhirProfileDetail;
import org.hspconsortium.sandboxmanagerapi.model.ProfileTask;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirProfileRepository;
import org.hspconsortium.sandboxmanagerapi.services.FhirProfileService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

@Service
public class FhirProfileServiceImpl implements FhirProfileService {

    private FhirProfileRepository repository;

    @Inject
    public FhirProfileServiceImpl(FhirProfileRepository repository) { this.repository = repository; }

    @Override
    @Transactional
    public void save(FhirProfile fhirProfile) {
        repository.save(fhirProfile);
    }

    @Override
    public List<FhirProfile> getFhirProfiles(Integer fhirProfileId) {
        return repository.findByFhirProfileId(fhirProfileId);
    }

    @Override
    @Transactional
    public void delete(Integer fhirProfileId) {
        List<FhirProfile> fhirProfiles = repository.findByFhirProfileId(fhirProfileId);
        for (FhirProfile fhirProfile : fhirProfiles) {
            repository.delete(fhirProfile);
        }
    }

    @Override
    public FhirProfile findByFullUrlAndFhirProfileId(String fullUrl, Integer fhirProfileId) {
        return repository.findByFullUrlAndFhirProfileId(fullUrl, fhirProfileId);
    }

}
