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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AppController.class, secure = false)
@ContextConfiguration(classes = AppController.class)
public class AppControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @MockBean
    private AppService appService;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private UserService userService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private RuleService ruleService;

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

    private App app;
    private Sandbox sandbox;
    private User user;

    @Before
    public void setup() {
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
        app = new App();
        app.setId(1);
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
        app.setSandbox(sandbox);
        app.setVisibility(Visibility.PRIVATE);
        app.setCreatedBy(user);
        Image image = new Image();
        image.setContentType("png");
        image.setBytes(hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d"));
        app.setLogo(image);
    }

    @Test(expected = NestedServletException.class)
    public void getAppNotFoundTest() throws Exception {
        when(appService.getById(app.getId())).thenReturn(null);
        mvc
                .perform(get("/app/" + app.getId()));
    }

    @Test
    public void getAppTest() throws Exception {
        String json = json(app);

        when(appService.getById(app.getId())).thenReturn(app);
        when(appService.getClientJSON(app)).thenReturn(app);

        mvc
                .perform(get("/app/" + app.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void getAppsTest() throws Exception {
        List<App> apps = new ArrayList<>();
        apps.add(app);
        String json = json(apps);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(appService.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), user.getSbmUserId(), Visibility.PUBLIC)).thenReturn(apps);
        mvc
                .perform(get("/app?sandboxId=" + sandbox.getSandboxId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void createAppTest() throws Exception {
        String json = json(app);

        when(sandboxService.findBySandboxId(app.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(ruleService.checkIfUserCanCreateApp(sandbox)).thenReturn(true);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(appService.create(any(), any())).thenReturn(app);

        mvc
                .perform(
                        post("/app")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void createTestSandboxNotFound() throws Exception {
        String json = json(app);

        when(sandboxService.findBySandboxId(app.getSandbox().getSandboxId())).thenReturn(null);

        mvc
                .perform(
                        post("/app")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json));
    }

    @Test
    public void updateTest() throws Exception {
        String json = json(app);

        when(appService.getById(app.getId())).thenReturn(app);
        when(appService.update(any())).thenReturn(app);

        mvc
                .perform(
                        put("/app/" + app.getId())
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void updateTestAppNotFound() throws Exception {
        String json = json(app);

        when(appService.update(any())).thenReturn(app);

        mvc
                .perform(
                        put("/app/" + app.getId())
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void deleteTest() throws Exception {
        when(appService.getById(app.getId())).thenReturn(app);
        doNothing().when(appService).delete(app);
        mvc
                .perform(delete("/app/" + app.getId()))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void deleteTestAppNotFound() throws Exception {
        doNothing().when(appService).delete(app);
        mvc
                .perform(delete("/app/" + app.getId()));
    }

    @Test
    public void getFullImageTest() throws Exception {
        when(appService.getById(app.getId())).thenReturn(app);
        mvc
                .perform(get("/app/" + app.getId() + "/image"))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void getFullImageTestAppNotFound() throws Exception {
        when(appService.getById(app.getId())).thenReturn(null);
        mvc
                .perform(get("/app/" + app.getId() + "/image"));
    }

    @Test
    public void putFullImageTest() throws Exception {
        when(appService.getById(app.getId())).thenReturn(app);
        byte[] bytes = Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:templates/hspc-sndbx-logo.png").getURI()));
        MockMultipartFile file = new MockMultipartFile("file", "hspc-sndbx-logo.png", "image/jpeg", bytes);
        mvc
                .perform(fileUpload("/app/" + app.getId() + "/image").file(file))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void putFullImageTestAppNotFound() throws Exception {
        when(appService.getById(app.getId())).thenReturn(null);
        byte[] bytes = Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:templates/hspc-sndbx-logo.png").getURI()));
        MockMultipartFile file = new MockMultipartFile("file", "hspc-sndbx-logo.png", "image/jpeg", bytes);
        mvc
                .perform(fileUpload("/app/" + app.getId() + "/image").file(file));
    }

    @Test
    public void putFullImageTestImageError() throws Exception {
        when(appService.getById(app.getId())).thenReturn(app);
        when(appService.updateAppImage(any(), any())).thenThrow(IOException.class);
        byte[] bytes = Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:templates/hspc-sndbx-logo.png").getURI()));
        MockMultipartFile file = new MockMultipartFile("file", "hspc-sndbx-logo.png", "image/jpeg", bytes);
        mvc
                .perform(fileUpload("/app/" + app.getId() + "/image").file(file))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteFullImageTest() throws Exception {
        when(appService.getById(app.getId())).thenReturn(app);
        when(appService.deleteAppImage(app)).thenReturn(app);
        mvc
                .perform(delete("/app/" + app.getId() + "/image"))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void deleteFullImageTestAppNotFound() throws Exception {
        when(appService.getById(app.getId())).thenReturn(null);
        mvc
                .perform(delete("/app/" + app.getId() + "/image"));
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}