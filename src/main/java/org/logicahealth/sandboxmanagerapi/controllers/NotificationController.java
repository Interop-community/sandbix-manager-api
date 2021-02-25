package org.logicahealth.sandboxmanagerapi.controllers;

import org.logicahealth.sandboxmanagerapi.model.NewsItem;
import org.logicahealth.sandboxmanagerapi.model.Notification;
import org.logicahealth.sandboxmanagerapi.model.SystemRole;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.services.AuthorizationService;
import org.logicahealth.sandboxmanagerapi.services.NotificationService;
import org.logicahealth.sandboxmanagerapi.services.UserService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping({"/notification"})
public class NotificationController {

    private UserService userService;
    private NotificationService notificationService;
    private AuthorizationService authorizationService;

    @Inject
    NotificationController(final NotificationService notificationService,
                           final UserService userService, final AuthorizationService authorizationService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @GetMapping(value = "", params = {"userId"})
    public List<Notification> getAllNotificationsByUser(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        return notificationService.getAllNotificationsByUser(user);
    }

    @PostMapping(value = "", params = {"userId"})
    public void createNotificationsForAllUsers(HttpServletRequest request, @RequestParam(value = "userId") String userId, @RequestBody final NewsItem newsItem) {
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        notificationService.createNotificationsForAllUsers(newsItem);
    }

    @PutMapping(value = "/mark-hidden", params = {"userId"})
    public List<Notification> markAllNotificationsAsHiddenByUser(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        return notificationService.markAllNotificationsAsHiddenByUser(user);
    }

    @PutMapping(value = "/mark-seen", params = {"userId"})
    public List<Notification> markAllNotificationsAsSeenByUser(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        return notificationService.markAllNotificationsAsSeenByUser(user);
    }

    @PutMapping(value = "/{id}", params = {"userId"})
    public Notification updateNotification(HttpServletRequest request, @PathVariable Integer id, @RequestParam(value = "userId") String userIdEncoded, @RequestBody final Notification notification) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        return notificationService.update(notification);
    }

    @DeleteMapping(value = "", params = {"userId", "newsItemId"})
    public void deleteNotificationForAllUsers(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded, @RequestParam(value = "newsItemId") Integer newsItemId) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        notificationService.deleteNotificationForAllUsers(newsItemId);
    }

//    @DeleteMapping(value = "/{id}", params = {"userId"})
//    public void deleteNotification(HttpServletRequest request, @PathVariable Integer id, @RequestParam(value = "userId") String userIdEncoded, @RequestBody final Notification notification) throws UnsupportedEncodingException {
//        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
//        checkUserAuthorization(request, userId);
//
//    }

}
