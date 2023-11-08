package org.logicahealth.sandboxmanagerapi.services.impl;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.logicahealth.sandboxmanagerapi.model.NewsItem;
import org.logicahealth.sandboxmanagerapi.model.Notification;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.repositories.NotificationRepository;
import org.logicahealth.sandboxmanagerapi.services.NewsItemService;
import org.logicahealth.sandboxmanagerapi.services.NotificationService;
import org.logicahealth.sandboxmanagerapi.services.UserService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class.getName());

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
        
        LOGGER.info("createNotificationsForAllUsers");

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

        LOGGER.debug("createNotificationsForAllUsers: "
        +"Parameters: newsItem = "+newsItem+"; No return value");

    }

    @Override
    public void createNotificationsForAGivenUser(NewsItem newsItem, User user){
        
        LOGGER.info("createNotificationsForAGivenUser");

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

        LOGGER.debug("createNotificationsForAGivenUser: "
        +"Parameters: newsItem = "+newsItem+", user = "+user+"; No return value");

    }

    @Override
    public Notification findById(Integer id) {

        LOGGER.info("findById");

        LOGGER.debug("findById: "
        +"Parameters: id = "+id+"; Return value = "+notificationRepository.findById(id).orElse(null));

        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    public Notification update(Notification notification) {
        
        LOGGER.info("update");

        Notification existingNotification = findById(notification.getId());
        if (existingNotification != null) {
            existingNotification.setHidden(notification.getHidden());
            existingNotification.setSeen(notification.getSeen());

            Notification retVal = save(existingNotification);

            LOGGER.debug("update: "
            +"Parameters: notification = "+notification+"; Return value = "+retVal);

            return retVal;
        }
        throw new ResourceNotFoundException("Notification does not exist");
    }

    @Override
    public List<Notification> getAllNotificationsByUser(User user) {
        
        LOGGER.info("getAllNotificationsByUser");

        LOGGER.debug("getAllNotificationsByUser: "
        +"Parameters: user = "+user+"; Return value = "+notificationRepository.findByUserId(user.getId()));

        return notificationRepository.findByUserId(user.getId());
    }

    @Override
    public List<Notification> markAllNotificationsAsHiddenByUser(User user) {
        
        LOGGER.info("markAllNotificationsAsHiddenByUser");

        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        for (Notification notification: notifications) {
            notification.setHidden(true);
            save(notification);
        }

        LOGGER.debug("markAllNotificationsAsHiddenByUser: "
        +"Parameters: user = "+user+"; Return value = "+notifications);

        return notifications;
    }

    @Override
    public List<Notification> markAllNotificationsAsSeenByUser(User user) {
        
        LOGGER.info("markAllNotificationsAsSeenByUser");

        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        for (Notification notification: notifications) {
            notification.setSeen(true);
            save(notification);
        }

        LOGGER.debug("markAllNotificationsAsSeenByUser: "
        +"Parameters: user = "+user+"; Return value = "+notifications);

        return notifications;
    }

    @Override
    public void deleteNotificationForAllUsers(Integer newsItemId) {
        
        LOGGER.info("deleteNotificationForAllUsers");

        NewsItem newsItem = newsItemService.findById(newsItemId);
        if (newsItem == null) {
            throw new ResourceNotFoundException("NewsItem not found.");
        }
        List<Notification> notifications = notificationRepository.findByNewsItemId(newsItemId);
        for (Notification notification: notifications) {
            delete(notification.getId());
        }

        LOGGER.debug("deleteNotificationForAllUsers: "
        +"Parameters: newsItemId = "+newsItemId+"; No return value");

    }

    public Notification save(Notification notification) {
        
        LOGGER.info("save");

        Notification retVal = notificationRepository.save(notification);

        LOGGER.debug("save: "
        +"Parameters: notification = "+notification+"; Return value = "+retVal);

        return retVal;
    }

    public void delete(Integer id) {

        LOGGER.info("delete");

        notificationRepository.deleteById(id);

        LOGGER.debug("delete: "
        +"Parameters: id = "+id+"; No return value");

    }

    @Override
    public void createNotificationForMoreThanThresholdTransaction(User user){
        
        LOGGER.info("createNotificationForMoreThanThresholdTransaction");

        NewsItem newsItemTransaction = new NewsItem();
        newsItemTransaction.setTitle("Transactions more than threshold");
        newsItemTransaction.setDescription("Transactions are more than 90%");
        newsItemTransaction.setActive(1);
        newsItemService.save(newsItemTransaction);
        createNotificationsForAGivenUser(newsItemTransaction, user);

        LOGGER.debug("createNotificationForMoreThanThresholdTransaction: "
        +"Parameters: user = "+user+"; No return value");

    }

    @Override
    public void createNotificationForMoreThanThresholdMemory(User user){
        
        LOGGER.info("createNotificationForMoreThanThresholdMemory");

        NewsItem newsItemStorage = new NewsItem();
        newsItemStorage.setTitle("Used storage more than 90%");
        newsItemStorage.setDescription("Used Storage is more than 90%");
        newsItemStorage.setActive(1);
        newsItemService.save(newsItemStorage);
        createNotificationsForAGivenUser(newsItemStorage, user);

        LOGGER.debug("createNotificationForMoreThanThresholdMemory: "
        +"Parameters: user = "+user+"; No return value");

    }

    @Override
    @Transactional
    public void delete(List<User> invitees) {

        LOGGER.info("delete");

        notificationRepository.deleteAllByUserIn(invitees);

        LOGGER.debug("delete: "
        +"Parameters: "+invitees+"; No return value");

    }
}
