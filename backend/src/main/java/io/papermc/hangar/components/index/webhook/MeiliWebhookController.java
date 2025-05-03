package io.papermc.hangar.components.index.webhook;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/meili/webhook")
public class MeiliWebhookController {

    private final Logger logger = LoggerFactory.getLogger(MeiliWebhookController.class);

    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public void handle(@RequestBody List<Webhook> webhooks) {
        for (final Webhook webhook : webhooks) {
            if (webhook.status().equals("succeeded")) {
                logger.info("Webhook succeeded: {}", webhook);
            } else if (webhook.status().equals("failed")) {
                logger.error("Webhook failed: {}", webhook);
            } else {
                logger.warn("Webhook status unknown: {}", webhook);
            }
        }
    }
}

