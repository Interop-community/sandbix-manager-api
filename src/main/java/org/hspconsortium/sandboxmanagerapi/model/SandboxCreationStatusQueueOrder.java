package org.hspconsortium.sandboxmanagerapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SandboxCreationStatusQueueOrder {
    private int queuePosition;
    private SandboxCreationStatus sandboxCreationStatus;
}
