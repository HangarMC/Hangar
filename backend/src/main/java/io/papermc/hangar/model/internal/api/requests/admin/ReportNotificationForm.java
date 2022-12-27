package io.papermc.hangar.model.internal.api.requests.admin;

import javax.validation.Valid;
import org.jetbrains.annotations.NotNull;

public record ReportNotificationForm(boolean warning, boolean toReporter, @Valid @NotNull String content) {
}
