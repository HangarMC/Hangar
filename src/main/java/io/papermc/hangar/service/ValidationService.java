package io.papermc.hangar.service;

import org.springframework.stereotype.Service;

import java.util.Set;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.util.StringUtils;

@Service
public class ValidationService {

    private final HangarConfig config;

    private static final Set<String> bannedRoutes = Set.of("api", "authors", "linkout", "logged-out", "new", "notifications", "staff", "admin", "organizations", "tools", "recommended", "null", "undefined");

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

    public boolean isValidProjectName(String name) {
        name = StringUtils.compact(name);
        if (bannedRoutes.contains(name)) {
            return false;
        }
        if (name.length() < 1 || name.length() > config.projects.getMaxNameLen() || !config.projects.getNameMatcher().test(name)) {
            return false;
        }
        return true;
    }

    public boolean isValidVersionName(String name) {
        name = StringUtils.compact(name);
        if (bannedRoutes.contains(name)) {
            return false;
        }
        if (name.length() < 1 || name.length() > config.projects.getMaxVersionNameLen() || !config.projects.getVersionNameMatcher().test(name)) {
            return false;
        }
        return true;
    }

    public boolean isValidPageName(String name) {
        name = StringUtils.compact(name);
        if (bannedRoutes.contains(name)) {
            return false;
        }
        if (name.length() < 1 || name.length() > config.projects.getMaxPageNameLen() || !config.projects.getPageNameMatcher().test(name)) {
            return false;
        }
        return true;
    }
}
