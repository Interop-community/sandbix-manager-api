package org.logicahealth.sandboxmanagerapi.services;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.model.Visibility;
import org.logicahealth.sandboxmanagerapi.repositories.SandboxRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.SandboxBackgroundTasksServiceImpl;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SandboxBackgroundTasksServiceTest {

    private final CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    private final SandboxRepository repository = mock(SandboxRepository.class);
    private final UserAccessHistoryService userAccessHistoryService = mock(UserAccessHistoryService.class);
    private final SandboxExportService sandboxExportService = mock(SandboxExportService.class);
    private SandboxInviteService sandboxInviteService = mock(SandboxInviteService.class);
    private AppService appService = mock(AppService.class);
    private UserService userService = mock(UserService.class);
    private UserPersonaService userPersonaService = mock(UserPersonaService.class);
    private CdsServiceEndpointService cdsServiceEndpointService = mock(CdsServiceEndpointService.class);
    private CdsHookService cdsHookService = mock(CdsHookService.class);
    private LaunchScenarioService launchScenarioService = mock(LaunchScenarioService.class);
    private FhirProfileDetailService fhirProfileDetailService = mock(FhirProfileDetailService.class);
    private SandboxEncryptionService sandboxEncryptionService = mock(SandboxEncryptionService.class);
    private EmailService emailService = mock(EmailService.class);
    private final SandboxBackgroundTasksService sandboxBackgroundTasksService = new SandboxBackgroundTasksServiceImpl(httpClient, repository, userAccessHistoryService, sandboxExportService, sandboxInviteService, appService, userService, userPersonaService, cdsServiceEndpointService, launchScenarioService, fhirProfileDetailService, sandboxEncryptionService, emailService);

    private final Sandbox sandbox = spy(Sandbox.class);
    private final Sandbox newSandbox = spy(Sandbox.class);
    private final User user = spy(User.class);
    private final CloseableHttpResponse response = spy(CloseableHttpResponse.class);

    @Before
    public void setup() {
        sandbox.setId(1);
        sandbox.setSandboxId("sandboxId");
        sandbox.setApiEndpointIndex("9");
        sandbox.setVisibility(Visibility.PUBLIC);
        newSandbox.setSandboxId("new-sandbox");
        newSandbox.setApiEndpointIndex("10");
    }

    @Test(expected = RuntimeException.class)
    public void cloneTestThrowsExceptionInApiCall() throws IOException {
        when(httpClient.execute(any())).thenThrow(IOException.class);
        sandboxBackgroundTasksService.cloneSandboxSchema(newSandbox, sandbox, user, "bearerToken", "sandboxApiURL");
    }

    @Test(expected = RuntimeException.class)
    public void cloneTestErrorInCallToReferenceApi() throws IOException {
        StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "NOT FINE!");
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(mock(HttpEntity.class));
        sandboxBackgroundTasksService.cloneSandboxSchema(newSandbox, sandbox, user, "bearerToken", "sandboxApiURL");
    }
}
