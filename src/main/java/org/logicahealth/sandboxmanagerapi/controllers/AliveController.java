package org.logicahealth.sandboxmanagerapi.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class AliveController {

    private static Logger LOGGER = LoggerFactory.getLogger(AliveController.class.getName());

    @RequestMapping(value = "/alive", method = RequestMethod.GET)
    public String aliveEndpoint(){
        
        LOGGER.info("aliveEndpoint");
        
        return "{\"status\": \"alive\"}";
    }
}
