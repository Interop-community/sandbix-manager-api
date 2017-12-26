package org.hspconsortium.sandboxmanagerapi.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/envinfo")
public class EnvInfoController {

    @Value("${envInfo.active:}")
    String active;

    @Value("${hspcEnv:}")
    String hspcEnv;

    @Value("${defaultServiceUrl:}")
    String defaultServiceUrl;

    @Value("${baseServiceUrl_1:}")
    String baseServiceUrl_1;

    @Value("${baseServiceUrl_2:}")
    String baseServiceUrl_2;

    @Value("${baseServiceUrl_3:}")
    String baseServiceUrl_3;

    @Value("${baseServiceUrl_4:}")
    String baseServiceUrl_4;

    @Value("${oauthLogoutUrl:}")
    String oauthLogoutUrl;

    @Value("${userManagementUrl:}")
    String userManagementUrl;

    @Value("${sbmUrlHasContextPath:}")
    String sbmUrlHasContextPath;

    @Value("${hostOrg:hspc}")
    String hostOrg;

    @Value("${oauthUserInfoUrl:}")
    String oauthUserInfoUrl;

    @Value("${hspcAccountCookieName:}")
    String hspcAccountCookieName;

    @Value("${personaCookieName:}")
    String personaCookieName;

    @Value("${personaCookieDomain:}")
    String personaCookieDomain;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public String get() {
        StringBuilder stringBuffer = new StringBuilder();

        stringBuffer.append("angular.module('sandManApp').constant('envInfo',");
        stringBuffer.append("{");
        stringBuffer.append("\"active\": \"");
        stringBuffer.append(active);
        stringBuffer.append("\",");
        stringBuffer.append("\"env\": \"");
        stringBuffer.append(hspcEnv);
        stringBuffer.append("\",");
        stringBuffer.append("\"defaultServiceUrl\": \"");
        stringBuffer.append(defaultServiceUrl);
        stringBuffer.append("\",");
        stringBuffer.append("\"baseServiceUrl_1\": \"");
        stringBuffer.append(baseServiceUrl_1);
        stringBuffer.append("\",");
        stringBuffer.append("\"baseServiceUrl_2\": \"");
        stringBuffer.append(baseServiceUrl_2);
        stringBuffer.append("\",");
        stringBuffer.append("\"baseServiceUrl_3\": \"");
        stringBuffer.append(baseServiceUrl_3);
        stringBuffer.append("\",");
        stringBuffer.append("\"baseServiceUrl_4\": \"");
        stringBuffer.append(baseServiceUrl_4);
        stringBuffer.append("\",");
        stringBuffer.append("\"oauthLogoutUrl\": \"");
        stringBuffer.append(oauthLogoutUrl);
        stringBuffer.append("\",");
        stringBuffer.append("\"userManagementUrl\": \"");
        stringBuffer.append(userManagementUrl);
        stringBuffer.append("\",");
        stringBuffer.append("\"sbmUrlHasContextPath\": \"");
        stringBuffer.append(sbmUrlHasContextPath);
        stringBuffer.append("\",");
        stringBuffer.append("\"hostOrg\": \"");
        stringBuffer.append(hostOrg);
        stringBuffer.append("\",");
        stringBuffer.append("\"oauthUserInfoUrl\": \"");
        stringBuffer.append(oauthUserInfoUrl);
        stringBuffer.append("\",");
        stringBuffer.append("\"hspcAccountCookieName\": \"");
        stringBuffer.append(hspcAccountCookieName);
        stringBuffer.append("\",");
        stringBuffer.append("\"personaCookieName\": \"");
        stringBuffer.append(personaCookieName);
        stringBuffer.append("\",");
        stringBuffer.append("\"personaCookieDomain\": \"");
        stringBuffer.append(personaCookieDomain);
        stringBuffer.append("\"");
        stringBuffer.append("});");

        return stringBuffer.toString();
    }

}