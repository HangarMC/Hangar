package io.papermc.hangar.security.annotations.permission;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.security.authorization.HangarAuthorizationManager;
import io.papermc.hangar.service.PermissionService;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Authorization manager for {@link PermissionRequired} annotation.
 * Handles project, organization, and global permission checks.
 */
@Component
public class PermissionRequiredAuthorizationManager extends HangarAuthorizationManager {

    private static final Logger logger = LoggerFactory.getLogger(PermissionRequiredAuthorizationManager.class);

    private final PermissionService permissionService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Autowired
    public PermissionRequiredAuthorizationManager(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation methodInvocation) {
        // Check if method or class has @PermissionRequired annotation (including repeated)
        Method method = methodInvocation.getMethod();
        Class<?> targetClass = methodInvocation.getThis() != null ? methodInvocation.getThis().getClass() : method.getDeclaringClass();
        
        Set<PermissionRequired> annotations = AnnotationUtils.getRepeatableAnnotations(method, PermissionRequired.class);
        if (annotations.isEmpty()) {
            annotations = AnnotationUtils.getRepeatableAnnotations(targetClass, PermissionRequired.class);
        }
        
        if (annotations.isEmpty()) {
            // Abstain if annotation not present
            return null;
        }
        
        Authentication auth = authentication.get();
        Long userId = null;
        if (auth instanceof final HangarAuthenticationToken hangarAuthenticationToken) {
            logger.debug("Possible permissions: {}", hangarAuthenticationToken.getPrincipal().getPossiblePermissions());
            userId = hangarAuthenticationToken.getUserId();
        }
        
        // Check each permission requirement
        for (PermissionRequired annotation : annotations) {
            this.checkPermission(auth, userId, methodInvocation, annotation);
        }
        
        return this.granted();
    }

    private void checkPermission(Authentication auth, Long userId, MethodInvocation methodInvocation, PermissionRequired annotation) {
        // Evaluate SpEL expression to get the arguments
        final MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
            methodInvocation.getMethod().getDeclaringClass(),
            methodInvocation.getMethod(),
            methodInvocation.getArguments(),
            this.parameterNameDiscoverer
        );
        
        final Object[] arguments = this.expressionParser.parseExpression(annotation.args()).getValue(context, Object[].class);
        if (arguments == null || !annotation.type().getArgCounts().contains(arguments.length)) {
            throw new IllegalStateException("Bad annotation configuration");
        }
        
        final Permission requiredPerm = Arrays.stream(annotation.perms())
            .map(NamedPermission::getPermission)
            .reduce(Permission::add)
            .orElse(Permission.None);
        logger.debug("Required permissions: {}", requiredPerm);
        
        final Permission currentPerm;
        switch (annotation.type()) {
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
                    } else if (argument1 instanceof final Long id) {
                        projectId = id;
                    } else {
                        throw new IllegalStateException("Bad annotation configuration, expected ProjectTable or Long but got " + 
                            (argument1 != null ? argument1.getClass().getName() : "null"));
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
                            throw new IllegalStateException("Bad annotation configuration, expected Long or String but got " + 
                                (arguments[0] != null ? arguments[0].getClass().getName() : "null"));
                    };
                } else {
                    currentPerm = Permission.None;
                }
            }
            case GLOBAL -> currentPerm = this.permissionService.getGlobalPermissions(userId);
            default -> currentPerm = Permission.None;
        }
        
        logger.debug("Current permissions: {}", currentPerm);
        if (auth instanceof final HangarAuthenticationToken hangarAuthenticationToken) {
            if (!hangarAuthenticationToken.getPrincipal().isAllowed(requiredPerm, currentPerm)) {
                throw new HangarApiException(HttpStatus.NOT_FOUND);
            }
        } else if (!this.isAllowed(requiredPerm, currentPerm)) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
    }

    private boolean isAllowed(final Permission requiredPermission, final Permission currentPermission) {
        final Permission intersect = requiredPermission.intersect(currentPermission);
        if (intersect.isNone()) {
            return false;
        }
        
        return Permission.All.has(intersect);
    }
}
