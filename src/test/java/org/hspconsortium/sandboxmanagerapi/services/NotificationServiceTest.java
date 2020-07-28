package org.hspconsortium.sandboxmanagerapi.services;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.NotificationRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.NotificationServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificationServiceTest {

    private NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private UserService userService = mock(UserService.class);
    private NewsItemService newsItemService = mock(NewsItemService.class);

    private NotificationServiceImpl notificationService = new NotificationServiceImpl(notificationRepository);

    private Notification notification;
    private NewsItem newsItem;
    private User user;
    private Iterable<User> users;
    private List<Notification> notifications;

    @Before
    public void setup() {
        notificationService.setNewsItemService(newsItemService);
        notificationService.setUserService(userService);
        notification = new Notification();
        notification.setId(1);
        notification.setHidden(false);
        notification.setSeen(false);
        newsItem = new NewsItem();
        newsItem.setId(1);
        user = new User();
        user.setId(1);
        List<User> userList = new ArrayList<>();
        userList.add(user);
        users = userList;
        notifications = new ArrayList<>();
        notifications.add(notification);
    }

    @Test
    public void createNotificationsForAllUsersTest() {
        when(newsItemService.findById(newsItem.getId())).thenReturn(newsItem);
        when(userService.findAll()).thenReturn(users);
        notificationService.createNotificationsForAllUsers(newsItem);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void createNotificationsForAllUsersTestNotFound() {
        when(newsItemService.findById(newsItem.getId())).thenReturn(null);
        notificationService.createNotificationsForAllUsers(newsItem);
    }

    @Test
    public void createNotificationsForAGivenUserTest() {
        when(newsItemService.findById(newsItem.getId())).thenReturn(newsItem);
        when(userService.findAll()).thenReturn(users);
        notificationService.createNotificationsForAGivenUser(newsItem, user);
    }

    @Test
    public void findByIdTest() {
        when(notificationRepository.findById(newsItem.getId())).thenReturn(of(notification));
        Notification returnedNotification = notificationService.findById(newsItem.getId());
        assertEquals(notification, returnedNotification);
    }

    @Test
    public void updateTest() {
        Notification originalNotification = new Notification();
        originalNotification.setSeen(true);
        originalNotification.setHidden(true);
        when(notificationRepository.findById(newsItem.getId())).thenReturn(of(originalNotification));
        notificationService.update(notification);
        assertEquals(notification.getHidden(), originalNotification.getHidden());
        assertEquals(notification.getSeen(), originalNotification.getSeen());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void updateTestNotFound() {
        when(notificationRepository.findById(newsItem.getId())).thenReturn(Optional.empty());
        notificationService.update(notification);
    }

    @Test
    public void getAllNotificationsByUserTest() {
        when(notificationRepository.findByUserId(user.getId())).thenReturn(notifications);
        List<Notification> returnedNotifications = notificationService.getAllNotificationsByUser(user);
        assertEquals(notifications, returnedNotifications);
    }

    @Test
    public void markAllNotificationsAsSeenByUserTest() {
        when(notificationRepository.findByUserId(user.getId())).thenReturn(notifications);
        List<Notification> returnedNotifications = notificationService.markAllNotificationsAsSeenByUser(user);
        assertEquals(true, notifications.get(0).getSeen());
    }

    @Test
    public void markAllNotificationsAsHiddenByUserTest() {
        when(notificationRepository.findByUserId(user.getId())).thenReturn(notifications);
        List<Notification> returnedNotifications = notificationService.markAllNotificationsAsHiddenByUser(user);
        assertEquals(true, notifications.get(0).getHidden());
    }

    @Test
    public void deleteNotificationForAllUsersTest() {
        when(newsItemService.findById(newsItem.getId())).thenReturn(newsItem);
        when(notificationRepository.findByNewsItemId(newsItem.getId())).thenReturn(notifications).thenReturn(notifications);
        notificationService.deleteNotificationForAllUsers(newsItem.getId());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void deleteNotificationForAllUsersTestNewsItemNull() {
        when(newsItemService.findById(newsItem.getId())).thenReturn(null);
        when(notificationRepository.findByNewsItemId(newsItem.getId())).thenReturn(notifications).thenReturn(notifications);
        notificationService.deleteNotificationForAllUsers(newsItem.getId());
    }

    @Test
    public void saveTest() {
        when(notificationRepository.save(notification)).thenReturn(notification);
        Notification returnedNotification = notificationService.save(notification);
        assertEquals(notification, returnedNotification);
    }

    @Test
    public void deleteTest() {
        notificationService.delete(notification.getId());
        verify(notificationRepository).deleteById(notification.getId());
    }

}
