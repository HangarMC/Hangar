package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.service.internal.projects.ChannelService;
import io.papermc.hangar.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectChannelResolver extends HangarModelResolver<ProjectChannelTable> {

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
                return null;
            }
            return this.channelService.getProjectChannel(projectIdLong, param);
        }

        return this.channelService.getProjectChannel(Long.parseLong(param));
    }
}
