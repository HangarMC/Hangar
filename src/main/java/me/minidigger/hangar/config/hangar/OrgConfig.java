package me.minidigger.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hangar.orgs")
public class OrgConfig {
    private boolean enabled = true;
    private String dummyEmailDomain = "org.papermc.io";
    private int createLimit = 5;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDummyEmailDomain() {
        return dummyEmailDomain;
    }

    public void setDummyEmailDomain(String dummyEmailDomain) {
        this.dummyEmailDomain = dummyEmailDomain;
    }

    public int getCreateLimit() {
        return createLimit;
    }

    public void setCreateLimit(int createLimit) {
        this.createLimit = createLimit;
    }
}
