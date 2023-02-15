package io.papermc.hangar.model.internal.user.notifications;

import java.time.OffsetDateTime;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public record HangarNotification(OffsetDateTime createdAt,
                                 long id,
                                 String action,
                                 List<String> message,
                                 boolean read,
                                 String originUserName,
                                 @EnumByOrdinal NotificationType type) {
}
