package io.papermc.hangar.components.auth.model;

import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;

public record AccountDeletionStatus(@Nullable OffsetDateTime deletionRequestedAt, long ownedProjectCount, long ownedOrganizationCount) {
}
