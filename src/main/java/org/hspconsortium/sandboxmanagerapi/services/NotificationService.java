package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.model.Notification;
import org.hspconsortium.sandboxmanagerapi.model.User;

import java.util.List;

public interface NotificationService {

    Notification findById(Integer id);

    Notification update(Notification notification);

    List<Notification> getAllNotificationsByUser(User user);

    List<Notification> markAllNotificationsAsHiddenByUser(User user);

    List<Notification> markAllNotificationsAsSeenByUser(User user);

    void createNotificationsForAllUsers(NewsItem newsItem);

    void createNotificationsForAGivenUser(NewsItem newsItem, User user);

    void createNotificationForMoreThanThresholdTransaction(User user);

    void createNotificationForMoreThanThresholdMemory(User user);

    void deleteNotificationForAllUsers(Integer newsItemId);
}
