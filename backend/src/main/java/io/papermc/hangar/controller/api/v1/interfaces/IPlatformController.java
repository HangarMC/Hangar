package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.platform.PlatformVersion;
import io.papermc.hangar.model.common.Platform;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Platform versions")
@RequestMapping("/api/v1")
public interface IPlatformController {

    @Operation(
        summary = "Gets a list of versions for a platform",
        operationId = "getPlatformVersions",
        description = "Gets a list of platform versions, including children.",
        tags = "Platforms"
    )
    @GetMapping("/platforms/{platform}/versions")
    List<PlatformVersion> getPlatformVersions(@Parameter(description = "The platform to get versions for") @PathVariable Platform platform);
}
