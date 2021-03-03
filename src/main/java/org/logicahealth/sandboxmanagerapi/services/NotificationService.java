package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.NewsItem;
import org.logicahealth.sandboxmanagerapi.model.Notification;
import org.logicahealth.sandboxmanagerapi.model.User;

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

    void delete(List<User> invitees);
}
