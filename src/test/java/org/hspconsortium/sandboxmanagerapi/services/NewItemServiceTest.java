package org.hspconsortium.sandboxmanagerapi.services;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.repositories.NewsItemRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.NewsItemServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static java.util.Optional.of;
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
        when(repository.findById(newsItem.getId())).thenReturn(of(newsItem));
        when(repository.save(newsItem)).thenReturn(newsItem);
        NewsItem returnedNewsItem = newsItemService.save(newsItem);
        assertEquals(newsItem, returnedNewsItem);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void updateTestNotFound() {
        when(repository.findById(newsItem.getId())).thenReturn(null);
        newsItemService.update(newsItem);
    }

    @Test
    public void deleteTest() {
        newsItemService.delete(newsItem.getId());
        verify(repository).deleteById(newsItem.getId());
    }

    @Test
    public void findAllTest() {
        when(repository.findAll()).thenReturn(Collections::emptyIterator);
        newsItemService.findAll();
        verify(repository).findAll();
    }

    @Test
    public void findByIdTest() {
        when(repository.findById(newsItem.getId())).thenReturn(of(newsItem));
        NewsItem returnedNewsItem = newsItemService.findById(newsItem.getId());
        assertEquals(newsItem, returnedNewsItem);
    }
}
