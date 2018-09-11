//package org.hspconsortium.sandboxmanagerapi.controllers;
//
//import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
//import org.hspconsortium.sandboxmanagerapi.model.User;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
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
//import org.springframework.web.util.NestedServletException;
//
//import javax.inject.Inject;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.util.Arrays;
//
//import static org.junit.Assert.assertNotNull;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.*;
//
//@RunWith(SpringRunner.class)
//public class AbstractControllerTest {
//
////    private HttpMessageConverter mappingJackson2HttpMessageConverter;
//
//
//
//    @MockBean
//    private OAuthService oAuthService;
//
//    private AbstractController abstractController = spy(new AbstractController(oAuthService));
//
////    @Autowired
////    void setConverters(HttpMessageConverter<?>[] converters) {
////
////        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
////                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
////                .findAny()
////                .orElse(null);
////
////        assertNotNull("the JSON message converter must not be null",
////                this.mappingJackson2HttpMessageConverter);
////    }
//
//    private User user;
//    private HttpServletRequest request;
//
//    @Before
//    public void setup() {
//        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
//        user = new User();
//        user.setSbmUserId("me");
//    }
//
//    @Test
//    public void checkUserAuthorizationTest() throws Exception {
//        abstractController.checkUserAuthorization(request, user.getSbmUserId());
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkUserAuthorizationTestNotAuthorized() throws Exception {
//        abstractController.checkUserAuthorization(request, user.getSbmUserId());
//    }
//
////    @SuppressWarnings("unchecked")
////    private String json(Object o) throws IOException {
////        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
////        mappingJackson2HttpMessageConverter.write(
////                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
////        return mockHttpOutputMessage.getBodyAsString();
////    }
//
//}
