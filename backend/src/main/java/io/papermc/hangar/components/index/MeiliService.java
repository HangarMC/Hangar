package io.papermc.hangar.components.index;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.requests.RequestPagination;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

@Service
public class MeiliService extends HangarComponent implements ApplicationListener<ContextRefreshedEvent> {

    public static final String PROJECT_INDEX = "projects";
    public static final String VERSION_INDEX = "versions";

    private final RestClient restClient;

    public MeiliService(HangarConfig config, final List<HttpMessageConverter<?>> messageConverters) {
        RestClient.Builder builder = RestClient.builder()
            .messageConverters(messageConverters)
            .baseUrl(config.meili().url())
            .defaultStatusHandler(new ResponseErrorHandler() {
                @Override
                public boolean hasError(final @NotNull ClientHttpResponse response) throws IOException {
                    return response.getStatusCode().isError();
                }

                @Override
                public void handleError(final @NotNull URI url, final @NotNull HttpMethod method, final @NotNull ClientHttpResponse response) throws IOException {
                    throw new HangarApiException("Error communicating with MeiliSearch: " + method.name() + " " + url + " -> " + response.getStatusCode(), new String(response.getBody().readAllBytes()));
                }
            });
        if (StringUtils.isNotBlank(config.meili().key())) {
            builder = builder.defaultHeader("Authorization", "Bearer " + config.meili().key());
        }
        restClient = builder.build();
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        this.setupProjectIndex("");
        this.setupVersionIndex("");
    }

    public void setupProjectIndex(String suffix) {
        var createIndexBody = Map.of(
            "uid", this.config.meili().prefix() + PROJECT_INDEX + suffix,
            "primaryKey", "id"
        );
        waitForTask(restClient.post().uri("/indexes").contentType(MediaType.APPLICATION_JSON).body(createIndexBody).retrieve().toEntity(Task.class));
        var settings = Map.of(
            "distinctAttribute", "id",
            "searchableAttributes", List.of("name", "namespace.owner", "description", "category", "mainPageContent", "memberNames", "createdAt", "lastUpdated", "stats", "settings.keywords", "settings.tags"),
            "displayedAttributes", List.of("*"),
            "filterableAttributes", List.of("category", "settings.tags", "namespace.owner", "createdAt", "lastUpdated", "settings.license.type", "supportedPlatforms", "memberNames", "visibility"),
            "sortableAttributes", List.of("stats.views", "stats.downloads", "stats.recentDownloads", "stats.recentViews", "stats.stars", "createdAt", "lastUpdated", "name"),
            "pagination", Map.of("maxTotalHits", 5000)
        );
        waitForTask(restClient.patch().uri("/indexes/" + this.config.meili().prefix() + PROJECT_INDEX + suffix + "/settings").contentType(MediaType.APPLICATION_JSON).body(settings).retrieve().toEntity(Task.class));
    }

    public void setupVersionIndex(String suffix) {
        var createIndexBody = Map.of(
            "uid", this.config.meili().prefix() + VERSION_INDEX + suffix,
            "primaryKey", "id"
        );
        waitForTask(restClient.post().uri("/indexes").contentType(MediaType.APPLICATION_JSON).body(createIndexBody).retrieve().toEntity(Task.class));
        var settings = Map.of(
            "distinctAttribute", "id",
            "searchableAttributes", List.of("name", "description", "author", "platformDependencies", "channel.name", "projectId"),
            "displayedAttributes", List.of("*"),
            "filterableAttributes", List.of("platformDependencies", "channel.name", "projectId", "visibility", "memberNames"),
            "sortableAttributes", List.of("createdAt"),
            "pagination", Map.of("maxTotalHits", 5000)
        );
        waitForTask(restClient.patch().uri("/indexes/" + this.config.meili().prefix() + VERSION_INDEX + suffix + "/settings").contentType(MediaType.APPLICATION_JSON).body(settings).retrieve().toEntity(Task.class));
    }

    public <T> ResponseEntity<Task> sendDocuments(String index, List<T> documents) {
        return restClient.post().uri("/indexes/" + this.config.meili().prefix() + index + "/documents").contentType(MediaType.APPLICATION_JSON).body(documents).retrieve().toEntity(Task.class);
    }

    public ResponseEntity<Task> removeDocument(String index, long id) {
        return restClient.delete().uri("/indexes/" + this.config.meili().prefix() + index + "/documents/" + id).retrieve().toEntity(Task.class);
    }

    public SearchResult<Project> searchProjects(final String query, final String filter, final List<String> sort, final long offset, final long limit) {
        var searchBody = Map.of(
            "q", query,
            "filter", filter,
            "sort", sort,
            "offset", offset,
            "limit", limit
        );
        var entity = restClient.post()
            .uri("/indexes/" + this.config.meili().prefix() + PROJECT_INDEX + "/search")
            .contentType(MediaType.APPLICATION_JSON)
            .body(searchBody)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<SearchResult<Project>>() {});
        if (entity.getStatusCode().is2xxSuccessful()) {
            return entity.getBody();
        }
        throw new HangarApiException("Error searching projects", entity.getStatusCode());
    }

    public SearchResult<Version> searchVersions(final String query, final String filter, final List<String> sort, final long offset, final long limit) {
        var searchBody = Map.of(
            "q", query,
            "filter", filter,
            "sort", sort,
            "offset", offset,
            "limit", limit
        );

        var entity = restClient.post()
            .uri("/indexes/" + this.config.meili().prefix() + VERSION_INDEX + "/search")
            .contentType(MediaType.APPLICATION_JSON)
            .body(searchBody)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<SearchResult<Version>>() {});
        if (entity.getStatusCode().is2xxSuccessful()) {
            return entity.getBody();
        }
        throw new HangarApiException("Error searching versions", entity.getStatusCode());
    }

    public void swapIndexes(String oldIndex, String newIndex) {
        // swap
        var body = List.of(Map.of("indexes", List.of(this.config.meili().prefix() + oldIndex, this.config.meili().prefix() + newIndex)));
        waitForTask(restClient.post().uri("/swap-indexes").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toEntity(Task.class));

        // then deleted the old index (which is now named -new)
        waitForTask(restClient.delete().uri("/indexes/" + this.config.meili().prefix() + newIndex).retrieve().toEntity(Task.class));
    }

    public Task getTask(String id) {
        var entity = restClient.get().uri("/tasks/" + id).retrieve().toEntity(Task.class);
        if (entity.getStatusCode().is2xxSuccessful()) {
            return entity.getBody();
        }
        throw new HangarApiException("Error getting task " + id, entity.getStatusCode());
    }

    public void waitForTask(ResponseEntity<Task> task) {
        if (task.getStatusCode().is2xxSuccessful() && task.getBody() != null) {
            waitForTask(task.getBody());
        } else {
            throw new HangarApiException("Error waiting for meili task: " + task);
        }
    }

    public void waitForTask(Task task) {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        while (task.status().equals("enqueued") || task.status().equals("processing")) {
            if (elapsedTime >= 5000) {
                throw new RuntimeException("Timeout waiting for meili task " + task);
            }
            task = this.getTask(task.uid() != null ? task.uid() : task.taskUid());
            try {
                //noinspection BusyWait - blocking is ok
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for meili task " + task);
            }
            elapsedTime = System.currentTimeMillis() - startTime;
        }
    }

    public record SearchResult<T>(
        List<T> hits,
        int offset,
        int limit,
        int estimatedTotalHits,
        int processingTimeMs
    ) {

        public PaginatedResult<T> asPaginatedResult() {
            return new PaginatedResult<>(new Pagination((long) estimatedTotalHits(), new RequestPagination((long) limit(), (long) offset())), hits());
        }
    }

    public record Task(
        String taskUid,
        String uid,
        String indexUid,
        String status,
        String type,
        String enqueuedAt,
        String error,
        Duration duration
    ) {
    }
}
