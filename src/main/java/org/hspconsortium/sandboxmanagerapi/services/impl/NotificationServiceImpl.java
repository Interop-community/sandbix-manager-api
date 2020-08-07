package org.hspconsortium.sandboxmanagerapi.services.impl;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.model.Notification;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.repositories.NotificationRepository;
import org.hspconsortium.sandboxmanagerapi.services.NewsItemService;
import org.hspconsortium.sandboxmanagerapi.services.NotificationService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private NotificationRepository notificationRepository;
    private UserService userService;
    private NewsItemService newsItemService;

    @Inject
    public NotificationServiceImpl(final NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setNewsItemService(NewsItemService newsItemService) {
        this.newsItemService = newsItemService;
    }

    @Override
    public void createNotificationsForAllUsers(NewsItem newsItem) {
        NewsItem existingNewsItem = newsItemService.findById(newsItem.getId());
        if (existingNewsItem == null) {
            throw new ResourceNotFoundException("NewsItem does not exist.");
        }
        Iterable<User> users = userService.findAll();
        for (User user : users) {
            Notification notification = new Notification();
            notification.setSeen(false);
            notification.setHidden(false);
            notification.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            notification.setNewsItem(existingNewsItem);
            notification.setUser(user);
            save(notification);
        }
    }

    @Override
    public void createNotificationsForAGivenUser(NewsItem newsItem, User user){
        NewsItem existingNewsItem = newsItemService.findById(newsItem.getId());
        if (existingNewsItem == null) {
            throw new ResourceNotFoundException("NewsItem does not exist.");
        }
        Notification notification = new Notification();
        notification.setSeen(false);
        notification.setHidden(false);
        notification.setCreatedTimestamp(new Timestamp(new Date().getTime()));
        notification.setNewsItem(existingNewsItem);
        notification.setUser(user);
        save(notification);
    }

    @Override
    public Notification findById(Integer id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    public Notification update(Notification notification) {
        Notification existingNotification = findById(notification.getId());
        if (existingNotification != null) {
            existingNotification.setHidden(notification.getHidden());
            existingNotification.setSeen(notification.getSeen());
            return save(existingNotification);
        }
        throw new ResourceNotFoundException("Notification does not exist");
    }

    @Override
    public List<Notification> getAllNotificationsByUser(User user) {
        return notificationRepository.findByUserId(user.getId());
    }

    @Override
    public List<Notification> markAllNotificationsAsHiddenByUser(User user) {
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        for (Notification notification: notifications) {
            notification.setHidden(true);
            save(notification);
        }
        return notifications;
    }

    @Override
    public List<Notification> markAllNotificationsAsSeenByUser(User user) {
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        for (Notification notification: notifications) {
            notification.setSeen(true);
            save(notification);
        }
        return notifications;
    }

    @Override
    public void deleteNotificationForAllUsers(Integer newsItemId) {
        NewsItem newsItem = newsItemService.findById(newsItemId);
        if (newsItem == null) {
            throw new ResourceNotFoundException("NewsItem not found.");
        }
        List<Notification> notifications = notificationRepository.findByNewsItemId(newsItemId);
        for (Notification notification: notifications) {
            delete(notification.getId());
        }
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void delete(Integer id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public void createNotificationForMoreThanThresholdTransaction(User user){
        NewsItem newsItemTransaction = new NewsItem();
        newsItemTransaction.setTitle("Transactions more than threshold");
        newsItemTransaction.setDescription("Transactions are more than 90%");
        newsItemTransaction.setActive(1);
        newsItemService.save(newsItemTransaction);
        createNotificationsForAGivenUser(newsItemTransaction, user);
    }

    @Override
    public void createNotificationForMoreThanThresholdMemory(User user){
        NewsItem newsItemStorage = new NewsItem();
        newsItemStorage.setTitle("Used storage more than 90%");
        newsItemStorage.setDescription("Used Storage is more than 90%");
        newsItemStorage.setActive(1);
        newsItemService.save(newsItemStorage);
        createNotificationsForAGivenUser(newsItemStorage, user);
    }
}
