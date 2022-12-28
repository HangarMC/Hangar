package io.papermc.hangar.service.internal.discourse;

import io.papermc.hangar.config.hangar.DiscourseConfig;
import io.papermc.hangar.model.internal.discourse.DiscourseError;
import io.papermc.hangar.model.internal.discourse.DiscoursePost;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class DiscourseApi {

    private static final Logger logger = LoggerFactory.getLogger(DiscourseApi.class);

    private final RestTemplate restTemplate;
    private final DiscourseConfig config;

    @Autowired
    public DiscourseApi(final RestTemplate restTemplate, final DiscourseConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    private HttpHeaders header(final String poster) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", this.config.apiKey());
        headers.set("Api-Username", poster == null ? this.config.adminUser() : poster);
        return headers;
    }

    public DiscoursePost createPost(final String poster, final long topicId, final String content) {
        final Map<String, Object> args = new HashMap<>();
        args.put("topic_id", topicId);
        args.put("raw", content);
        return this.execute(args, this.config.url() + "/posts.json", this.header(poster), HttpMethod.POST, DiscoursePost.class);
    }

    public DiscoursePost createTopic(final String poster, final String title, final String content, final @Nullable Integer categoryId) {
        final Map<String, Object> args = new HashMap<>();
        args.put("title", title);
        args.put("raw", content);
        args.put("category", categoryId);
        return this.execute(args, this.config.url() + "/posts.json", this.header(poster), HttpMethod.POST, DiscoursePost.class);
    }

    public void updateTopic(final String poster, final long topicId, final @Nullable String title, final @Nullable Integer categoryId) {
        final Map<String, Object> args = new HashMap<>();
        args.put("topic_id", topicId);
        args.put("title", title);
        args.put("category", categoryId);
        this.execute(args, this.config.url() + "/t/-/" + topicId + ".json", this.header(poster), HttpMethod.PUT);
    }

    public void updatePost(final String poster, final long postId, final String content) {
        final Map<String, String> args = new HashMap<>();
        args.put("raw", content);
        this.execute(args, this.config.url() + "/posts/" + postId + ".json", this.header(poster), HttpMethod.PUT);
    }

    public void deleteTopic(final String poster, final long topicId) {
        this.execute(null, this.config.url() + "/t/" + topicId + ".json", this.header(poster), HttpMethod.DELETE);
    }

    private void execute(final Object args, final String url, final HttpHeaders headers, final HttpMethod method) {
        this.execute(args, url, headers, method, Object.class);
    }

    private <T> T execute(final Object args, final String url, final HttpHeaders headers, final HttpMethod method, final Class<T> responseType) {
        try {
            final HttpEntity<Object> entity = new HttpEntity<>(args, headers);
            final ResponseEntity<T> response = this.restTemplate.exchange(url, method, entity, responseType);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                final Object body = response.getBody();
                throw this.createFromStatus(response.getStatusCode(), body != null ? body.toString() : null);
            }
        } catch (final HttpStatusCodeException ex) {
            throw this.createFromStatus(ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (final Exception ex) {
            throw new DiscourseError.UnknownError("Unknown discourse error, " + ex.getMessage(), "unknown", Map.of());
        }
    }

    private DiscourseError createFromStatus(final HttpStatusCode status, final String message) {
        if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            if (message != null) {
                try {
                    final JSONObject json = new JSONObject(message);
                    final JSONObject extras = json.getJSONObject("extras");
                    return new DiscourseError.RateLimitError(Duration.ofSeconds(extras.getInt("wait_seconds")));
                } catch (final JSONException e) {
                    logger.warn("Failed to parse JSON in 429 from Discourse. Error: {} To parse: {}", e.getMessage(), message, e);
                    return new DiscourseError.RateLimitError(Duration.ofHours(12));
                }
            } else {
                logger.warn("Received 429 from Discourse with no body. Assuming wait time of 12 hours");
                return new DiscourseError.RateLimitError(Duration.ofHours(12));
            }
        } else if (status.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
            return new DiscourseError.NotProcessable(message);
        } else {
            return new DiscourseError.StatusError(status, message);
        }
    }
}
