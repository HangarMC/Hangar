package io.papermc.hangar.service;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Set;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.util.StringUtils;

@Service
public class ValidationService {

    private final HangarConfig config;

    private static final Set<String> bannedRoutes = Set.of("api", "authors", "linkout", "logged-out", "new", "unread", "notifications", "staff", "admin", "organizations", "tools", "recommended", "null", "undefined", "tos", "settings");

    public ValidationService(HangarConfig config) {
        this.config = config;
    }

    public boolean isValidUsername(String name) {
        name = StringUtils.compact(name);
        if (bannedRoutes.contains(name)) {
            return false;
        }
        if (name.length() < 1) {
            return false;
        }
        return true;
    }

    public @Nullable String isValidProjectName(String name) {
        name = StringUtils.compact(name);
        String error = null;
        if (bannedRoutes.contains(name)) {
            error = "invalidName";
        } else if (name.length() < 3) {
            error = "tooShortName";
        } else if (name.length() > config.projects.getMaxNameLen()) {
            error = "tooLongName";
        } else if (name.contains(ProjectFactory.SOFT_DELETION_SUFFIX) || !config.projects.getNameMatcher().test(name)) {
            error = "invalidName";
        }
        return error != null ? "project.new.error." + error : null;
    }

    public boolean isValidVersionName(String name) {
        name = StringUtils.compact(name);
        if (bannedRoutes.contains(name) || name.contains(ProjectFactory.SOFT_DELETION_SUFFIX)) {
            return false;
        }
        if (name.length() < 1 || name.length() > config.projects.getMaxVersionNameLen() || !config.projects.getVersionNameMatcher().test(name)) {
            return false;
        }
        return true;
    }

    public void testPageName(String name) {
        name = StringUtils.compact(name);
        if (bannedRoutes.contains(name)) {
            throw new HangarApiException("page.new.error.invalidName");
        }

        config.pages.testPageName(name);
    }
}
