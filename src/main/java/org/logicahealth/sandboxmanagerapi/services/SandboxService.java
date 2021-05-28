package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;

public interface SandboxService {

    Sandbox save(final Sandbox sandbox);

    void deleteQueuedSandboxes();

    void delete(final int id);

    void delete(final Sandbox sandbox, final String bearerToken);

    void delete(final Sandbox sandbox, final String bearerToken, final User isAdmin, final boolean sync);

    Sandbox create(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException;

    void clone(final Sandbox newSandbox, final String clonedSandboxId, final User user, final String bearerToken) throws UnsupportedEncodingException;

    Sandbox update(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException;

    void removeMember(final Sandbox sandbox, final User user, final String bearerToken);

    void addMember(final Sandbox sandbox, final User user);

    void addMember(final Sandbox sandbox, final User user, final Role role);

    void addMemberRole(final Sandbox sandbox, final User user, final Role role);

    void removeMemberRole(final Sandbox sandbox, final User user, final Role role);

    void changePayerForSandbox(final Sandbox sandbox, final User payer);

    boolean hasMemberRole(final Sandbox sandbox, final User user, final Role role);

    void addSandboxImport(final Sandbox sandbox, final SandboxImport sandboxImport);

    void reset(final Sandbox sandboxId, final String bearerToken);

    void sandboxLogin(final String sandboxId, final String userId);

    boolean isSandboxMember(final Sandbox sandbox, final User user);

    String getSandboxApiURL(final Sandbox sandbox);

    String getSystemSandboxApiURL();

    List<Sandbox> getAllowedSandboxes(final User user);

    Sandbox findBySandboxId(final String sandboxId);

    List<Sandbox> findByVisibility(final Visibility visibility);

    String fullCount();

    String fullCountForSpecificTimePeriod(Timestamp endDate);

    String schemaCount(final String apiEndpointIndex);

    String schemaCountForSpecificTimePeriod(final String apiEndpointIndex, final Timestamp endDate);

    String intervalCount(final Timestamp intervalTime);

    List<Sandbox> findByPayerId(Integer payerId);

    Iterable<Sandbox> findAll();

    String getApiSchemaURL(final String apiEndpointIndex);

    String newSandboxesInIntervalCount(final Timestamp intervalTime, final String apiEndpointIndex);

    String newSandboxesInIntervalCountForSpecificTimePeriod(String apiEndpointIndex, Timestamp beginDate, Timestamp endDate);

    String intervalCountForSpecificTimePeriod(Timestamp beginDate, Timestamp endDate);

    SandboxCreationStatusQueueOrder getQueuedCreationStatus(String sandboxId);

    void exportSandbox(Sandbox sandbox, String sbmUserId, String bearerToken);

    void generateKeyPair();

    void importSandbox(MultipartFile zipFile, User requestingUser, String bearerToken);

    void importSandboxWithDifferentId(MultipartFile zipFile, String sandboxId, User requestingUser, String bearerToken);

}
