package io.papermc.hangar.controller.extras;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public class ApiScope {

    private final ApiScopeType type;
    private final String owner;
    private final String slug;
    private final Long id;

    private ApiScope(ApiScopeType type, long id) {
        this.type = type;
        this.owner = null;
        this.slug = null;
        this.id = id;
    }

    private ApiScope(ApiScopeType apiScopeType, @Nullable String owner, @Nullable String slug) {
        this.type = apiScopeType;
        this.owner = owner;
        this.slug = slug;
        this.id = null;
    }

    private ApiScope(ApiScopeType apiScopeType, @Nullable String owner) {
        this(apiScopeType, owner, null);
    }

    @NotNull
    public ApiScopeType getType() {
        return type;
    }

    @NotNull
    public String getOwner() {
        if (owner == null) throw new IllegalStateException("Cannot call getOwner if owner is null");
        return owner;
    }

    @NotNull
    public String getSlug() {
        if (slug == null) throw new IllegalStateException("Cannot call getSlug if slug is null");
        return slug;
    }

    public Long getId() {
        return id;
    }

    public static ApiScope ofGlobal() {
        return new ApiScope(ApiScopeType.GLOBAL, null);
    }

    public static ApiScope ofProject(String author, String slug) {
        return new ApiScope(ApiScopeType.PROJECT, author, slug);
    }

    public static ApiScope ofProject(long id) {
        return new ApiScope(ApiScopeType.PROJECT, id);
    }

    public static ApiScope ofOrg(String organizationName) {
        return new ApiScope(ApiScopeType.ORGANIZATION, organizationName);
    }

    @Deprecated(forRemoval = true)
    public enum ApiScopeType {
        GLOBAL,
        PROJECT,
        ORGANIZATION
    }
}
