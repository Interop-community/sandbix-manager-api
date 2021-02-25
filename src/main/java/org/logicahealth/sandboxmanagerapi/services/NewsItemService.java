package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.NewsItem;

import java.util.List;

public interface NewsItemService {

    NewsItem save(NewsItem newsItem);

    NewsItem update(NewsItem newsItem);

    void delete(int id);

    List<NewsItem> findAll();

    NewsItem findById(Integer id);

}
