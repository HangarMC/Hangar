package io.papermc.hangar.config.hangar;

import io.papermc.hangar.exceptions.HangarApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "hangar.pages")
public class PagesConfig {

    @NestedConfigurationProperty
    public Home home;
    private String nameRegex = "^[a-zA-Z0-9-_ ]+$";
    private int minNameLen = 3;
    private int maxNameLen = 25;
    private int minLen = 15;
    private int maxLen = 32000;

    @Autowired
    public PagesConfig(Home home) {
        this.home = home;
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

    public Home getHome() {
        return home;
    }

    public void setHome(Home home) {
        this.home = home;
    }

    public String getNameRegex() {
        return nameRegex;
    }

    public void setNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
    }

    public int getMinNameLen() {
        return minNameLen;
    }

    public void setMinNameLen(int minNameLen) {
        this.minNameLen = minNameLen;
    }

    public int getMaxNameLen() {
        return maxNameLen;
    }

    public void setMaxNameLen(int maxNameLen) {
        this.maxNameLen = maxNameLen;
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

    public void testPageName(String name) {
        if (name.length() > maxNameLen) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.name.maxLength");
        } else if (name.length() < minNameLen) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.name.minLength");
        } else if (!Pattern.compile(nameRegex).matcher(name).matches()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.name.invalidChars");
        }
    }
}
