package me.minidigger.hangar.util;

import me.minidigger.hangar.config.hangar.HangarConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TemplateHelper {

    private final HangarConfig hangarConfig;

    public TemplateHelper(HangarConfig hangarConfig) {
        this.hangarConfig = hangarConfig;
    }

    public String format(String s, String...args) {
        return String.format(s, args);
    }

    public String urlEncode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    public String avatarUrl(String name) {
        return String.format(hangarConfig.security.api.getAvatarUrl(), name);
    }
}
