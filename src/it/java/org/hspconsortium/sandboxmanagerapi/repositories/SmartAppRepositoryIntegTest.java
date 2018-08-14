package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.SandboxManagerApiApplication;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SmartApp;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.Visibility2;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.SmartAppService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = SandboxManagerApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class SmartAppRepositoryIntegTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SmartAppService smartAppService;

    @Autowired
    private UserService userService;

    @Autowired
    private SandboxService sandboxService;

    @MockBean
    private OAuthService oAuthService;

    private User testUser;

    private Sandbox testSandbox;

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

    @Before
    public void setup() {
        when(oAuthService.getOAuthUserId(any())).thenReturn("testuser");

        testUser = userService.findBySbmUserId("testuser");

        if (testUser == null) {
            testUser = new User();
            testUser.setName("testuser");
            testUser.setSbmUserId("testuser");
            testUser = userService.save(testUser);
        }

        testSandbox = sandboxService.findBySandboxId("testsandbox");

        if (testSandbox == null) {
            testSandbox = new Sandbox();
            testSandbox.setSandboxId("testsandbox");
            testSandbox.setName("name");
            testSandbox.setAllowOpenAccess(false);
            testSandbox = sandboxService.save(testSandbox);
        }
    }

    @Test
    public void getNotFoundTest() throws Exception {
        mvc
                .perform(get("/smartapp/not-found"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Rollback
    public void getFoundTest() throws Exception {
        SmartApp smartApp = SmartApp.of(UUID.randomUUID().toString(), testSandbox.getSandboxId(), "manifestUrl",
                "manifest", "clientId", testUser.getId(), new Timestamp(System.currentTimeMillis()), Visibility2.PRIVATE, "samplePatients",
                "info", "briefDesc", "author");

        String json = json(smartApp);

        SmartApp saved = smartAppService.save(smartApp, "testuser");
        SmartApp loaded = smartAppService.getById(smartApp.getId(), "testuser");

        mvc
                .perform(get("/smartapp/" + smartApp.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

//    @Test
//    @Rollback
//    public void saveTest() throws Exception {
//        SmartApp smartApp = SmartApp.of(UUID.randomUUID().toString(), testSandbox.getSandboxId(), "manifestUrl",
//                "manifest", "clientId", testUser.getId(), new Timestamp(System.currentTimeMillis()), Visibility2.PRIVATE, "samplePatients",
//                "info", "briefDesc", "author");
//
//        String json = json(smartApp);
//
//        // save it
//        mvc
//                .perform(
//                        put("/smartapp/" + smartApp.getId())
//                                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                                .content(json))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//
//        // verify created
//        SmartApp found = smartAppService.getById(smartApp.getId(), "testuser");
//        Assert.assertNotNull(found);
//    }
//
//    @Test
//    @Rollback
//    public void deleteTest() throws Exception {
//        SmartApp smartApp = SmartApp.of(UUID.randomUUID().toString(), testSandbox.getSandboxId(), "manifestUrl",
//                "manifest", "clientId", testUser.getId(), new Timestamp(System.currentTimeMillis()), Visibility2.PRIVATE, "samplePatients",
//                "info", "briefDesc", "author");
//
//        String json = json(smartApp);
//
//        // save it
//        smartAppService.save(smartApp, "testuser");
//
//        mvc
//                .perform(delete("/smartapp/" + smartApp.getId()))
//                .andExpect(status().isOk());
//
//        // verify deleted
//        SmartApp found = smartAppService.getById(smartApp.getId(), "testuser");
//        Assert.assertNull(found);
//    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
