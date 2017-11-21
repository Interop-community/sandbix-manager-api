package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.repositories.NewsItemRepository;
import org.hspconsortium.sandboxmanagerapi.services.NewsItemService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsItemServiceImpl implements NewsItemService {

    private final NewsItemRepository repository;

    @Inject
    public NewsItemServiceImpl(final NewsItemRepository repository){
        this.repository = repository;
    }

    @Override
    @Transactional
    public NewsItem save(final NewsItem newsItem) {
        return repository.save(newsItem);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public List<NewsItem> findAll() {
        Iterable<NewsItem> newsItems = repository.findAll();
        List<NewsItem> target = new ArrayList<>();
        newsItems.forEach(target::add);
        return target;
    }

}
