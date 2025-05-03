package io.papermc.hangar.components.webhook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.components.webhook.dao.WebhookDAO;
import io.papermc.hangar.components.webhook.model.DiscordWebhook;
import io.papermc.hangar.components.webhook.model.event.ProjectEvent;
import io.papermc.hangar.components.webhook.model.event.ProjectPublishedEvent;
import io.papermc.hangar.components.webhook.model.event.VersionPublishedEvent;
import io.papermc.hangar.components.webhook.model.event.WebhookEvent;
import io.papermc.hangar.components.jobs.model.Job;
import io.papermc.hangar.components.jobs.model.SendWebhookJob;
import io.papermc.hangar.components.jobs.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    // TODO put these into a DB too
    private static final String discordProjectTemplate = """
        [
          {
            "id": {{id}},
            "title": "{{author}}/{{name}}",
            "description": "{{description}}",
            "color": 2326507,
            "fields": [],
            "url": "{{url}}",
            "thumbnail": {
              "url": "{{avatar}}"
            },
            "footer": {
              "text": "Platforms: {{platforms}}"
            }
          }
        ]
        """;

    private static final String discordVersionTemplate = """
        [
          {
            "id": {{id}},
            "title": "Version {{version}} - {{author}}/{{name}}",
            "description": "{{description}}",
            "color": 2326507,
            "fields": [],
            "url": "{{url}}",
            "thumbnail": {
              "url": "{{avatar}}"
            },
            "footer": {
              "text": "Platforms: {{platforms}}"
            }
          }
        ]
        """;

    private final RestTemplate restTemplate;
    private final JobService jobService;
    private final ObjectMapper objectMapper;
    private final WebhookDAO webhookDAO;

    public WebhookService(final RestTemplate restTemplate, final JobService jobService, final ObjectMapper objectMapper, final WebhookDAO webhookDAO) {
        this.restTemplate = restTemplate;
        this.jobService = jobService;
        this.objectMapper = objectMapper;
        this.webhookDAO = webhookDAO;
    }

    public void handleEvent(final WebhookEvent event) {
        final String payload;
        try {
            payload = this.objectMapper.writeValueAsString(event);
        } catch (final JsonProcessingException e) {
            logger.error("Can't serialize event {}", event, e);
            return;
        }

        this.jobService.schedule(
            this.webhookDAO.getGlobalWebhooksForType(event.getType())
                .stream().map(webhook -> new SendWebhookJob(String.valueOf(webhook.getId()), webhook.getUrl(), webhook.getType(), webhook.getSecret(), payload))
                .toArray(Job[]::new)
        );
    }

    public void sendWebhook(final SendWebhookJob webhook) {
        final WebhookEvent event;
        try {
            event = this.objectMapper.readValue(webhook.getPayload(), WebhookEvent.class);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize webhook payload", e);
        }

        final var payload = this.buildPayload(webhook.getType(), event);
        final var headers = new HttpHeaders();
        headers.set("User-Agent", "HangarWebhook (https://hangar.papermc.io, 1.0)");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // TODO handle secret
        final ResponseEntity<String> response = this.restTemplate.postForEntity(webhook.getUrl(), new HttpEntity<>(payload, headers), String.class);
        if (response.getStatusCode().isError()) {
            logger.warn("Failed to send webhook {} to {} with status code {}: {}", webhook.getId(), webhook.getUrl(), response.getStatusCode(), response.getBody());
        }
    }

    private Object buildPayload(final String type, final WebhookEvent event) {
        // TODO these need to be looked up from the DB/cache
        return switch (type) {
            case "discord_project" -> new DiscordWebhook(this.fillTemplate(discordProjectTemplate, event));
            case "discord_version" -> new DiscordWebhook(this.fillTemplate(discordVersionTemplate, event));
            case "rest" -> event;
            default -> throw new IllegalArgumentException("Unknown webhook type: " + type);
        };
    }

    // can prolly be improved by regexing thru stuff instead of looping multiple times, but oh well
    private String fillTemplate(String template, final WebhookEvent event) {
        template = template.replace("{{id}}", event.getId());

        if (event instanceof final ProjectEvent projectEvent) {
            template = template
                .replace("{{author}}", projectEvent.getAuthor())
                .replace("{{name}}", projectEvent.getName())
                .replace("{{url}}", projectEvent.getUrl())
                .replace("{{avatar}}", projectEvent.getAvatar())
                .replace("{{platforms}}", String.join(", ", projectEvent.getPlatforms()));
        }

        if (event instanceof final ProjectPublishedEvent projectPublishedEvent) {
            template = template.replace("{{description}}", projectPublishedEvent.getDescription());
        } else if (event instanceof final VersionPublishedEvent versionPublishedEvent) {
            template = template.replace("{{description}}", versionPublishedEvent.getDescription());
            template = template.replace("{{version}}", versionPublishedEvent.getVersion());
        }

        return template.replace("\n", "");
    }
}
