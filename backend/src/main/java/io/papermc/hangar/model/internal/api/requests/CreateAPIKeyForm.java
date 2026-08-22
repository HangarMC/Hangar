package io.papermc.hangar.model.internal.api.requests;

import io.papermc.hangar.model.common.NamedPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record CreateAPIKeyForm(@Schema(minimum = "5", maximum = "36", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @Size(min = 5, max = 36) String name,
                               @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Set<NamedPermission> permissions,
                               @Schema(description = "Slugs of the projects the key may be used on. Leave empty to allow all projects") @Size(max = 100) @Nullable Set<String> projects,
                               @Schema(description = "Point in time at which the key stops working. Leave empty for a key that never expires") @Nullable OffsetDateTime expiresAt) {
}
