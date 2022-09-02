package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "hangar.projects")
public class ProjectsConfig {

    private String nameRegex = "^[a-zA-Z0-9-_]{3,}$";
    private String versionNameRegex = "^[a-zA-Z0-9-_.+]+$";
    private String pageNameRegex = "^[a-zA-Z0-9-_.]+$";
    private Pattern namePattern = Pattern.compile(this.nameRegex);
    private Pattern versionNamePattern = Pattern.compile(this.versionNameRegex);
    private Pattern pageNamePattern = Pattern.compile(this.versionNameRegex);
    private int maxNameLen = 25;
    private int maxVersionNameLen = 30;
    private int maxDependencies = 100;
    private int maxPageNameLen = 25;
    private int maxPages = 50;
    private int maxChannels = 5;
    private int maxBBCodeLen = 30_000;
    private int initLoad = 25;
    private int initVersionLoad = 10;
    private int maxDescLen = 120;
    private int maxSponsorsLen = 500;
    private int maxKeywords = 5;
    private int contentMaxLen = 1_000_000;
    private boolean fileValidate = true;
    @DurationUnit(ChronoUnit.DAYS)
    private Duration staleAge = Duration.ofDays(28);
    private String checkInterval = "1h";
    private String draftExpire = "1d";
    private int userGridPageSize = 30;
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration unsafeDownloadMaxAge = Duration.ofMinutes(10);
    private boolean showUnreviewedDownloadWarning;

    public String getNameRegex() {
        return nameRegex;
    }

    public Predicate<String> getNameMatcher() {
        return namePattern.asMatchPredicate();
    }

    public Predicate<String> getVersionNameMatcher() {
        return versionNamePattern.asMatchPredicate();
    }

    public Predicate<String> getPageNameMatcher() {
        return pageNamePattern.asMatchPredicate();
    }

    public void setNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
        this.namePattern = Pattern.compile(nameRegex);
    }

    public String getVersionNameRegex() {
        return versionNameRegex;
    }

    public void setVersionNameRegex(String versionNameRegex) {
        this.versionNameRegex = versionNameRegex;
        this.versionNamePattern = Pattern.compile(versionNameRegex);
    }

    public String getPageNameRegex() {
        return pageNameRegex;
    }

    public void setPageNameRegex(String pageNameRegex) {
        this.pageNameRegex = pageNameRegex;
    }

    public int getMaxNameLen() {
        return maxNameLen;
    }

    public void setMaxNameLen(int maxNameLen) {
        this.maxNameLen = maxNameLen;
    }

    public int getMaxVersionNameLen() {
        return maxVersionNameLen;
    }

    public int getMaxDependencies() {
        return maxDependencies;
    }

    public void setMaxDependencies(final int maxDependencies) {
        this.maxDependencies = maxDependencies;
    }

    public void setMaxVersionNameLen(int maxVersionNameLen) {
        this.maxVersionNameLen = maxVersionNameLen;
    }

    public int getMaxPageNameLen() {
        return maxPageNameLen;
    }

    public void setMaxPageNameLen(int maxPageNameLen) {
        this.maxPageNameLen = maxPageNameLen;
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

    public void setMaxBBCodeLen(int maxBBCodeLen) {
        this.maxBBCodeLen = maxBBCodeLen;
    }

    public int getMaxBBCodeLen() {
        return maxBBCodeLen;
    }

    public int getMaxSponsorsLen() {
        return maxSponsorsLen;
    }

    public void setMaxSponsorsLen(final int maxSponsorsLen) {
        this.maxSponsorsLen = maxSponsorsLen;
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

    public Duration getUnsafeDownloadMaxAge() {
        return unsafeDownloadMaxAge;
    }

    public void setUnsafeDownloadMaxAge(Duration unsafeDownloadMaxAage) {
        this.unsafeDownloadMaxAge = unsafeDownloadMaxAage;
    }

    public boolean showUnreviewedDownloadWarning() {
        return showUnreviewedDownloadWarning;
    }

    public void setShowUnreviewedDownloadWarning(boolean showUnreviewedDownloadWarning) {
        this.showUnreviewedDownloadWarning = showUnreviewedDownloadWarning;
    }

    public int getContentMaxLen() {
        return contentMaxLen;
    }

    public void setContentMaxLen(int contentMaxLen) {
        this.contentMaxLen = contentMaxLen;
    }
}
