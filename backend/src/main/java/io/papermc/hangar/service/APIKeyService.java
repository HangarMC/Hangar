package io.papermc.hangar.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarApiKeysDAO;
import io.papermc.hangar.db.dao.internal.table.auth.ApiKeyDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.ApiKey;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.model.identified.UserIdentified;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import io.papermc.hangar.model.internal.api.responses.ScopableProject;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.util.CryptoUtils;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class APIKeyService extends HangarComponent {

    private final ApiKeyDAO apiKeyDAO;
    private final HangarApiKeysDAO hangarApiKeysDAO;
    private final ProjectsDAO projectsDAO;

    @Autowired
    public APIKeyService(final ApiKeyDAO apiKeyDAO, final HangarApiKeysDAO hangarApiKeysDAO, final ProjectsDAO projectsDAO) {
        this.apiKeyDAO = apiKeyDAO;
        this.hangarApiKeysDAO = hangarApiKeysDAO;
        this.projectsDAO = projectsDAO;
    }

    public List<ApiKey> getApiKeys(final long userId) {
        return this.hangarApiKeysDAO.getUserApiKeys(userId);
    }

    public List<ScopableProject> getScopableProjects(final long userId) {
        return this.hangarApiKeysDAO.getScopableProjects(userId);
    }

    public void checkName(final UserIdentified userIdentified, final String name) {
        if (this.apiKeyDAO.getByUserAndName(userIdentified.getUserId(), name) != null) {
            throw new HangarApiException("apiKeys.error.duplicateName");
        }
    }

    @Transactional
    public String createApiKey(final UserIdentified userIdentified, final CreateAPIKeyForm apiKeyForm, final Permission possiblePermissions) {
        final Permission keyPermission = apiKeyForm.permissions().stream().map(NamedPermission::getPermission).reduce(Permission::add).orElse(Permission.None);
        if (!possiblePermissions.has(keyPermission)) {
            throw new HangarApiException("apiKeys.error.notEnoughPerms");
        }

        this.checkName(userIdentified, apiKeyForm.name());

        final OffsetDateTime expiresAt = this.validateExpiration(apiKeyForm.expiresAt());
        final Set<Long> projectIds = this.resolveProjects(apiKeyForm.projects());
        final boolean projectScoped = !projectIds.isEmpty();

        final UUID tokenIdentifier = UUID.randomUUID();
        final String token = UUID.randomUUID().toString();
        final String hashedToken = CryptoUtils.hmacSha256(this.config.security().tokenSecret(), token.getBytes(StandardCharsets.UTF_8));
        final long keyId = this.apiKeyDAO.insert(new ApiKeyTable(apiKeyForm.name(), userIdentified.getUserId(), tokenIdentifier, hashedToken, keyPermission, expiresAt, projectScoped));
        if (projectScoped) {
            this.apiKeyDAO.insertScopedProjects(keyId, projectIds);
        }

        final StringBuilder log = new StringBuilder("Key '").append(apiKeyForm.name()).append("': ")
            .append(apiKeyForm.permissions().stream().map(NamedPermission::getFrontendName).collect(Collectors.joining(", ")));
        if (projectScoped) {
            log.append(" | limited to projects: ").append(String.join(", ", apiKeyForm.projects()));
        }
        if (expiresAt != null) {
            log.append(" | expires at: ").append(expiresAt);
        }
        this.actionLogger.user(LogAction.USER_APIKEY_CREATED.create(UserContext.of(userIdentified.getUserId()), log.toString(), ""));
        return tokenIdentifier + "." + token;
    }

    @Transactional
    public void deleteApiKey(final UserIdentified userIdentified, final String keyName) {
        if (this.apiKeyDAO.delete(keyName, userIdentified.getUserId()) == 0) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        this.actionLogger.user(LogAction.USER_APIKEY_DELETED.create(UserContext.of(userIdentified.getUserId()), "", "Key '" + keyName + "'"));
    }

    private @Nullable OffsetDateTime validateExpiration(final @Nullable OffsetDateTime expiresAt) {
        if (expiresAt == null) {
            return null;
        }
        if (!expiresAt.isAfter(OffsetDateTime.now())) {
            throw new HangarApiException("apiKeys.error.expirationInPast");
        }
        return expiresAt;
    }

    private Set<Long> resolveProjects(final @Nullable Set<String> slugs) {
        if (slugs == null || slugs.isEmpty()) {
            return Set.of();
        }

        final Set<Long> ids = new LinkedHashSet<>();
        for (final String slug : slugs) {
            final Long id = this.projectsDAO.getIdBySlug(slug);
            if (id == null) {
                throw new HangarApiException("apiKeys.error.unknownProject", slug);
            }
            ids.add(id);
        }
        return ids;
    }
}
