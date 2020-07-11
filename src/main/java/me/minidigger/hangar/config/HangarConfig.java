package me.minidigger.hangar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("me.minidigger.hangar")
public class HangarConfig {

    @Value("${debug:false}")
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

    public void checkDebug() {
        if (!debug) {
            throw new UnsupportedOperationException("this function is supported in debug mode only");
        }
    }
}
