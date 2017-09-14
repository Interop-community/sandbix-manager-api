package org.hspconsortium.sandboxmanagerapi.controllers.dto;

/**
 * Used to send a subst of the persona info to the auth server when it is requesting details about a persona user.
 */
public class UserPersonaDto {
    private String username;
    private String name;
    private String resourceUrl;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }
}
