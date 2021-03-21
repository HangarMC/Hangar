package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "hangar.orgs")
public class OrganizationsConfig {
    private boolean enabled = true;
    private String dummyEmailDomain = "org.papermc.io";
    private int createLimit = 5;
    private int minNameLen = 3;
    private int maxNameLen = 24;
    private String nameRegex = "[a-zA-Z0-9-_]*";
    private final Predicate<String> namePredicate = Pattern.compile(nameRegex).asMatchPredicate();

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

    public int getMinNameLen() {
        return minNameLen;
    }

    public void setMinNameLen(int minNameLen) {
        this.minNameLen = minNameLen;
    }

    public String getNameRegex() {
        return nameRegex;
    }

    public void setNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
    }

    public int getMaxNameLen() {
        return maxNameLen;
    }

    public void setMaxNameLen(int maxNameLen) {
        this.maxNameLen = maxNameLen;
    }

    public boolean testName(String name) {
        return namePredicate.test(name);
    }
}
