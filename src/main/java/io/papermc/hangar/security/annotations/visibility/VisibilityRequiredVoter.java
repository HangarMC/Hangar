package io.papermc.hangar.security.annotations.visibility;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequiredMetadataExtractor.VisibilityRequiredAttribute;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.versions.VersionService;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class VisibilityRequiredVoter extends HangarDecisionVoter<VisibilityRequiredAttribute> {

    private final ProjectService projectService;
    private final VersionService versionService;

    @Autowired
    public VisibilityRequiredVoter(ProjectService projectService, VersionService versionService) {
        super(VisibilityRequiredAttribute.class);
        this.projectService = projectService;
        this.versionService = versionService;
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation method, @NotNull VisibilityRequiredAttribute attribute) {
        Object[] arguments = attribute.getExpression().getValue(getMethodEvaluationContext(method), Object[].class);
        if (arguments == null || !attribute.getType().getArgCount().contains(arguments.length)) {
            throw new IllegalStateException("Bad annotation configuration");
        }
        logger.debug("Resolved arguments: {}", Arrays.toString(arguments));
        switch (attribute.getType()) {
            case PROJECT:
                if (arguments.length == 1 && projectService.getProjectTable((long) arguments[0]) != null) {
                    return ACCESS_GRANTED;
                } else if (projectService.getProjectTable((String) arguments[0], (String) arguments[1]) != null) {
                    return ACCESS_GRANTED;
                } else {
                    return ACCESS_DENIED;
                }
            case VERSION:
                if (arguments.length == 1 && versionService.getProjectVersionTable((long) arguments[0]) != null) {
                    return ACCESS_GRANTED;
                } else {
                    if (versionService.getProjectVersionTable((String) arguments[0], (String) arguments[1], (String) arguments[2]) != null) {
                        return ACCESS_GRANTED;
                    } else {
                        return ACCESS_DENIED;
                    }
                }
        }
        return ACCESS_ABSTAIN;
    }

    @Override
    public void onAccessDenied() {
        throw HangarApiException.notFound();
    }
}
