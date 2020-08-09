package me.minidigger.hangar.controller.util;

public class ApiScope {

    private final ApiScopeType type;
    private final String value;

    private ApiScope(ApiScopeType apiScopeType, String value) {
        this.type = apiScopeType;
        this.value = value;
    }

    public ApiScopeType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public static ApiScope forGlobal() {
        return new ApiScope(ApiScopeType.GLOBAL, null);
    }

    public static ApiScope forProject(String pluginId) {
        return new ApiScope(ApiScopeType.PROJECT, pluginId);
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
