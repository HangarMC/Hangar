package io.papermc.hangar.model.internal.api.requests;

import io.papermc.hangar.model.common.NamedPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateAPIKeyForm(@Schema(minimum = "5", maximum = "255", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @Size(min = 5, max = 255) String name,
                               @Schema(requiredMode = Schema.RequiredMode.REQUIRED) @Size(min = 1) List<NamedPermission> permissions) {
}
