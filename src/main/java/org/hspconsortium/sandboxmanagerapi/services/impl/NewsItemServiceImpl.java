package org.hspconsortium.sandboxmanagerapi.services.impl;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.repositories.NewsItemRepository;
import org.hspconsortium.sandboxmanagerapi.services.NewsItemService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
    public NewsItem update(NewsItem newsItem) {
        NewsItem existingNewsItem = repository.findById(newsItem.getId()).orElse(null);
        if (existingNewsItem != null) {
            existingNewsItem.setActive(newsItem.getActive());
            existingNewsItem.setDescription(newsItem.getDescription());
            existingNewsItem.setLink(newsItem.getLink());
            existingNewsItem.setTitle(newsItem.getTitle());
            existingNewsItem.setType(newsItem.getType());
            existingNewsItem.setExpiration_date(newsItem.getExpiration_date());
            return repository.save(existingNewsItem);
        }
        throw new ResourceNotFoundException("NewsItem not found.");
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public List<NewsItem> findAll() {
        Iterable<NewsItem> newsItems = repository.findAll();
        List<NewsItem> target = new ArrayList<>();
        newsItems.forEach(target::add);
        return target;
    }

    @Override
    @Transactional
    public NewsItem findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

}
