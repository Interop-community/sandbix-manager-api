package org.logicahealth.sandboxmanagerapi.services.impl;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.logicahealth.sandboxmanagerapi.model.NewsItem;
import org.logicahealth.sandboxmanagerapi.repositories.NewsItemRepository;
import org.logicahealth.sandboxmanagerapi.services.NewsItemService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NewsItemServiceImpl implements NewsItemService {
    private static Logger LOGGER = LoggerFactory.getLogger(NewsItemServiceImpl.class.getName());

    private final NewsItemRepository repository;

    @Inject
    public NewsItemServiceImpl(final NewsItemRepository repository){
        this.repository = repository;
    }

    @Override
    @Transactional
    public NewsItem save(final NewsItem newsItem) {
        
        LOGGER.info("Inside NewsItemServiceImpl - save");

        NewsItem retVal = repository.save(newsItem);

        LOGGER.debug("Inside NewsItemServiceImpl - save: "
        +"Parameters: newsItem = "+newsItem+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public NewsItem update(NewsItem newsItem) {
        
        LOGGER.info("Inside NewsItemServiceImpl - update");

        NewsItem existingNewsItem = repository.findById(newsItem.getId()).orElse(null);
        if (existingNewsItem != null) {
            existingNewsItem.setActive(newsItem.getActive());
            existingNewsItem.setDescription(newsItem.getDescription());
            existingNewsItem.setLink(newsItem.getLink());
            existingNewsItem.setTitle(newsItem.getTitle());
            existingNewsItem.setType(newsItem.getType());
            existingNewsItem.setExpiration_date(newsItem.getExpiration_date());

            NewsItem retVal = repository.save(existingNewsItem);

            LOGGER.debug("Inside NewsItemServiceImpl - update: "
            +"Parameters: newsItem = "+newsItem+"; Return value = "+retVal);

            return retVal;
        }
        throw new ResourceNotFoundException("NewsItem not found.");
    }

    @Override
    @Transactional
    public void delete(final int id) {
        
        LOGGER.info("Inside NewsItemServiceImpl - delete");

        repository.deleteById(id);

        LOGGER.debug("Inside NewsItemServiceImpl - delete: "
        +"Parameters: id = "+id+"; No return value");

    }

    @Override
    @Transactional
    public List<NewsItem> findAll() {
        
        LOGGER.info("Inside NewsItemServiceImpl - findAll");

        Iterable<NewsItem> newsItems = repository.findAll();
        List<NewsItem> target = new ArrayList<>();
        newsItems.forEach(target::add);

        LOGGER.debug("Inside NewsItemServiceImpl - findAll: "
        +"No input parameters; Return value = "+target);

        return target;
    }

    @Override
    @Transactional
    public NewsItem findById(Integer id) {

        LOGGER.info("Inside NewsItemServiceImpl - findById");

        LOGGER.debug("Inside NewsItemServiceImpl - findById: "
        +"Parameters: id = "+id+"; Return value = "+repository.findById(id).orElse(null));

        return repository.findById(id).orElse(null);
    }

}
