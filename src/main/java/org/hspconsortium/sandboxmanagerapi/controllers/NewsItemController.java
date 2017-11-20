package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.services.NewsItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping({"/newsItem"})
public class NewsItemController {
    private static Logger LOGGER = LoggerFactory.getLogger(NewsItemController.class.getName());

    private final NewsItemService newsItemService;

    @Inject
    public NewsItemController(NewsItemService newsItemService){
        this.newsItemService = newsItemService;
    }

    @RequestMapping("/all")
    public @ResponseBody
    List<NewsItem> findAllNewsItems(HttpServletRequest request){
        return newsItemService.findAll();
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    @Transactional
    public void deleteNewsItemById(@PathVariable int id) {
        newsItemService.delete(id);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public void saveNewsItem(@RequestBody NewsItem newsItem) {

        newsItemService.save(newsItem);
    }


}
