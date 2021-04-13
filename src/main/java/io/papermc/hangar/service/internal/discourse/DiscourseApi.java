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

    public void createPost(String poster, int topicId, String content) {
        Map<String, String> args = new HashMap<>();
        args.put("topic_id", "" + topicId);
        args.put("raw", content);
        execute(args, config.getUrl() + "/posts.json", header(poster), HttpMethod.POST);
    }

    public void createTopic(String poster, String title, String content, @Nullable Integer categoryId) {
        Map<String, String> args = new HashMap<>();
        args.put("title", title);
        args.put("raw", content);
        args.put("category", categoryId == null ? null : categoryId.toString());
        execute(args, config.getUrl() + "/posts.json", header(poster), HttpMethod.POST);
    }

    public void updateTopic(String poster, int topicId, @Nullable String title, @Nullable Integer categoryId) {
        Map<String, String> args = new HashMap<>();
        args.put("topic_id", topicId + "");
        args.put("title", title);
        args.put("category", categoryId == null ? null : categoryId.toString());
        execute(args, config.getUrl() + "/t/-/" + topicId + ".json", header(poster), HttpMethod.PUT);
    }

    public void updatePost(String poster, int postId, String content) {
        Map<String, String> args = new HashMap<>();
        args.put("raw", content);
        execute(args, config.getUrl() + "/posts/" + postId + ".json", header(poster), HttpMethod.PUT);
    }

    public void deleteTopic(String poster, int topicId) {
        execute(null, config.getUrl() + "/t/" + topicId + ".json", header(poster), HttpMethod.DELETE);
    }

    // TODO error handling, 429 handling

    private void execute(Object args, String url, HttpHeaders headers, HttpMethod method) {
        HttpEntity<Object> entity = new HttpEntity<>(args, headers);
        ResponseEntity<Object> response = restTemplate.exchange(url, method, entity, Object.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println(response.getBody());
        } else {
            System.out.println("error! " + response.getStatusCode().name() + ": " + response.getBody());
        }
    }
}
