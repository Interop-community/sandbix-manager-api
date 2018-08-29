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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping({"/newsItem"})
public class NewsItemController {
    private final NewsItemService newsItemService;

    //TODO implement security
    @Inject
    public NewsItemController(NewsItemService newsItemService){
        this.newsItemService = newsItemService;
    }

    @GetMapping("all")
    public @ResponseBody
    List<NewsItem> findAllNewsItems(HttpServletRequest request){
        return newsItemService.findAll();
    }

    @DeleteMapping(value = "/delete/{id}")
    @Transactional
    public void deleteNewsItemById(@PathVariable int id) {
        newsItemService.delete(id);
    }

    @PostMapping(value = "/save", produces = APPLICATION_JSON_VALUE)
    public NewsItem saveNewsItem(@RequestBody NewsItem newsItem) {
        return newsItemService.save(newsItem);
    }

    @PutMapping(value = "/update/{id}", produces = APPLICATION_JSON_VALUE)
    public NewsItem updateNewsItem(@RequestBody NewsItem newsItem) {
        return newsItemService.update(newsItem);
    }


}
