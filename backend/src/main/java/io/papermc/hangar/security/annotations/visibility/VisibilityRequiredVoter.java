package io.papermc.hangar.security.annotations.visibility;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.versions.VersionService;
import java.util.Arrays;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class VisibilityRequiredVoter extends HangarDecisionVoter<VisibilityRequiredMetadataExtractor.VisibilityRequiredAttribute> {

    private final ProjectService projectService;
    private final VersionService versionService;

    @Autowired
    public VisibilityRequiredVoter(final ProjectService projectService, final VersionService versionService) {
        super(VisibilityRequiredMetadataExtractor.VisibilityRequiredAttribute.class);
        this.projectService = projectService;
        this.versionService = versionService;
    }

    @Override
    public int vote(final Authentication authentication, final MethodInvocation method, final @NotNull VisibilityRequiredMetadataExtractor.VisibilityRequiredAttribute attribute) {
        final Object[] arguments = attribute.expression().getValue(this.getMethodEvaluationContext(method), Object[].class);
        if (arguments == null || !attribute.type().getArgCount().contains(arguments.length)) {
            throw new IllegalStateException("Bad annotation configuration");
        }
        this.logger.debug("Resolved arguments: {}", Arrays.toString(arguments));
        switch (attribute.type()) {
            case PROJECT:
                if (arguments.length == 1 && this.projectService.getProjectTable((long) arguments[0]) != null) {
                    return ACCESS_GRANTED;
                } else if (this.projectService.getProjectTable((String) arguments[0], (String) arguments[1]) != null) {
                    return ACCESS_GRANTED;
                } else {
                    return ACCESS_DENIED;
                }
            case VERSION:
                if (arguments.length == 1 && this.versionService.getProjectVersionTable((long) arguments[0]) != null) {
                    return ACCESS_GRANTED;
                } else {
                    if (this.versionService.getProjectVersionTable((String) arguments[0], (String) arguments[1], (String) arguments[2]) != null) { // TODO is platform needed here?
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
