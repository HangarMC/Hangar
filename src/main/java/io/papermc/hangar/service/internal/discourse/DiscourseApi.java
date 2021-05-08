package io.papermc.hangar.service.internal.discourse;

import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import io.papermc.hangar.config.hangar.DiscourseConfig;
import io.papermc.hangar.model.internal.discourse.DiscoursePost;

@Component
public class DiscourseApi {

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

    // TODO error handling, 429 handling
    private void execute(Object args, String url, HttpHeaders headers, HttpMethod method) {
        Object object = execute(args, url, headers, method, Object.class);
        if (object != null) {
            System.out.println(object);
        }
    }

    private <T> T execute(Object args, String url, HttpHeaders headers, HttpMethod method, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(args, headers);
        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            System.out.println("error! " + response.getStatusCode().name() + ": " + response.getBody());
            return null;
        }
    }
}
