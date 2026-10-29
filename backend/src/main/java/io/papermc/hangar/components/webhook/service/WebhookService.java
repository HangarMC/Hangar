package io.papermc.hangar.components.webhook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.papermc.hangar.components.webhook.dao.WebhookDAO;
import io.papermc.hangar.components.webhook.model.DiscordWebhook;
import io.papermc.hangar.components.webhook.model.event.ProjectEvent;
import io.papermc.hangar.components.webhook.model.event.ProjectFlaggedEvent;
import io.papermc.hangar.components.webhook.model.event.ProjectPublishedEvent;
import io.papermc.hangar.components.webhook.model.event.VersionPublishedEvent;
import io.papermc.hangar.components.webhook.model.event.WebhookEvent;
import io.papermc.hangar.components.jobs.model.Job;
import io.papermc.hangar.components.jobs.model.SendWebhookJob;
import io.papermc.hangar.components.jobs.JobService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");

    // TODO put these into a DB too
    private static final String discordProjectTemplateSource = """
        [
          {
            "id": "{{id}}",
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

    private static final String discordVersionTemplateSource = """
        [
          {
            "id": "{{id}}",
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

    private static final String discordProjectFlagTemplateSource = """
        [
          {
            "id": "{{id}}",
            "title": "Project {{author}}/{{name}} flagged for {{reason}}",
            "color": 2326507,
            "description": "{{comment}}",
            "author": {
              "name": "{{flaggerName}}",
              "url": "{{flaggerUrl}}"
            },
            "url": "{{flagUrl}}"
          }
        ]
        """;

    private final JsonNode discordProjectTemplate;
    private final JsonNode discordVersionTemplate;
    private final JsonNode discordProjectFlagTemplate;
    private final RestTemplate restTemplate;
    private final JobService jobService;
    private final ObjectMapper objectMapper;
    private final WebhookDAO webhookDAO;

    public WebhookService(final RestTemplate restTemplate, final JobService jobService, final ObjectMapper objectMapper, final WebhookDAO webhookDAO) {
        this.restTemplate = restTemplate;
        this.jobService = jobService;
        this.objectMapper = objectMapper;
        this.webhookDAO = webhookDAO;
        this.discordProjectTemplate = parseTemplate(objectMapper, discordProjectTemplateSource);
        this.discordVersionTemplate = parseTemplate(objectMapper, discordVersionTemplateSource);
        this.discordProjectFlagTemplate = parseTemplate(objectMapper, discordProjectFlagTemplateSource);
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
            case "discord_project" -> new DiscordWebhook(this.fillTemplate(this.discordProjectTemplate, event));
            case "discord_version" -> new DiscordWebhook(this.fillTemplate(this.discordVersionTemplate, event));
            case "discord_project_flag"-> new DiscordWebhook(this.fillTemplate(this.discordProjectFlagTemplate, event));
            case "rest" -> event;
            default -> throw new IllegalArgumentException("Unknown webhook type: " + type);
        };
    }

    private static JsonNode parseTemplate(final ObjectMapper objectMapper, final String source) {
        try {
            return objectMapper.readTree(source);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Invalid webhook template", e);
        }
    }

    private JsonNode fillTemplate(final JsonNode template, final WebhookEvent event) {
        final Map<String, String> values = new HashMap<>();
        values.put("id", event.getId());

        if (event instanceof final ProjectEvent projectEvent) {
            values.put("author", projectEvent.getAuthor());
            values.put("name", projectEvent.getName());
            values.put("url", projectEvent.getUrl());
            values.put("avatar", projectEvent.getAvatar());
            values.put("platforms", String.join(", ", projectEvent.getPlatforms()));
        }

        if (event instanceof final ProjectPublishedEvent projectPublishedEvent) {
            values.put("description", projectPublishedEvent.getDescription());
        } else if (event instanceof final VersionPublishedEvent versionPublishedEvent) {
            values.put("description", versionPublishedEvent.getDescription());
            values.put("version", versionPublishedEvent.getVersion());
        } else if (event instanceof final ProjectFlaggedEvent projectFlaggedEvent) {
            values.put("flaggerName", projectFlaggedEvent.getFlaggerName());
            values.put("flaggerUrl", projectFlaggedEvent.getFlaggerUrl());
            values.put("flagUrl", projectFlaggedEvent.getFlagUrl());
            values.put("reason", projectFlaggedEvent.getReason());
            values.put("comment", projectFlaggedEvent.getComment());
        }

        return this.substitute(template, values);
    }

    // values are only ever inserted as string nodes, so they can never alter the structure of the template
    private JsonNode substitute(final JsonNode node, final Map<String, String> values) {
        if (node.isTextual()) {
            final Matcher matcher = PLACEHOLDER.matcher(node.asText());
            return new TextNode(matcher.replaceAll(match -> Matcher.quoteReplacement(Objects.toString(values.get(match.group(1)), ""))));
        } else if (node.isObject()) {
            final ObjectNode result = this.objectMapper.createObjectNode();
            node.fields().forEachRemaining(entry -> result.set(entry.getKey(), this.substitute(entry.getValue(), values)));
            return result;
        } else if (node.isArray()) {
            final ArrayNode result = this.objectMapper.createArrayNode();
            node.forEach(element -> result.add(this.substitute(element, values)));
            return result;
        }
        return node;
    }
}