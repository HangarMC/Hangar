package io.papermc.hangar.controller.internal;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.JarScanResultTable;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.versions.JarScanningService;
import java.io.IOException;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Unlocked
@Controller
@RateLimit(path = "jarscanning")
@PermissionRequired(NamedPermission.REVIEWER)
@RequestMapping(value = "/api/internal/jarscanning", produces = MediaType.APPLICATION_JSON_VALUE)
public class JarScanningController {

    private final JarScanningService jarScanningService;

    public JarScanningController(final JarScanningService jarScanningService) {
        this.jarScanningService = jarScanningService;
    }

    @ResponseBody
    @GetMapping("/result/{platform}/{versionId}")
    public @Nullable JarScanResultTable getResult(@PathVariable final Platform platform, @PathVariable final long versionId)  {
        return this.jarScanningService.getLastResult(versionId, platform);
    }

    @ResponseBody
    @PostMapping("/scan/{platform}/{versionId}")
    public void scan(@PathVariable final Platform platform, @PathVariable final long versionId)  {
        try {
            this.jarScanningService.scan(versionId, platform);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
