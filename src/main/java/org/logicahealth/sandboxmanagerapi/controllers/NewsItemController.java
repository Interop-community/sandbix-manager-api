package org.logicahealth.sandboxmanagerapi.controllers;

import org.logicahealth.sandboxmanagerapi.model.NewsItem;
import org.logicahealth.sandboxmanagerapi.model.SystemRole;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.services.AuthorizationService;
import org.logicahealth.sandboxmanagerapi.services.NewsItemService;
import org.logicahealth.sandboxmanagerapi.services.UserService;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping({"/newsItem"})
public class NewsItemController {
    private static Logger LOGGER = LoggerFactory.getLogger(NewsItemController.class.getName());
    
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
        
        LOGGER.info("findAllNewsItems");
        
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
        
        LOGGER.info("deleteNewsItemById");
        
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new UnauthorizedException("User not found in token.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        newsItemService.delete(id);
    }

    @PostMapping(value = "/save", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody NewsItem saveNewsItem(HttpServletRequest request, @RequestBody NewsItem newsItem) {
        
        LOGGER.info("saveNewsItem");
        
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
        
        LOGGER.info("updateNewsItem");
        
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new UnauthorizedException("User not found in token.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return newsItemService.update(newsItem);
    }

}
