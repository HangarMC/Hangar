package io.papermc.hangar.util;


import io.papermc.hangar.components.images.service.AvatarService;
import org.jspecify.annotations.Nullable;

public final class AvatarUtil {

    public static String avatarUrl(@Nullable String avatar, @Nullable String fallback) {
        String prefix = AvatarService.getInstance().getAvatarUrlPrefix();

        if (avatar != null) {
            return prefix + avatar;
        } else if (fallback != null) {
            return prefix + fallback;
        } else {
            return AvatarService.getInstance().getDefaultAvatarUrl();
        }
    }


}
