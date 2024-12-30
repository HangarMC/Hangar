package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.service.internal.projects.ChannelService;
import io.papermc.hangar.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectChannelResolver extends HangarModelPathVarResolver<ProjectChannelTable> {

    private final ChannelService channelService;

    @Autowired
    public ProjectChannelResolver(final ChannelService channelService) {
        this.channelService = channelService;
    }

    @Override
    protected Class<ProjectChannelTable> modelType() {
        return ProjectChannelTable.class;
    }

    @Override
    protected ProjectChannelTable resolveParameter(final @NotNull String param, final NativeWebRequest request) {
        if (!StringUtils.isLong(param)) {
            final Object projectId = request.getAttribute("projectId", NativeWebRequest.SCOPE_REQUEST);
            if (!(projectId instanceof final Long projectIdLong)) {
                throw new HangarApiException(HttpStatus.NOT_FOUND);
            }
            final ProjectChannelTable projectTable = this.channelService.getProjectChannel(projectIdLong, param);
            if (projectTable != null) {
                return projectTable;
            }
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        return this.channelService.getProjectChannel(Long.parseLong(param));
    }
}
