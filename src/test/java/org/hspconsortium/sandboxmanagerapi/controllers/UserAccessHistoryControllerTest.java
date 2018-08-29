//package org.hspconsortium.sandboxmanagerapi.controllers;
//
//import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.mock.http.MockHttpOutputMessage;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.io.IOException;
//import java.util.Arrays;
//
//import static org.junit.Assert.assertNotNull;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.when;
//
//@RunWith(SpringRunner.class)
//@WebMvcTest(value = UserAccessHistoryController.class, secure = false)
//@ContextConfiguration(classes = UserAccessHistoryController.class)
//public class UserAccessHistoryControllerTest {
//
//    @Autowired
//    private MockMvc mvc;
//
//    private HttpMessageConverter mappingJackson2HttpMessageConverter;
//
//    @MockBean
//    private OAuthService oAuthService;
//
//    @Autowired
//    void setConverters(HttpMessageConverter<?>[] converters) {
//
//        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
//                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
//                .findAny()
//                .orElse(null);
//
//        assertNotNull("the JSON message converter must not be null",
//                this.mappingJackson2HttpMessageConverter);
//    }
//
//    @Before
//    public void setup() {
//        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
//    }
//
//    @Test
//    public void getUserTest() throws Exception {
//
//    }
//
//
//    @SuppressWarnings("unchecked")
//    private String json(Object o) throws IOException {
//        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
//        mappingJackson2HttpMessageConverter.write(
//                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
//        return mockHttpOutputMessage.getBodyAsString();
//    }
//}
