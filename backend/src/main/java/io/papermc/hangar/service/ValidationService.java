package io.papermc.hangar.service;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import io.papermc.hangar.util.StringUtils;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    private static final Set<String> BANNED_ROUTES = Set.of("api", "authors", "linkout", "logged-out", "new", "unread", "notifications", "staff", "admin", "organizations", "tools", "recommended", "null", "undefined", "privacy", "tos", "settings");
    private final HangarConfig config;

    public ValidationService(final HangarConfig config) {
        this.config = config;
    }

    public boolean isValidOrgName(final String name) {
        return this.config.org.testOrgName(name) && this.isValidUsername(name);
    }

    public boolean isValidUsername(String name) {
        name = StringUtils.compact(name);
        if (BANNED_ROUTES.contains(name)) {
            return false;
        }
        return name.length() >= 1;
    }

    public @Nullable String isValidProjectName(String name) {
        name = StringUtils.compact(name);
        String error = null;
        if (BANNED_ROUTES.contains(name)) {
            error = "invalidName";
        } else if (name.length() < 3) {
            error = "tooShortName";
        } else if (name.length() > this.config.projects.maxNameLen()) {
            error = "tooLongName";
        } else if (name.contains(ProjectFactory.SOFT_DELETION_SUFFIX) || !this.config.projects.nameRegex().test(name)) {
            error = "invalidName";
        }
        return error != null ? "project.new.error." + error : null;
    }

    public boolean isValidVersionName(String name) {
        name = StringUtils.compact(name);
        if (BANNED_ROUTES.contains(name) || name.contains(ProjectFactory.SOFT_DELETION_SUFFIX)) {
            return false;
        }
        return name.length() >= 1 && name.length() <= this.config.projects.maxVersionNameLen() && this.config.projects.versionNameRegex().test(name);
    }

    public void testPageName(String name) {
        name = StringUtils.compact(name);
        if (BANNED_ROUTES.contains(name)) {
            throw new HangarApiException("page.new.error.invalidName");
        }

        this.config.pages.testPageName(name);
    }
}
