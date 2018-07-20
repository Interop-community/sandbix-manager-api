package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.CopyType;
import org.hspconsortium.sandboxmanagerapi.model.SmartApp;
import org.hspconsortium.sandboxmanagerapi.model.Visibility2;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.hspconsortium.sandboxmanagerapi.services.SmartAppService;
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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = SmartAppController.class, secure = false)
@ContextConfiguration(classes = SmartAppController.class)
public class SmartAppControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SmartAppService smartAppService;

    @MockBean
    private OAuthService oAuthService;

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
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
    }

    @Test
    public void getNotFoundTest() throws Exception {
        when(smartAppService.getById("not-found", "sandboxId")).thenReturn(null);

        mvc
                .perform(get("/smartapp/not-found"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getFoundTest() throws Exception {
        SmartApp smartApp = SmartApp.of(UUID.randomUUID().toString(), "sandboxId", "manifestUrl",
                "manifest", "clientId", 10, new Timestamp(System.currentTimeMillis()),
                Visibility2.PRIVATE, "samplePatients", "info", "briefDesc", "author", CopyType.MASTER);

        String json = json(smartApp);

        when(smartAppService.getById(smartApp.getSmartAppId(), "sandboxId")).thenReturn(smartApp);

        mvc
                .perform(get("/smartapp/found"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
        ;
    }

    @Test
    public void saveTest() throws Exception {
        SmartApp smartApp = SmartApp.of(UUID.randomUUID().toString(), "sandboxId", "manifestUrl",
                "manifest", "clientId", 10, new Timestamp(System.currentTimeMillis()),
                Visibility2.PRIVATE, "samplePatients","info", "briefDesc", "author", CopyType.MASTER);

        String json = json(smartApp);

        when(smartAppService.save(any())).thenReturn(smartApp);

        mvc
                .perform(
                        put("/smartapp/" + smartApp.getSmartAppId())
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void deleteTest() throws Exception {
        SmartApp smartApp = SmartApp.of(UUID.randomUUID().toString(), "sandboxId", "manifestUrl",
                "manifest", "clientId", 10, new Timestamp(System.currentTimeMillis()),
                Visibility2.PRIVATE, "samplePatients","info", "briefDesc", "author", CopyType.MASTER);

        String json = json(smartApp);

        doNothing().when(smartAppService).delete(smartApp);

        mvc
                .perform(delete("/smartapp/" + smartApp.getSmartAppId()))
                .andExpect(status().isOk());

    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}