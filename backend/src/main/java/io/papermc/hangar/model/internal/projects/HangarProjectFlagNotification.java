package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public record HangarProjectFlagNotification(long id, long userId, List<String> message,
                                            String originUserName, @EnumByOrdinal NotificationType type) {
}
