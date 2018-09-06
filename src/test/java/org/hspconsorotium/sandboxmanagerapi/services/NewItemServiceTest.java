package org.hspconsorotium.sandboxmanagerapi.services;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.repositories.NewsItemRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.NewsItemServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class NewItemServiceTest {

    private NewsItemRepository repository = mock(NewsItemRepository.class);

    private NewsItemServiceImpl newsItemService = new NewsItemServiceImpl(repository);

    private NewsItem newsItem;

    @Before
    public void setup() {
        newsItem = new NewsItem();
        newsItem.setId(1);
    }

    @Test
    public void saveTest() {
        when(repository.save(newsItem)).thenReturn(newsItem);
        NewsItem returnedNewsItem = newsItemService.save(newsItem);
        assertEquals(newsItem, returnedNewsItem);
    }

    @Test
    public void updateTest() {
        when(repository.findOne(newsItem.getId())).thenReturn(newsItem);
        when(repository.save(newsItem)).thenReturn(newsItem);
        NewsItem returnedNewsItem = newsItemService.save(newsItem);
        assertEquals(newsItem, returnedNewsItem);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void updateTestNotFound() {
        when(repository.findOne(newsItem.getId())).thenReturn(null);
        newsItemService.update(newsItem);
    }

    @Test
    public void deleteTest() {
        newsItemService.delete(newsItem.getId());
        verify(repository).delete(newsItem.getId());
    }

    @Test
    public void findAllTest() {
        when(repository.findAll()).thenReturn(new Iterable<NewsItem>() {
            @Override
            public Iterator<NewsItem> iterator() {
                return Collections.emptyIterator();
            }
        });
        newsItemService.findAll();
        verify(repository).findAll();
    }

    @Test
    public void findByIdTest() {
        when(repository.findOne(newsItem.getId())).thenReturn(newsItem);
        NewsItem returnedNewsItem = newsItemService.findById(newsItem.getId());
        assertEquals(newsItem, returnedNewsItem);
    }
}
