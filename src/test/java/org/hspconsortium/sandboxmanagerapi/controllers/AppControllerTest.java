package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.AppService;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AppController.class, secure = false)
@ContextConfiguration(classes = AppController.class)
public class AppControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AbstractController abstractController;

    @MockBean
    private AppService appService;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private UserService userService;

    @MockBean
    private SandboxService sandboxService;

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

    App app;

    @Before
    public void setup() {
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
        app = new App();
        app.setId(1);
        app.setSandbox(new Sandbox());
    }

    @Test(expected = NestedServletException.class)
    public void getNotFoundTest() throws Exception {
        when(appService.getById(app.getId())).thenReturn(null);

        mvc
                .perform(get("/app/" + app.getId()));
    }

//    @Test
//    public void getFoundTest() throws Exception {
////        App app = App.of(UUID.randomUUID().toString(), "sandboxId", "clientName", "manifestUrl",
////                "clientId", new User(), new Timestamp(System.currentTimeMillis()),
////                Visibility2.PRIVATE, "samplePatients", "info", "briefDesc", "author", CopyType.MASTER,
////                "launchUri", "logoUri", "clientUri", "fhirVersions", new Image(), "clienJSON");
//
//
//        String json = json(app);
//
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Authorization", "sample");
//        Iterator<String> iterator = headers.keySet().iterator();
//        Enumeration headerNames = new Enumeration<String>() {
//            @Override
//            public boolean hasMoreElements() {
//                return iterator.hasNext();
//            }
//
//            @Override
//            public String nextElement() {
//                return iterator.next();
//            }
//        };
//        when(request.getHeaderNames()).thenReturn(headerNames);
//
//        when(appService.getById(app.getId())).thenReturn(app);
//        doReturn("nothing").when(abstractController.checkSandboxUserReadAuthorization(request, app.getSandbox()));
//        when(appService.getClientJSON(app)).thenReturn(app);
//
//        mvc
//                .perform(get("/app/" + app.getId()).header("Authorization", "sample"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//
//    }
//
//    @Test
//    public void saveTest() throws Exception {
////        App app = App.of(UUID.randomUUID().toString(), "sandboxId", "clientName", "manifestUrl",
////                 "clientId", new User(), new Timestamp(System.currentTimeMillis()),
////                Visibility2.PRIVATE, "samplePatients","info", "briefDesc", "author", CopyType.MASTER,
////                "launchUri", "logoUri", "clientUri", "fhirVersions", new Image(), "clienJSON");
//
//        String json = json(app);
//
//        when(appService.save(any())).thenReturn(app);
//
//        mvc
//                .perform(
//                        put("/app/" + app.getId())
//                                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                                .content(json))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//    }
//
//    @Test
//    public void deleteTest() throws Exception {
////        App app = App.of(UUID.randomUUID().toString(), "sandboxId", "clientName", "manifestUrl",
////                "clientId", new User(), new Timestamp(System.currentTimeMillis()),
////                Visibility2.PRIVATE, "samplePatients","info", "briefDesc", "author", CopyType.MASTER,
////                "launchUri", "logoUri", "clientUri", "fhirVersions", new Image(), "clienJSON");
//
//        String json = json(app);
//
//        doNothing().when(appService).delete(app);
//
//        mvc
//                .perform(delete("/app/" + app.getId()))
//                .andExpect(status().isOk());
//
//    }
//
//    @SuppressWarnings("unchecked")
//    private String json(Object o) throws IOException {
//        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
//        mappingJackson2HttpMessageConverter.write(
//                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
//        return mockHttpOutputMessage.getBodyAsString();
//    }
}