package io.papermc.hangar.security.authorization;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.versions.VersionService;
import io.papermc.hangar.service.internal.visibility.ProjectVersionVisibilityService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Authorization manager for {@link VisibilityRequired} annotation.
 * Checks project and version visibility requirements.
 */
@Component
public class VisibilityRequiredAuthorizationManager extends HangarAuthorizationManager {

    private final ProjectService projectService;
    private final ProjectVisibilityService projectVisibilityService;
    private final VersionService versionService;
    private final ProjectVersionVisibilityService projectVersionVisibilityService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Autowired
    public VisibilityRequiredAuthorizationManager(
            ProjectService projectService,
            ProjectVisibilityService projectVisibilityService,
            VersionService versionService,
            ProjectVersionVisibilityService projectVersionVisibilityService) {
        this.projectService = projectService;
        this.projectVisibilityService = projectVisibilityService;
        this.versionService = versionService;
        this.projectVersionVisibilityService = projectVersionVisibilityService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation methodInvocation) {
        // Check if method or class has @VisibilityRequired annotation
        Method method = methodInvocation.getMethod();
        Class<?> targetClass = methodInvocation.getThis() != null ? methodInvocation.getThis().getClass() : method.getDeclaringClass();
        
        VisibilityRequired methodAnnotation = AnnotationUtils.findAnnotation(method, VisibilityRequired.class);
        VisibilityRequired classAnnotation = AnnotationUtils.findAnnotation(targetClass, VisibilityRequired.class);
        VisibilityRequired annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        
        if (annotation == null) {
            // Abstain if annotation not present
            return null;
        }
        
        // Evaluate SpEL expression to get the arguments
        final MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
            method.getDeclaringClass(),
            method,
            methodInvocation.getArguments(),
            this.parameterNameDiscoverer
        );
        
        final Object[] arguments = this.expressionParser.parseExpression(annotation.args()).getValue(context, Object[].class);
        if (arguments == null || !annotation.type().getArgCount().contains(arguments.length)) {
            throw new IllegalStateException("Bad annotation configuration on " + method.getDeclaringClass().getName() + "#" + method.getName());
        }
        
        switch (annotation.type()) {
            case PROJECT:
                if (arguments.length == 1) {
                    if (arguments[0] instanceof ProjectTable projectTable) {
                        if (this.projectVisibilityService.checkVisibility(projectTable) != null) {
                            return granted();
                        }
                    } else if (arguments[0] instanceof String slug) {
                        if (this.projectService.getProjectTable(slug) != null) {
                            return granted();
                        }
                    } else if (this.projectService.getProjectTable((long) arguments[0]) != null) {
                        return granted();
                    }
                }
                throw HangarApiException.notFound();
            case VERSION:
                if (arguments.length == 1) {
                    if (arguments[0] instanceof final ProjectVersionTable projectVersionTable) {
                        if (this.projectVersionVisibilityService.checkVisibility(projectVersionTable) != null) {
                            return granted();
                        }
                    } else if (this.versionService.getProjectVersionTable((long) arguments[0]) != null) {
                        return granted();
                    }
                } else if (arguments.length == 2) {
                    if (arguments[1] instanceof final ProjectVersionTable projectVersionTable) {
                        if (this.projectVersionVisibilityService.checkVisibility(projectVersionTable) != null) {
                            return granted();
                        }
                    } else if (this.versionService.getProjectVersionTable((String) arguments[0], (String) arguments[1]) != null) {
                        return granted();
                    }
                }
                throw HangarApiException.notFound();
        }
        
        // Abstain for unknown types
        return null;
    }
}
