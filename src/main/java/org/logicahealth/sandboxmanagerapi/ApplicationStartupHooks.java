package org.logicahealth.sandboxmanagerapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.logicahealth.sandboxmanagerapi.services.SandboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@Component
public class ApplicationStartupHooks {
    private final SandboxService sandboxService;
    private static Logger LOGGER = LoggerFactory.getLogger(ApplicationStartupHooks.class.getName());

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        sandboxService.deleteQueuedSandboxes();
        LOGGER.info("Queued sandbox creation entries removed.");
    }

}
