package org.hspconsortium.sandboxmanagerapi.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)
//@WebMvcTest(value = AliveController.class, secure = false)
//@ContextConfiguration(classes = AliveController.class)
public class AliveControllerTest {

//    @Autowired
//    private MockMvc mvc;
//
//    @Test
//    public void aliveEndpointTest() throws Exception {
//        String statement = "{\"status\": \"alive\"}";
//
//        mvc
//                .perform(get("/alive"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("text/plain;charset=UTF-8"))
//                .andExpect(content().string(statement));
//    }

}
