package io.papermc.hangar.model.internal.api.requests.admin;

import org.jetbrains.annotations.NotNull;
import javax.validation.Valid;

public record ReportNotificationForm(boolean warning, boolean toReporter, @Valid @NotNull String content) {
}
