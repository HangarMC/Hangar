package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.interfaces.IVersionsController;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.service.api.VersionsApiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class VersionsController implements IVersionsController {

    private final VersionsApiService versionsApiService;

    @Autowired
    public VersionsController(VersionsApiService versionsApiService) {
        this.versionsApiService = versionsApiService;
    }

    @Override
    public ResponseEntity<Version> getVersion(String author, String slug, String name) {
        return ResponseEntity.ok(versionsApiService.getVersion(author, slug, name));
    }

    @Override
    public ResponseEntity<PaginatedResult<Version>> getVersions(String author, String slug, List<String> tags, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(versionsApiService.getVersions(author, slug, tags, pagination));
    }

    @Override
    public ResponseEntity<Map<String, VersionStats>> getVersionStats(String author, String slug, String version, @NotNull OffsetDateTime fromDate, @NotNull OffsetDateTime toDate) {
        return ResponseEntity.ok(versionsApiService.getVersionStats(author, slug, version, fromDate, toDate));
    }
}
