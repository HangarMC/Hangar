package io.papermc.hangar.model.internal.api.requests;

import io.papermc.hangar.model.common.NamedPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreateAPIKeyForm(@Schema(minimum = "5", maximum = "36", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @Size(min = 5, max = 36) String name,
                               @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Set<NamedPermission> permissions) {
}
