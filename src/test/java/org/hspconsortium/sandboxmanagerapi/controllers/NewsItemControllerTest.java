package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.AuthorizationService;
import org.hspconsortium.sandboxmanagerapi.services.NewsItemService;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = NewsItemController.class, secure = false)
@ContextConfiguration(classes = NewsItemController.class)
public class NewsItemControllerTest {

    @Autowired
    private MockMvc mvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @MockBean
    private NewsItemService newsItemService;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private UserService userService;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    private NewsItem newsItem;
    private List<NewsItem> newsItemList;

    @Before
    public void setup() {
        User user = new User();
        user.setSbmUserId("userId");
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        newsItem = new NewsItem();
        newsItem.setId(1);
        newsItemList = new ArrayList<>();
        newsItemList.add(newsItem);
    }

    // TODO: Test when user = null and when user isn't an ADMIN

    @Test
    public void findAllNewsItemsTest() throws Exception {
        String json = json(newsItemList);
        when(newsItemService.findAll()).thenReturn(newsItemList);
        mvc
                .perform(get("/newsItem/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void findAllNewsItemsTestUserNull() throws Exception {
        String json = json(newsItemList);
        when(authorizationService.getSystemUserId(any())).thenReturn("");
        when(userService.findBySbmUserId("me")).thenReturn(null);
        when(newsItemService.findAll()).thenReturn(newsItemList);
        mvc
                .perform(get("/newsItem/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void deleteNewsItemByIdTest() throws Exception {
        doNothing().when(newsItemService).delete(newsItem.getId());
        mvc
                .perform(delete("/newsItem/delete/" + newsItem.getId()))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void deleteNewsItemByIdTestUserNull() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn("");
        when(userService.findBySbmUserId("me")).thenReturn(null);
        doNothing().when(newsItemService).delete(newsItem.getId());
        mvc
                .perform(delete("/newsItem/delete/" + newsItem.getId()))
                .andExpect(status().isOk());
    }

    @Test
    public void saveNewsItemTest() throws Exception {
        String json = json(newsItem);
        when(newsItemService.save(any())).thenReturn(newsItem);
        mvc
                .perform(post("/newsItem/save")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void saveNewsItemTestUserNull() throws Exception {
        String json = json(newsItem);
        when(authorizationService.getSystemUserId(any())).thenReturn("");
        when(userService.findBySbmUserId("me")).thenReturn(null);
        when(newsItemService.save(any())).thenReturn(newsItem);
        mvc
                .perform(post("/newsItem/save")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void updateNewsItemTest() throws Exception {
        String json = json(newsItem);
        when(newsItemService.update(any())).thenReturn(newsItem);
        mvc
                .perform(put("/newsItem/update/" + newsItem.getId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void updateNewsItemTestUserNull() throws Exception {
        String json = json(newsItem);
        when(authorizationService.getSystemUserId(any())).thenReturn("");
        when(userService.findBySbmUserId("me")).thenReturn(null);
        when(newsItemService.update(any())).thenReturn(newsItem);
        mvc
                .perform(put("/newsItem/update/" + newsItem.getId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
