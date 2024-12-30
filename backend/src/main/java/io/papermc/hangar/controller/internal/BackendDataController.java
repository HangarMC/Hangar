package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.service.OAuthService;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.dao.internal.table.roles.RolesDAO;
import io.papermc.hangar.model.Announcement;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.PlatformVersion;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.common.roles.RoleData;
import io.papermc.hangar.model.internal.api.responses.Security;
import io.papermc.hangar.model.internal.api.responses.Validations;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.service.internal.PlatformService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Anyone
@RateLimit(path = "backenddata")
@RequestMapping(path = "/api/internal/data", produces = MediaType.APPLICATION_JSON_VALUE)
public class BackendDataController extends HangarComponent {

    private final HangarConfig config;
    private final PlatformService platformService;
    private final Optional<GitProperties> gitProperties;
    private final RolesDAO rolesDAO;
    private final OAuthService oAuthService;

    @Autowired
    public BackendDataController(final HangarConfig config, final PlatformService platformService, final Optional<GitProperties> gitProperties, final RolesDAO rolesDAO, final OAuthService oAuthService) {
        this.config = config;
        this.platformService = platformService;
        this.gitProperties = gitProperties;
        this.rolesDAO = rolesDAO;
        this.oAuthService = oAuthService;
    }

    @GetMapping("/categories")
    @Cacheable(CacheConfig.CATEGORIES)
    public List<CategoryData> getCategories() {
        return Arrays.stream(Category.values()).map(category -> new CategoryData(category.getIcon(), category.getApiName(), category.isVisible(), "project.category." + category.getApiName())).toList();
    }

    public record CategoryData(String icon, String apiName, boolean visible, String title) {
    }

    @GetMapping("/permissions")
    @Cacheable(CacheConfig.PERMISSIONS)
    public List<PermissionData> getPermissions() {
        return Arrays.stream(NamedPermission.getValues()).map(permission -> new PermissionData(permission.getValue(), permission.getFrontendName(), permission.getPermission().toBinString())).toList();
    }

    public record PermissionData(String value, String frontendName, String permission) {
    }

    @GetMapping("/platforms")
    @Cacheable(CacheConfig.PLATFORMS)
    public List<PlatformData> getPlatforms() {
        return Arrays.stream(Platform.values()).map(platform -> new PlatformData(platform.getName(), platform.getCategory(), platform.getUrl(), platform.getEnumName(), platform.isVisible(), this.platformService.getDescendingVersionsForPlatform(platform))).toList();
    }

    public record PlatformData(String name, Platform.Category category, String url, String enumName, boolean visible, List<PlatformVersion> platformVersions) {
    }

    @GetMapping("/channelColors")
    @Cacheable(CacheConfig.CHANNEL_COLORS)
    public List<ColorData> getColors() {
        return Color.getNonTransparentValues().stream().map(color -> new ColorData(color.name(), color.getHex())).toList();
    }

    public record ColorData(String name, String hex) {
    }

    @Secured("ROLE_USER")
    @GetMapping("/flagReasons")
    @Cacheable(CacheConfig.FLAG_REASONS)
    public List<FlagReasonData> getFlagReasons() {
        return Arrays.stream(FlagReason.getValues()).map(flagReason -> new FlagReasonData(flagReason.name(), flagReason.getTitle())).toList();
    }

    public record FlagReasonData(String type, String title) {
    }

    @GetMapping("/announcements")
    @Cacheable(CacheConfig.ANNOUNCEMENTS)
    public List<Announcement> getAnnouncements() {
        return this.config.getAnnouncements();
    }

    @GetMapping("/projectRoles")
    @Cacheable(CacheConfig.PROJECT_ROLES)
    public List<RoleData> getProjectRoles() {
        return this.rolesDAO.getRoles(RoleCategory.PROJECT);
    }

    @GetMapping("/globalRoles")
    @Cacheable(CacheConfig.GLOBAL_ROLES)
    public List<RoleData> getGlobalRoles() {
        return this.rolesDAO.getRoles(RoleCategory.GLOBAL);
    }

    @GetMapping("/orgRoles")
    @Cacheable(CacheConfig.ORG_ROLES)
    public List<RoleData> getOrganizationRoles() {
        return this.rolesDAO.getRoles(RoleCategory.ORGANIZATION);
    }

    @GetMapping("/licenses")
    @Cacheable(CacheConfig.LICENSES)
    public List<String> getLicenses() {
        return this.config.getLicenses();
    }

    @GetMapping("/visibilities")
    @Cacheable(CacheConfig.VISIBILITIES)
    public List<VisibilityData> getVisibilities() {
        return Arrays.stream(Visibility.values()).map(visibility -> new VisibilityData(visibility.getName(), visibility.shouldShowModal(), visibility.canChangeTo(), visibility.getCssClass(), visibility.getTitle())).toList();
    }

    public record VisibilityData(String name, boolean showModal, boolean canChangeTo, String cssClass, String title) {
    }

    @GetMapping("/prompts")
    @Cacheable(CacheConfig.PROMPTS)
    public List<PromptData> getPrompts() {
        return Arrays.stream(Prompt.getValues()).map(prompt -> new PromptData(prompt.name(), prompt.getTitleKey(), prompt.getMessageKey())).toList();
    }

    public record PromptData(String name, String titleKey, String messageKey) {
    }

    @GetMapping("/version-info")
    @Cacheable(CacheConfig.VERSION_INFO)
    public VersionInfo info() {
        return new VersionInfo(
            this.get("build.version", "-1"),
            this.get("commit.user.name", "dummy"),
            this.get("commit.time", "-1"),
            this.gitProperties.map(GitProperties::getCommitId).orElse("0"),
            this.gitProperties.map(GitProperties::getShortCommitId).orElse("0"),
            this.get("commit.message.short", "dummy"),
            this.gitProperties.map(gp -> gp.get("tags")).or(() -> this.gitProperties.map(gp -> gp.get("closest.tag.name"))).orElse("dummy"),
            this.get("closest.tag.commit.count", "0")
        );
    }

    public record VersionInfo(String version, String committer, String time, String commit, String commitShort, String message, String tag, String behind) {
    }

    private String get(final String propName, final String fallback) {
        return this.gitProperties.map(gp -> gp.get(propName)).orElse(fallback);
    }

    @GetMapping("/validations")
    @Cacheable(CacheConfig.VALIDATIONS)
    public Validations getValidations() {
        return Validations.create(this.config);
    }

    @GetMapping("/loggedActions")
    @Cacheable(CacheConfig.LOGGED_ACTIONS)
    public List<String> getLoggedActions() {
        return LogAction.LOG_REGISTRY.keySet().stream().sorted().toList();
    }

    @GetMapping("/security")
    public Security getSecurity() {
        return new Security(this.config.security.safeDownloadHosts(), this.oAuthService.getProviders().keySet());
    }
}
