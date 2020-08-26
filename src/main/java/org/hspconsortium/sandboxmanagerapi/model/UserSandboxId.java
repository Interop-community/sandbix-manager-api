package org.hspconsortium.sandboxmanagerapi.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserSandboxId implements Serializable {
    private Integer userId;
    private Integer sandboxId;
}
