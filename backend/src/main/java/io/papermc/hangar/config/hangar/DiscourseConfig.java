package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hangar.discourse")
public class DiscourseConfig {

    private boolean enabled = false;
    private String url = "https://papermc.io/forums/";
    private String adminUser;
    private String apiKey;
    private int category;
    private int categoryDeleted;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getCategoryDeleted() {
        return categoryDeleted;
    }

    public void setCategoryDeleted(int categoryDeleted) {
        this.categoryDeleted = categoryDeleted;
    }
}
