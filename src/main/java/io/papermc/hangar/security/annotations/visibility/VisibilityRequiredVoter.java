package io.papermc.hangar.security.annotations.visibility;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequiredMetadataExtractor.VisibilityRequiredAttribute;
import io.papermc.hangar.service.internal.projects.ProjectService;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class VisibilityRequiredVoter extends HangarDecisionVoter<VisibilityRequiredAttribute> {

    private final ProjectService projectService;

    @Autowired
    public VisibilityRequiredVoter(ProjectService projectService) {
        super(VisibilityRequiredAttribute.class);
        this.projectService = projectService;
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation method, Collection<ConfigAttribute> attributes) {
        VisibilityRequiredAttribute attribute = findAttribute(attributes);
        if (attribute == null) {
            return ACCESS_ABSTAIN;
        }
        Object[] arguments = attribute.getExpression().getValue(new MethodBasedEvaluationContext(
                method.getMethod().getDeclaringClass(),
                method.getMethod(),
                method.getArguments(),
                parameterNameDiscoverer
        ), Object[].class);
        if (arguments == null || !attribute.getType().getArgCount().contains(arguments.length)) {
            throw new IllegalStateException("Bad annotation configuration");
        }
        switch (attribute.getType()) {
            case PROJECT:
                if (arguments.length == 1 && projectService.getProjectTable((long) arguments[0]) != null) {
                    return ACCESS_GRANTED;
                } else if (projectService.getProjectTable((String) arguments[0], (String) arguments[1]) != null) {
                    return ACCESS_GRANTED;
                } else {
                    throw new HangarApiException(HttpStatus.NOT_FOUND);
                }
            case VERSION:
                throw new NotImplementedException();

        }
        return ACCESS_ABSTAIN;
    }
}
