package io.papermc.hangar.model.internal.api.requests.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.NotNull;

public record ReportNotificationForm(boolean warning, boolean toReporter, @Valid @Size(max = 500) @NotNull String content) {
}
