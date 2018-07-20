package org.hspconsortium.sandboxmanagerapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class SmartAppCompositeId implements Serializable {
    private String smartAppId;
    private String sandboxId;
}
