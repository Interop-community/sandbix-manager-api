package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = CdsHookController.class, secure = false)
@ContextConfiguration(classes = CdsHookController.class)
public class CdsHookControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @MockBean
    private CdsServiceEndpointService cdsServiceEndpointService;

    @MockBean
    private CdsHookService cdsHookService;

    @MockBean
    private AuthorizationService authorizationService;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    private CdsServiceEndpoint cdsServiceEndpoint;
    private CdsHook cdsHook;
    private User user;
    private Sandbox sandbox;
    private List<CdsHook> cdsHooks = new ArrayList<>();

    @Before
    public void setup() {
        cdsServiceEndpoint = new CdsServiceEndpoint();
        cdsHook = new CdsHook();
        user = new User();
        sandbox = new Sandbox();

        user.setSbmUserId("me");
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole);
        sandbox.setUserRoles(userRoles);
        sandbox.setVisibility(Visibility.PUBLIC);
        sandbox.setSandboxId("sandbox");

        cdsHook.setId(1);
        cdsHook.setHook("patient-view");
        cdsHook.setTitle("Sends a Demo Info Card");
        cdsHook.setHookUrl("http://www.google.com");
        cdsHook.setHookId("demo-suggestion-card");
        cdsHook.setCdsServiceEndpointId(1);

        cdsHooks.add(cdsHook);

        cdsServiceEndpoint.setId(1);
        cdsServiceEndpoint.setSandbox(sandbox);
        cdsServiceEndpoint.setCreatedBy(user);
        cdsServiceEndpoint.setCdsHooks(cdsHooks);
        cdsServiceEndpoint.setUrl("http://www.google.com");

        Image image = new Image();
        image.setContentType("png");
        image.setBytes(hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d"));
        cdsHook.setLogo(image);
    }

    @Test
    public void getFullImageTest() throws Exception {
        when(cdsHookService.getById(cdsHook.getId())).thenReturn(cdsHook);
        mvc
                .perform(get("/cds-hook/" + cdsHook.getId() + "/image"))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void getFullImageTestCdsHookNotFound() throws Exception {
        when(cdsHookService.getById(cdsHook.getId())).thenReturn(null);
        when(cdsServiceEndpointService.getById(cdsHook.getCdsServiceEndpointId())).thenReturn(cdsServiceEndpoint);
        mvc
                .perform(get("/cds-hook/" + cdsHook.getId() + "/image"));
    }

    @Test(expected = NestedServletException.class)
    public void getFullImageTestIOException() throws Exception {
        CdsHook badCdsHook = new CdsHook();
        when(cdsHookService.getById(1)).thenReturn(badCdsHook);
        mvc
                .perform(get("/cds-hook/" + 1 + "/image"));
    }


    @Test
    public void putFullImageTest() throws Exception {
        when(cdsHookService.getById(cdsHook.getId())).thenReturn(cdsHook);
        when(cdsServiceEndpointService.getById(cdsHook.getCdsServiceEndpointId())).thenReturn(cdsServiceEndpoint);
        byte[] bytes = Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:templates/hspc-sndbx-logo.png").getURI()));
        MockMultipartFile file = new MockMultipartFile("file", "hspc-sndbx-logo.png", "image/jpeg", bytes);
        mvc
                .perform(fileUpload("/cds-hook/" + cdsHook.getId() + "/image").file(file))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void putFullImageTestCdsHookNotFound() throws Exception {
        when(cdsHookService.getById(cdsHook.getId())).thenReturn(null);
        when(cdsServiceEndpointService.getById(cdsHook.getCdsServiceEndpointId())).thenReturn(cdsServiceEndpoint);
        byte[] bytes = Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:templates/hspc-sndbx-logo.png").getURI()));
        MockMultipartFile file = new MockMultipartFile("file", "hspc-sndbx-logo.png", "image/jpeg", bytes);
        mvc
                .perform(fileUpload("/cds-hook/" + cdsHook.getId() + "/image").file(file));
    }

    @Test
    public void putFullImageTestImageError() throws Exception {
        when(cdsHookService.getById(cdsHook.getId())).thenReturn(cdsHook);
        when(cdsServiceEndpointService.getById(cdsHook.getCdsServiceEndpointId())).thenReturn(cdsServiceEndpoint);
        when(cdsHookService.updateCdsHookImage(any(), any())).thenThrow(IOException.class);
        byte[] bytes = Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:templates/hspc-sndbx-logo.png").getURI()));
        MockMultipartFile file = new MockMultipartFile("file", "hspc-sndbx-logo.png", "image/jpeg", bytes);
        mvc
                .perform(fileUpload("/cds-hook/" + cdsHook.getId() + "/image").file(file))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteFullImageTest() throws Exception {
        when(cdsHookService.getById(cdsHook.getId())).thenReturn(cdsHook);
        when(cdsServiceEndpointService.getById(cdsHook.getCdsServiceEndpointId())).thenReturn(cdsServiceEndpoint);
        when(cdsHookService.deleteCdsHookImage(cdsHook)).thenReturn(cdsHook);
        mvc
                .perform(delete("/cds-hook/" + cdsHook.getId() + "/image"))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void deleteFullImageTestCdsHookNotFound() throws Exception {
        when(cdsHookService.getById(cdsHook.getId())).thenReturn(null);
        when(cdsServiceEndpointService.getById(cdsHook.getCdsServiceEndpointId())).thenReturn(cdsServiceEndpoint);
        mvc
                .perform(delete("/cds-hook/" + cdsHook.getId() + "/image"));
    }


    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}
