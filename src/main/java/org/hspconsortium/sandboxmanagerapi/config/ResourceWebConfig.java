package org.hspconsortium.sandboxmanagerapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
//@ComponentScan({"org.hspconsortium"})
public class ResourceWebConfig extends WebMvcConfigurerAdapter {}
