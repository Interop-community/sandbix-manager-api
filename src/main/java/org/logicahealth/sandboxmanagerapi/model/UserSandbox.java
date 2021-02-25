package org.logicahealth.sandboxmanagerapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@IdClass(UserSandboxId.class)
public class UserSandbox implements Serializable {
    @Id
    @Column(name="user_id")
    private Integer userId;
    @Id
    @Column(name="sandbox_id")
    private Integer sandboxId;
}
