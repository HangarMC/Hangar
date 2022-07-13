package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import java.util.List;

public record HangarProjectFlagNotification(long id, long userId, List<String> message,
                                            String originUserName, @EnumByOrdinal NotificationType type) {
}
