package io.papermc.hangar.service.internal.discourse;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import io.papermc.hangar.config.hangar.DiscourseConfig;
import io.papermc.hangar.model.internal.discourse.DiscourseError;
import io.papermc.hangar.model.internal.discourse.DiscoursePost;

@Component
public class DiscourseApi {

    private static final Logger logger = LoggerFactory.getLogger(DiscourseApi.class);

    private final RestTemplate restTemplate;
    private final DiscourseConfig config;

    public DiscourseApi(RestTemplate restTemplate, DiscourseConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    private HttpHeaders header(String poster) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", config.getApiKey());
        headers.set("Api-Username", poster == null ? config.getAdminUser() : poster);
        return headers;
    }

    public DiscoursePost createPost(String poster, long topicId, String content) {
        Map<String, Object> args = new HashMap<>();
        args.put("topic_id", topicId);
        args.put("raw", content);
        return execute(args, config.getUrl() + "/posts.json", header(poster), HttpMethod.POST, DiscoursePost.class);
    }

    public DiscoursePost createTopic(String poster, String title, String content, @Nullable Integer categoryId) {
        Map<String, Object> args = new HashMap<>();
        args.put("title", title);
        args.put("raw", content);
        args.put("category", categoryId);
        return execute(args, config.getUrl() + "/posts.json", header(poster), HttpMethod.POST, DiscoursePost.class);
    }

    public void updateTopic(String poster, long topicId, @Nullable String title, @Nullable Integer categoryId) {
        Map<String, Object> args = new HashMap<>();
        args.put("topic_id", topicId);
        args.put("title", title);
        args.put("category", categoryId);
        execute(args, config.getUrl() + "/t/-/" + topicId + ".json", header(poster), HttpMethod.PUT);
    }

    public void updatePost(String poster, long postId, String content) {
        Map<String, String> args = new HashMap<>();
        args.put("raw", content);
        execute(args, config.getUrl() + "/posts/" + postId + ".json", header(poster), HttpMethod.PUT);
    }

    public void deleteTopic(String poster, long topicId) {
        execute(null, config.getUrl() + "/t/" + topicId + ".json", header(poster), HttpMethod.DELETE);
    }

    private void execute(Object args, String url, HttpHeaders headers, HttpMethod method) {
        execute(args, url, headers, method, Object.class);
    }

    private <T> T execute(Object args, String url, HttpHeaders headers, HttpMethod method, Class<T> responseType) {
        try {
            HttpEntity<Object> entity = new HttpEntity<>(args, headers);
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                Object body = response.getBody();
                throw createFromStatus(response.getStatusCode(), body != null ? body.toString() : null);
            }
        } catch (RestClientException ex) {
            throw new DiscourseError.UnknownError("Unknown discourse error, " + ex.getMessage(), "unknown", Map.of());
        }
    }

    private DiscourseError createFromStatus(HttpStatus status, String message) {
        if (status.equals(HttpStatus.TOO_MANY_REQUESTS)) {
            if (message != null) {
                try {
                    JSONObject json = new JSONObject(message);
                    JSONObject extras = json.getJSONObject("extras");
                    return new DiscourseError.RateLimitError(Duration.ofSeconds(extras.getInt("wait_seconds")));
                } catch (JSONException e) {
                    logger.warn("Failed to parse JSON in 429 from Discourse. Error: {} To parse: {}", e.getMessage(), message, e);
                    return new DiscourseError.RateLimitError(Duration.ofHours(12));
                }
            } else {
                logger.warn("Received 429 from Discourse with no body. Assuming wait time of 12 hours");
                return new DiscourseError.RateLimitError(Duration.ofHours(12));
            }
        } else {
            return new DiscourseError.StatusError(status, message);
        }
    }
}
