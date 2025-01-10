package io.papermc.hangar.security.annotations.permission;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.service.PermissionService;
import java.util.Arrays;
import java.util.Set;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PermissionRequiredVoter extends HangarDecisionVoter<PermissionRequiredMetadataExtractor.PermissionRequiredAttribute> {

    private final PermissionService permissionService;

    @Autowired
    public PermissionRequiredVoter(final PermissionService permissionService) {
        super(PermissionRequiredMetadataExtractor.PermissionRequiredAttribute.class);
        this.permissionService = permissionService;
        this.setAllowMultipleAttributes(true);
    }

    @Override
    public int vote(final Authentication authentication, final MethodInvocation methodInvocation, final Set<PermissionRequiredMetadataExtractor.PermissionRequiredAttribute> attributes) {
        Long userId = null;
        if (authentication instanceof final HangarAuthenticationToken hangarAuthenticationToken) {
            this.logger.debug("Possible permissions: {}", hangarAuthenticationToken.getPrincipal().getPossiblePermissions());
            userId = hangarAuthenticationToken.getUserId();
        }

        for (final PermissionRequiredMetadataExtractor.PermissionRequiredAttribute attribute : attributes) {
            final Object[] arguments = attribute.expression().getValue(this.getMethodEvaluationContext(methodInvocation), Object[].class);
            if (arguments == null || !attribute.permissionType().getArgCounts().contains(arguments.length)) {
                throw new IllegalStateException("Bad annotation configuration");
            }

            final Permission requiredPerm = Arrays.stream(attribute.permissions()).map(NamedPermission::getPermission).reduce(Permission::add).orElse(Permission.None);
            this.logger.debug("Required permissions: {}", requiredPerm);

            final Permission currentPerm;
            switch (attribute.permissionType()) {
                case PROJECT -> {
                    if (arguments.length == 1) {
                        final long projectId;
                        final Object argument1 = arguments[0];
                        if (argument1 instanceof final String projectName) {
                            currentPerm = this.permissionService.getProjectPermissions(userId, projectName);
                            break;
                        }

                        if (argument1 instanceof final ProjectTable table) {
                            projectId = table.getId();
                        } else if (argument1 instanceof final Long id){
                            projectId = id;
                        } else {
                            throw new IllegalStateException("Bad annotation configuration, expected ProjectTable or Long but got " + argument1 + " for expression " + attribute.expression().getExpressionString());
                        }

                        currentPerm = this.permissionService.getProjectPermissions(userId, projectId);
                    } else {
                        currentPerm = Permission.None;
                    }
                }
                case ORGANIZATION -> {
                    if (arguments.length == 1) {
                        currentPerm = switch (arguments[0]) {
                            case final Long id -> this.permissionService.getOrganizationPermissions(userId, id);
                            case final String name -> this.permissionService.getOrganizationPermissions(userId, name);
                            case final OrganizationTable org -> this.permissionService.getOrganizationPermissions(userId, org.getId());
                            case null, default ->
                                throw new IllegalStateException("Bad annotation configuration, expected Long or String but got " + arguments[0] + " for expression " + attribute.expression().getExpressionString());
                        };
                    } else {
                        currentPerm = Permission.None;
                    }
                }
                case GLOBAL -> currentPerm = this.permissionService.getGlobalPermissions(userId);
                default -> currentPerm = Permission.None;
            }

            this.logger.debug("Current permissions: {}", currentPerm);
            if (authentication instanceof final HangarAuthenticationToken hangarAuthenticationToken) {
                if (!hangarAuthenticationToken.getPrincipal().isAllowed(requiredPerm, currentPerm)) {
                    throw new HangarApiException(HttpStatus.NOT_FOUND);
                }
            } else if (!this.isAllowed(requiredPerm, currentPerm)) {
                throw new HangarApiException(HttpStatus.NOT_FOUND);
            }
        }
        return ACCESS_GRANTED;
    }

    @Override
    public void onAccessDenied() {
        throw HangarApiException.notFound();
    }

    private boolean isAllowed(final Permission requiredPermission, final Permission currentPermission) {
        final Permission intersect = requiredPermission.intersect(currentPermission);
        if (intersect.isNone()) {
            return false;
        }

        return Permission.All.has(intersect);
    }
}
