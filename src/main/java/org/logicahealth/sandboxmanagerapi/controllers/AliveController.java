package org.logicahealth.sandboxmanagerapi.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AliveController {

    @RequestMapping(value = "/alive", method = RequestMethod.GET)
    public String aliveEndpoint(){
        return "{\"status\": \"alive\"}";
    }
}
