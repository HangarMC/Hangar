package me.minidigger.hangar.config.hangar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hangar.pages")
public class PagesConfig {

    @NestedConfigurationProperty
    public Home home;
    private int minLen = 15;
    private int maxLen = 32000;
    @NestedConfigurationProperty
    public Page page;

    @Autowired
    public PagesConfig(Home home, Page page) {
        this.home = home;
        this.page = page;
    }

    @Component
    @ConfigurationProperties(prefix = "hangar.pages.home")
    public static class Home {
        private String name = "Home";
        private String message = "Welcome to your new project!";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @Component
    @ConfigurationProperties(prefix = "hangar.pages.page")
    public static class Page {
        private int maxLen = 75000;

        public int getMaxLen() {
            return maxLen;
        }

        public void setMaxLen(int maxLen) {
            this.maxLen = maxLen;
        }
    }

    public Home getHome() {
        return home;
    }

    public void setHome(Home home) {
        this.home = home;
    }

    public int getMinLen() {
        return minLen;
    }

    public void setMinLen(int minLen) {
        this.minLen = minLen;
    }

    public int getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(int maxLen) {
        this.maxLen = maxLen;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }
}
