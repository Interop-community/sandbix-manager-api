package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.AuthorizationService;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.hspconsortium.sandboxmanagerapi.services.TermsOfUseService;
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

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = TermsOfUseController.class, secure = false)
@ContextConfiguration(classes = TermsOfUseController.class)
public class TermsOfUseControllerTest {

    @Autowired
    private MockMvc mvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @MockBean
    private TermsOfUseService termsOfUseService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthorizationService authorizationService;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    private TermsOfUse termsOfUse;
    private User user;

    @Before
    public void setup() {
        user = new User();
        user.setSbmUserId("userId");
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        termsOfUse = new TermsOfUse();
    }

    @Test
    public void getLatestTermsOfUseTest() throws Exception {
        when(termsOfUseService.mostRecent()).thenReturn(termsOfUse);
        mvc
                .perform(get("/termsofuse"))
                .andExpect(status().isOk());
    }

    @Test
    public void getLatestTermsOfUseTestNotFound() throws Exception {
        when(termsOfUseService.mostRecent()).thenReturn(null);
        mvc
                .perform(get("/termsofuse"))
                .andExpect(status().isNotFound());
    }

//    @Test
//    public void createTermsOfUseTest() throws Exception {
//        String json = json(termsOfUse);
//        User user = new User();
//        Set<SystemRole> systemRoleList = new HashSet<>();
//        systemRoleList.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoleList);
//        when(userService.findBySbmUserId(any())).thenReturn(user);
//        when(termsOfUseService.save(any())).thenReturn(termsOfUse);
//        mvc
//                .perform(post("/termsofuse")
//                        .contentType(MediaType.APPLICATION_JSON_UTF8)
//                        .content(json))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//    }

//    @Test(expected = NestedServletException.class)
//    public void createTermsOfUseTestUserNotFound() throws Exception {
//        String json = json(termsOfUse);
//        when(userService.findBySbmUserId(any())).thenReturn(null);
//        mvc
//                .perform(post("/termsofuse")
//                        .contentType(MediaType.APPLICATION_JSON_UTF8)
//                        .content(json));
//    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
