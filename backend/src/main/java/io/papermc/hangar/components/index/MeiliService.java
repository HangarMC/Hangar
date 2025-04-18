package io.papermc.hangar.components.index;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.requests.RequestPagination;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

@Service
public class MeiliService extends HangarComponent implements ApplicationListener<ContextRefreshedEvent> {

    private final RestClient restClient;

    public MeiliService(final List<HttpMessageConverter<?>> messageConverters) {
        RestClient.Builder builder = RestClient.builder()
            .messageConverters(messageConverters)
            .baseUrl(config.meili.url())
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
        if (StringUtils.isNotBlank(config.meili.key())) {
            builder = builder.defaultHeader("Authorization", "Bearer " + config.meili.key());
        }
        restClient = builder.build();
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        this.setupProjectIndex();
        this.setupVersionIndex();
    }

    public void setupProjectIndex() {
        var createIndexBody = Map.of(
            "uid", this.config.meili.prefix() + "projects",
            "primaryKey", "id"
        );
        restClient.post().uri("/indexes").contentType(MediaType.APPLICATION_JSON).body(createIndexBody).retrieve().toEntity(Task.class);
        var settings = Map.of(
            "distinctAttribute", "id",
            "searchableAttributes", List.of("name", "namespace.owner", "description", "category", "mainPageContent", "memberNames", "createdAt", "lastUpdated", "stats", "settings.keywords", "settings.tags"),
            "displayedAttributes", List.of("*"),
            "filterableAttributes", List.of("category", "settings.tags", "namespace.owner", "createdAt", "lastUpdated", "settings.license.type", "supportedPlatforms", "memberNames"),
            "sortableAttributes", List.of("stats.views", "stats.downloads", "stats.recentDownloads", "stats.recentViews", "stats.stars", "createdAt", "lastUpdated", "name")
        );
        restClient.patch().uri("/indexes/projects/settings").contentType(MediaType.APPLICATION_JSON).body(settings).retrieve().toEntity(Task.class);
    }

    public void setupVersionIndex() {
        var createIndexBody = Map.of(
            "uid", this.config.meili.prefix() + "versions",
            "primaryKey", "id"
        );
        restClient.post().uri("/indexes").contentType(MediaType.APPLICATION_JSON).body(createIndexBody).retrieve().toEntity(Task.class);
        var settings = Map.of(
            "distinctAttribute", "id",
            "searchableAttributes", List.of("name", "description", "author", "platformDependencies", "channel.name", "projectId"),
            "displayedAttributes", List.of("*"),
            "filterableAttributes", List.of("platformDependencies", "channel.name", "projectId"),
            "sortableAttributes", List.of("createdAt")
        );
        restClient.patch().uri("/indexes/versions/settings").contentType(MediaType.APPLICATION_JSON).body(settings).retrieve().toEntity(Task.class);
    }

    public void sendProjects(List<Project> projects) {
        sendDocuments("projects", projects);
    }

    public void sendVersions(List<Version> versions) {
        sendDocuments("versions", versions);
    }

    private <T> void sendDocuments(String index, List<T> documents) {
        restClient.post().uri("/indexes/" + this.config.meili.prefix() + index + "/documents").contentType(MediaType.APPLICATION_JSON).body(documents).retrieve().toEntity(Task.class);
    }

    public void removeProject(long id) {
        removeDocument("projects", id);
    }

    public void removeVersion(long id) {
        removeDocument("versions", id);
    }

    private void removeDocument(String index, long id) {
        restClient.delete().uri("/indexes/" + this.config.meili.prefix() + index + "/documents/" + id).retrieve().toEntity(Task.class);
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
            .uri("/indexes/" + this.config.meili.prefix() + "projects/search")
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
            .uri("/indexes/" + this.config.meili.prefix() + "versions/search")
            .contentType(MediaType.APPLICATION_JSON)
            .body(searchBody)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<SearchResult<Version>>() {});
        if (entity.getStatusCode().is2xxSuccessful()) {
            return entity.getBody();
        }
        throw new HangarApiException("Error searching versions", entity.getStatusCode());
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

    record Task(
        String taskUid,
        String indexUid,
        String status,
        String type,
        String enqueuedAt
    ) {
    }
}
