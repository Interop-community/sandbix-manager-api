package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.UserAccessHistory;
import org.hspconsortium.sandboxmanagerapi.model.UserRole;
import org.hspconsortium.sandboxmanagerapi.services.AuthorizationService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.UserAccessHistoryService;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = UserAccessHistoryController.class)
@ContextConfiguration(classes = UserAccessHistoryController.class)
public class UserAccessHistoryControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private UserAccessHistoryService userAccessHistoryService;

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

    private Sandbox sandbox;
    private User user;
    private UserAccessHistory userAccessHistory;
    private List<UserAccessHistory> userAccessHistoryList;

    @Before
    public void setup() {
        user = new User();
        sandbox = new Sandbox();
        user.setSbmUserId("me");
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole);
        sandbox.setUserRoles(userRoles);
//        sandbox.setVisibility(Visibility.PUBLIC);
        sandbox.setSandboxId("sandbox");
        userAccessHistory = new UserAccessHistory();
        userAccessHistoryList = new ArrayList<>();
        userAccessHistoryList.add(userAccessHistory);
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void getLastSandboxAccessWithSandboxIdTest() throws Exception {
        String json = json(userAccessHistoryList);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userAccessHistoryService.getLatestUserAccessHistoryInstancesWithSandbox(sandbox)).thenReturn(userAccessHistoryList);
        mvc.perform(get("/sandbox-access?sandboxId=" + sandbox.getSandboxId()))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getLastSandboxAccessWithSandboxIdTestSandboxNull() throws Exception {
        String json = json(userAccessHistoryList);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        when(userAccessHistoryService.getLatestUserAccessHistoryInstancesWithSandbox(sandbox)).thenReturn(userAccessHistoryList);
        mvc.perform(get("/sandbox-access?sandboxId=" + sandbox.getSandboxId()))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(content().json(json));
    }

    @Test
    public void getLastSandboxAccessWithSbmUserIdTest() throws Exception {
        String json = json(userAccessHistoryList);
        when(userService.findBySbmUserId(any())).thenReturn(user);
        when(userAccessHistoryService.getLatestUserAccessHistoryInstancesWithSbmUser(user)).thenReturn(userAccessHistoryList);
        mvc.perform(get("/sandbox-access?sbmUserId=" + user.getSbmUserId()))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getLastSandboxAccessWithSbmUserIdTestUserNull() throws Exception {
        String json = json(userAccessHistoryList);
        when(userService.findBySbmUserId(any())).thenReturn(null);
        when(userAccessHistoryService.getLatestUserAccessHistoryInstancesWithSbmUser(user)).thenReturn(userAccessHistoryList);
        mvc.perform(get("/sandbox-access?sbmUserId=" + user.getSbmUserId()))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(content().json(json));
    }

    @Test
    public void getLastSandboxAccessTest() throws Exception {
        Timestamp timestamp = new Timestamp(new Date().getTime());
        ZonedDateTime zonedDateTime = timestamp.toLocalDateTime().atZone(ZoneId.systemDefault());
        when(userService.findBySbmUserId(any())).thenReturn(user);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userAccessHistoryService.getLatestUserAccessHistoryInstance(sandbox, user)).thenReturn(timestamp);
        mvc.perform(get("/sandbox-access?sbmUserId=" + user.getSbmUserId() + "&sandboxId=" + sandbox.getSandboxId()))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content -> {
               String timestampString = timestamp.toInstant().toString();
               if (!content.getResponse().getContentAsString().contains(timestampString.substring(0, timestampString.length()-1))) {
                   throw new Exception();
               }
           });
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
