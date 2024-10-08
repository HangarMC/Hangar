package io.papermc.hangar.controller.internal;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.internal.versions.JarScanResult;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.versions.JarScanningService;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Unlocked
@RestController
@RateLimit(path = "jarscanning")
@PermissionRequired(NamedPermission.REVIEWER)
@RequestMapping(value = "/api/internal/jarscanning", produces = MediaType.APPLICATION_JSON_VALUE)
public class JarScanningController {

    private final JarScanningService jarScanningService;

    public JarScanningController(final JarScanningService jarScanningService) {
        this.jarScanningService = jarScanningService;
    }

    @GetMapping("/result/{versionId}")
    public @Nullable List<JarScanResult> getResults(@PathVariable final long versionId)  {
        return this.jarScanningService.getLastResults(versionId);
    }

    @GetMapping("/result/{versionId}/{platform}")
    public @Nullable JarScanResult getResult(@PathVariable final long versionId, @PathVariable final Platform platform)  {
        return this.jarScanningService.getLastResult(versionId, platform);
    }

    @PostMapping("/scan/{versionId}/{platform}")
    public void scan(@PathVariable final long versionId, @PathVariable final Platform platform)  {
        try {
            this.jarScanningService.scanPlatform(versionId, platform);
        } catch (final Exception e) {
            throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
