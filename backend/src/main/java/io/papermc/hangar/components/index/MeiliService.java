package io.papermc.hangar.components.index;

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
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

@Service
public class MeiliService implements ApplicationListener<ContextRefreshedEvent> {

    private final RestClient restClient;
    private final ResponseErrorHandler errorHandler;

    public MeiliService() {
        // TODO config
        // TODO auth
        restClient = RestClient.create("http://localhost:7700");
        this.errorHandler = new ResponseErrorHandler() {
            @Override
            public boolean hasError(final @NotNull ClientHttpResponse response) throws IOException {
                return response.getStatusCode().isError();
            }

            @Override
            public void handleError(final @NotNull URI url, final @NotNull HttpMethod method, final @NotNull ClientHttpResponse response) throws IOException {
                throw new HangarApiException("Error communicating with MeiliSearch: " + method.name() + " " + url + " -> " + response.getStatusCode(), new String(response.getBody().readAllBytes()));
            }
        };
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        this.setupProjectIndex();
        this.setupVersionIndex();
    }

    public void setupProjectIndex() {
        var createIndexBody = Map.of(
            "uid", "projects",
            "primaryKey", "id"
        );
        restClient.post().uri("/indexes").contentType(MediaType.APPLICATION_JSON).body(createIndexBody).retrieve().onStatus(errorHandler).toEntity(Task.class);
        var settings = Map.of(
            "distinctAttribute", "id",
            "searchableAttributes", List.of("name", "namespace.owner", "description", "category", "createdAt", "lastUpdated", "stats", "settings.keywords", "settings.tags"),
            "displayedAttributes", List.of("*"),
            "filterableAttributes", List.of("category", "settings.tags", "namespace.owner", "createdAt", "lastUpdated", "settings.license.type", "supportedPlatforms"), // TODO members <---
            "sortableAttributes", List.of("stats.views", "stats.downloads", "stats.recentDownloads", "stats.recentViews", "stats.stars", "createdAt", "lastUpdated", "name")
        );
        restClient.patch().uri("/indexes/projects/settings").contentType(MediaType.APPLICATION_JSON).body(settings).retrieve().onStatus(errorHandler).toEntity(Task.class);
    }

    public void setupVersionIndex() {
        var createIndexBody = Map.of(
            "uid", "versions",
            "primaryKey", "id"
        );
        restClient.post().uri("/indexes").contentType(MediaType.APPLICATION_JSON).body(createIndexBody).retrieve().onStatus(errorHandler).toEntity(Task.class);
        var settings = Map.of(
            "distinctAttribute", "id",
            "searchableAttributes", List.of("name", "description", "author", "platformDependencies", "channel.name", "projectId"),
            "displayedAttributes", List.of("*"),
            "filterableAttributes", List.of("platformDependencies", "channel.name", "projectId"),
            "sortableAttributes", List.of("createdAt")
        );
        restClient.patch().uri("/indexes/versions/settings").contentType(MediaType.APPLICATION_JSON).body(settings).retrieve().onStatus(errorHandler).toEntity(Task.class);
    }

    // TODO can see hidden?
    // TODO what about main page content?

    public void sendProjects(List<Project> projects) {
        sendDocuments("projects", projects);
    }

    public void sendVersions(List<Version> versions) {
        sendDocuments("versions", versions);
    }

    private <T> void  sendDocuments(String index, List<T> documents) {
        restClient.post().uri("/indexes/" + index + "/documents").contentType(MediaType.APPLICATION_JSON).body(documents).retrieve().onStatus(errorHandler).toEntity(Task.class);
    }

    public void removeProject(long id) {
        removeDocument("projects", id);
    }

    public void removeVersion(long id) {
        removeDocument("versions", id);
    }

    private void removeDocument(String index, long id) {
        restClient.delete().uri("/indexes/" + index + "/documents/" + id).retrieve().onStatus(errorHandler).toEntity(Task.class);
    }

    public SearchResult<Project> searchProjects(final String query, final String filter, final List<String> sort, final long offset, final long limit) {
        var searchBody = Map.of(
            "q", query,
            "filter", filter,
            "sort", sort,
            "offset", offset,
            "limit", limit
            // TODO highlight stuff in UI <---
            //"attributesToHighlight", List.of("name", "namespace.owner", "description", "category"),
            //"showMatchesPosition", true,
            // TODO show facet stats in UI <---
            //"facets", List.of("*")
        );
        var entity = restClient.post().uri("/indexes/projects/search").contentType(MediaType.APPLICATION_JSON).body(searchBody).retrieve().onStatus(errorHandler).toEntity(SearchResult.class);
        if (entity.getStatusCode().is2xxSuccessful()) {
            // noinspection unchecked
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
            // "facets", List.of("platformDependencies", "channel.name")
        );
        var entity = restClient.post().uri("/indexes/versions/search").contentType(MediaType.APPLICATION_JSON).body(searchBody).retrieve().onStatus(errorHandler).toEntity(SearchResult.class);
        if (entity.getStatusCode().is2xxSuccessful()) {
            // noinspection unchecked
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
