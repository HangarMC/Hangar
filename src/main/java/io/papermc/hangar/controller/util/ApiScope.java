package io.papermc.hangar.controller.util;

import org.jetbrains.annotations.Nullable;

public class ApiScope {

    private final ApiScopeType type;
    private final String owner;
    private final String slug;

    private ApiScope(ApiScopeType apiScopeType, @Nullable String owner, @Nullable String slug) {
        this.type = apiScopeType;
        this.owner = owner;
        this.slug = slug;
    }

    private ApiScope(ApiScopeType apiScopeType, @Nullable String owner) {
        this(apiScopeType, owner, null);
    }

    public ApiScopeType getType() {
        return type;
    }

    @Nullable
    public String getOwner() {
        return owner;
    }

    @Nullable
    public String getSlug() {
        return slug;
    }

    public static ApiScope forGlobal() {
        return new ApiScope(ApiScopeType.GLOBAL, null);
    }

    public static ApiScope forProject(String author, String slug) {
        return new ApiScope(ApiScopeType.PROJECT, author, slug);
    }

    public static ApiScope forOrg(String organizationName) {
        return new ApiScope(ApiScopeType.ORGANIZATION, organizationName);
    }

    public enum ApiScopeType {
        GLOBAL,
        PROJECT,
        ORGANIZATION
    }
}
