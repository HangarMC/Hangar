package me.minidigger.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hangar.users")
public class UserConfig {
    private int starsPerPage = 5;
    private int maxTaglineLen = 100;
    private int authorPageSize = 25;
    private int projectPageSize = 5;

    public int getStarsPerPage() {
        return starsPerPage;
    }

    public void setStarsPerPage(int starsPerPage) {
        this.starsPerPage = starsPerPage;
    }

    public int getMaxTaglineLen() {
        return maxTaglineLen;
    }

    public void setMaxTaglineLen(int maxTaglineLen) {
        this.maxTaglineLen = maxTaglineLen;
    }

    public int getAuthorPageSize() {
        return authorPageSize;
    }

    public void setAuthorPageSize(int authorPageSize) {
        this.authorPageSize = authorPageSize;
    }

    public int getProjectPageSize() {
        return projectPageSize;
    }

    public void setProjectPageSize(int projectPageSize) {
        this.projectPageSize = projectPageSize;
    }
}
