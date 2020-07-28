package me.minidigger.hangar.config;

import me.minidigger.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import me.minidigger.hangar.model.Color;

@Configuration
@ComponentScan("me.minidigger.hangar")
public class HangarConfig {

    @Value("${hangar.debug:false}")
    private boolean debug;

    @Value("${fakeUser.enabled:false}")
    private boolean fakeUserEnabled;
    @Value("${fakeUser.id:-1}")
    private long fakeUserId;
    @Value("${fakeUser.name:paper}")
    private String fakeUserName;
    @Value("${fakeUser.username:paper}")
    private String fakeUserUserName;
    @Value("${fakeUser.email:paper@papermc.io}")
    private String fakeUserEmail;

    @Value("${session.expiration:1209600}") // 14 days
    private long sessionExpiration;

    @Value("${pluginUploadDir:/work/uploads}")
    private String pluginUploadDir;

    @Value("${defaultChannelName:Release}")
    private String defaultChannelName;

    @Value("${defaultChannelColor:7}")
    private int defaultChannelColor;

    @Value("${authorPageSize:25}")
    private long authorPageSize;

    @Value("${maxTaglineLen:100}")
    private long maxTaglineLen;

    @Value("${authUrl:https://hangarauth.minidigger.me}")
    private String authUrl;

    @Value("${maxProjectNameLen:25}")
    private long maxProjectNameLen;

    public boolean isFakeUserEnabled() {
        return fakeUserEnabled;
    }

    public long getFakeUserId() {
        return fakeUserId;
    }

    public String getFakeUserName() {
        return fakeUserName;
    }

    public String getFakeUserUserName() {
        return fakeUserUserName;
    }

    public String getFakeUserEmail() {
        return fakeUserEmail;
    }

    public long getSessionExpiration() {
        return sessionExpiration;
    }

    public String getPluginUploadDir() {
        return pluginUploadDir;
    }

    public String getDefaultChannelName() {
        return defaultChannelName;
    }

    public Color getDefaultChannelColor() {
        return Color.getById(defaultChannelColor);
    }

    public void checkDebug() {
        if (!debug) {
            throw new UnsupportedOperationException("this function is supported in debug mode only");
        }
    }

    public long getAuthorPageSize() {
        return authorPageSize;
    }

    public long getMaxTaglineLen() {
        return maxTaglineLen;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public boolean isValidProjectName(String name) {
        String sanitized = StringUtils.compact(name);
        return sanitized.length() >= 1 && sanitized.length() <= maxProjectNameLen;
    }
}
