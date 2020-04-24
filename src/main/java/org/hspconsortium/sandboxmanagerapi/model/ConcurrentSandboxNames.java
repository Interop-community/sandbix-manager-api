package org.hspconsortium.sandboxmanagerapi.model;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name="ConcurrentSandboxNames.findAllSDsforAProfileByFhirProfileId",
                query="SELECT c FROM ConcurrentSandboxNames c")
})
public class ConcurrentSandboxNames {

    private String sandboxName;

    public String getSandboxName() {
        return sandboxName;
    }

    public void setSandboxName(String sandboxName) {
        this.sandboxName = sandboxName;
    }
}
