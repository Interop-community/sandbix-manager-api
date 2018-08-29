package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.NewsItem;
import org.hspconsortium.sandboxmanagerapi.services.NewsItemService;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
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
    private OAuthService oAuthService;

    @MockBean
    private NewsItemService newsItemService;

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
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
        newsItem = new NewsItem();
        newsItem.setId(1);
        newsItemList = new ArrayList<>();
        newsItemList.add(newsItem);
    }

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

    @Test
    public void deleteNewsItemByIdTest() throws Exception {
        doNothing().when(newsItemService).delete(newsItem.getId());
        mvc
                .perform(delete("/newsItem/delete/" + newsItem.getId()))
                .andExpect(status().isOk());
    }

//    @Test
//    public void saveNewsItemTest() throws Exception {
//        String json = json(newsItem);
//        when(newsItemService.save(newsItem)).thenReturn(newsItem);
//        mvc
//                .perform(post("/newsItem/save")
//                        .contentType(MediaType.APPLICATION_JSON_UTF8)
//                        .content(json))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//    }
//
//    @Test
//    public void updateNewsItemTest() throws Exception {
//        String json = json(newsItem);
//        when(newsItemService.update(newsItem)).thenReturn(newsItem);
//        mvc
//                .perform(put("/newsItem/update/" + newsItem.getId())
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                        .content(json))
//                .andExpect(status().isOk())
////                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
