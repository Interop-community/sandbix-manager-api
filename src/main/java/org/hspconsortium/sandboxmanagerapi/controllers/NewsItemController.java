package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.model.SystemRole;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.AuthorizationService;
import org.hspconsortium.sandboxmanagerapi.services.NewsItemService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping({"/newsItem"})
public class NewsItemController {
    private final NewsItemService newsItemService;
    private final AuthorizationService authorizationService;
    private final UserService userService;

    @Inject
    public NewsItemController(NewsItemService newsItemService, AuthorizationService authorizationService,
                              UserService userService){
        this.newsItemService = newsItemService;
        this.authorizationService = authorizationService;
        this.userService = userService;
    }

    @GetMapping("all")
    public @ResponseBody
    List<NewsItem> findAllNewsItems(HttpServletRequest request){
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new UnauthorizedException("User not found in token.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return newsItemService.findAll();
    }

    @DeleteMapping(value = "/delete/{id}")
    @Transactional
    public void deleteNewsItemById(HttpServletRequest request, @PathVariable int id) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new UnauthorizedException("User not found in token.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        newsItemService.delete(id);
    }

    @PostMapping(value = "/save", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody NewsItem saveNewsItem(HttpServletRequest request, @RequestBody NewsItem newsItem) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new UnauthorizedException("User not found in token.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return newsItemService.save(newsItem);
    }

    @PutMapping(value = "/update/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody NewsItem updateNewsItem(HttpServletRequest request, @RequestBody NewsItem newsItem) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new UnauthorizedException("User not found in token.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return newsItemService.update(newsItem);
    }

}
