package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.AuthorizationService;
import org.hspconsortium.sandboxmanagerapi.services.NotificationService;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = NotificationController.class, secure = false)
@ContextConfiguration(classes = NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AuthorizationService authorizationService;

    @Autowired
    private NotificationController notificationController = new NotificationController(notificationService, userService, authorizationService);

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
    private Notification notification;
    private List<Notification> notifications;
    private MockHttpServletRequest request;
    private AbstractSandboxItem abstractSandboxItem = mock(AbstractSandboxItem.class);

    @Before
    public void setup() {
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
        sandbox = new Sandbox();
        sandbox.setSandboxId("sandbox");
        user = new User();
        user.setSbmUserId("me");
        notification = new Notification();
        notification.setId(1);
        notifications = new ArrayList<>();
        notifications.add(notification);
        Set<SystemRole> systemRoles = new HashSet<>();
        systemRoles.add(SystemRole.ADMIN);
        user.setSystemRoles(systemRoles);

        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
    }

    @Test
    public void getAllNotificationsByUserTest() throws Exception {
        String json = json(notifications);
        when(notificationService.getAllNotificationsByUser(any())).thenReturn(notifications);
        mvc
                .perform(get("/notification?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void createNotificationsForAllUsersTest() throws Exception {
        NewsItem newsItem = new NewsItem();
        String json = json(newsItem);
        mvc
                .perform(post("/notification?userId=" + user.getSbmUserId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
        verify(notificationService).createNotificationsForAllUsers(any());
    }

    @Test(expected = NestedServletException.class)
    public void createNotificationsForAllUsersTestUserUnauthorized() throws Exception {
        NewsItem newsItem = new NewsItem();
        String json = json(newsItem);
        user.setSystemRoles(new HashSet<>());
        mvc
                .perform(post("/notification?userId=" + user.getSbmUserId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json));
    }

    @Test
    public void markAllNotificationsAsHiddenByUserTest() throws Exception {
        String json = json(notifications);
        when(notificationService.markAllNotificationsAsHiddenByUser(any())).thenReturn(notifications);
        mvc
                .perform(put("/notification/mark-hidden?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void markAllNotificationsAsSeenByUserTest() throws Exception {
        String json = json(notifications);
        when(notificationService.markAllNotificationsAsSeenByUser(any())).thenReturn(notifications);
        mvc
                .perform(put("/notification/mark-seen?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void updateNotificationTest() throws  Exception {
        String json = json(notification);
        when(notificationService.update(notification)).thenReturn(notification);
        mvc
                .perform(put("/notification/" + notification.getId() + "?userId=" + user.getSbmUserId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void deleteNotificationForAllUsersTest() throws Exception {
        doNothing().when(notificationService).deleteNotificationForAllUsers(any());
        mvc
                .perform(delete("/notification?userId=" + user.getSbmUserId() + "&newsItemId=1"))
                .andExpect(status().isOk());
        verify(notificationService).deleteNotificationForAllUsers(any());
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
