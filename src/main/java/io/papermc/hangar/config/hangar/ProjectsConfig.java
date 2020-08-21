package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "hangar.projects")
public class ProjectsConfig {
    private int maxNameLen = 25;
    private int maxPages = 50;
    private int maxChannels = 5;
    private int initLoad = 25;
    private int initVersionLoad = 10;
    private int maxDescLen = 120;
    private int maxKeywords = 5;
    private boolean fileValidate = true;
    private Duration staleAge = Duration.ofDays(28);
    private String checkInterval = "1h";
    private String draftExpire = "1d";
    private int userGridPageSize = 30;

    public int getMaxNameLen() {
        return maxNameLen;
    }

    public void setMaxNameLen(int maxNameLen) {
        this.maxNameLen = maxNameLen;
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public int getMaxChannels() {
        return maxChannels;
    }

    public void setMaxChannels(int maxChannels) {
        this.maxChannels = maxChannels;
    }

    public int getInitLoad() {
        return initLoad;
    }

    public void setInitLoad(int initLoad) {
        this.initLoad = initLoad;
    }

    public int getInitVersionLoad() {
        return initVersionLoad;
    }

    public void setInitVersionLoad(int initVersionLoad) {
        this.initVersionLoad = initVersionLoad;
    }

    public int getMaxDescLen() {
        return maxDescLen;
    }

    public void setMaxDescLen(int maxDescLen) {
        this.maxDescLen = maxDescLen;
    }

    public boolean isFileValidate() {
        return fileValidate;
    }

    public void setFileValidate(boolean fileValidate) {
        this.fileValidate = fileValidate;
    }

    public Duration getStaleAge() {
        return staleAge;
    }

    public void setStaleAge(Duration staleAge) {
        this.staleAge = staleAge;
    }

    public String getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(String checkInterval) {
        this.checkInterval = checkInterval;
    }

    public String getDraftExpire() {
        return draftExpire;
    }

    public void setDraftExpire(String draftExpire) {
        this.draftExpire = draftExpire;
    }

    public int getUserGridPageSize() {
        return userGridPageSize;
    }

    public void setUserGridPageSize(int userGridPageSize) {
        this.userGridPageSize = userGridPageSize;
    }

    public int getMaxKeywords() {
        return maxKeywords;
    }

    public void setMaxKeywords(int maxKeywords) {
        this.maxKeywords = maxKeywords;
    }
}
