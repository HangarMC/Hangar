package io.papermc.hangar.security.annotations.visibility;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.versions.VersionService;
import io.papermc.hangar.service.internal.visibility.ProjectVersionVisibilityService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import java.util.Arrays;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class VisibilityRequiredVoter extends HangarDecisionVoter<VisibilityRequiredMetadataExtractor.VisibilityRequiredAttribute> {

    private final ProjectService projectService;
    private final ProjectVisibilityService projectVisibilityService;
    private final VersionService versionService;
    private final ProjectVersionVisibilityService projectVersionVisibilityService;

    @Autowired
    public VisibilityRequiredVoter(final ProjectService projectService, final ProjectVisibilityService projectVisibilityService, final VersionService versionService, final ProjectVersionVisibilityService projectVersionVisibilityService) {
        super(VisibilityRequiredMetadataExtractor.VisibilityRequiredAttribute.class);
        this.projectService = projectService;
        this.projectVisibilityService = projectVisibilityService;
        this.versionService = versionService;
        this.projectVersionVisibilityService = projectVersionVisibilityService;
    }

    @Override
    public int vote(final Authentication authentication, final MethodInvocation method, final @NotNull VisibilityRequiredMetadataExtractor.VisibilityRequiredAttribute attribute) {
        final Object[] arguments = attribute.expression().getValue(this.getMethodEvaluationContext(method), Object[].class);
        if (arguments == null || !attribute.type().getArgCount().contains(arguments.length)) {
            throw new IllegalStateException("Bad annotation configuration on " + method.getMethod().getDeclaringClass().getName() + "#" + method.getMethod().getName());
        }
        this.logger.debug("Resolved arguments: {}", Arrays.toString(arguments));
        switch (attribute.type()) {
            case PROJECT:
                if (arguments.length == 1) {
                    if (arguments[0] instanceof ProjectTable projectTable) {
                        if (this.projectVisibilityService.checkVisibility(projectTable) != null) {
                            return ACCESS_GRANTED;
                        }
                    }
                    else if (arguments[0] instanceof String slug) {
                        if (this.projectService.getProjectTable(slug) != null) {
                            return ACCESS_GRANTED;
                        }
                    } else if (this.projectService.getProjectTable((long) arguments[0]) != null) {
                        return ACCESS_GRANTED;
                    }
                }
                return ACCESS_DENIED;
            case VERSION:
                if (arguments.length == 1) {
                    if (arguments[0] instanceof final ProjectVersionTable projectVersionTable) {
                        if (this.projectVersionVisibilityService.checkVisibility(projectVersionTable) != null) {
                            return ACCESS_GRANTED;
                        }
                    } else if (this.versionService.getProjectVersionTable((long) arguments[0]) != null) {
                        return ACCESS_GRANTED;
                    }
                } else if (arguments.length == 2) {
                    if (arguments[1] instanceof final ProjectVersionTable projectVersionTable) {
                        if (this.projectVersionVisibilityService.checkVisibility(projectVersionTable) != null) {
                            return ACCESS_GRANTED;
                        }
                    } else if (this.versionService.getProjectVersionTable((String) arguments[0], (String) arguments[1]) != null) { // TODO is platform needed here?
                        return ACCESS_GRANTED;
                    }
                }
                return ACCESS_DENIED;
        }
        return ACCESS_ABSTAIN;
    }

    @Override
    public void onAccessDenied() {
        throw HangarApiException.notFound();
    }
}
