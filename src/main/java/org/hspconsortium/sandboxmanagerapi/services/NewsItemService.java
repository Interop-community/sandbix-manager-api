package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.NewsItem;

import java.util.List;

public interface NewsItemService {

    NewsItem save(NewsItem newsItem);

    void delete(int id);

    List<NewsItem> findAll();

}
