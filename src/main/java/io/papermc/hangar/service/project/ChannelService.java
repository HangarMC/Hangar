package io.papermc.hangar.service.project;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectChannelDao;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.model.Color;
import io.papermc.hangar.util.HangarException;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChannelService {

    private final HangarConfig hangarConfig;
    private final ChannelFactory channelFactory;
    private final HangarDao<ProjectChannelDao> channelDao;

    @Autowired
    public ChannelService(HangarConfig hangarConfig, ChannelFactory channelFactory, HangarDao<ProjectChannelDao> channelDao) {
        this.hangarConfig = hangarConfig;
        this.channelFactory = channelFactory;
        this.channelDao = channelDao;
    }

    public ProjectChannelsTable getFirstChannel(ProjectsTable project) {
        return channelDao.get().getFirstChannel(project.getId());
    }

    public ProjectChannelsTable getProjectChannel(long projectId, String channelName) {
        return channelDao.get().getProjectChannel(projectId, channelName, null);
    }

    public ProjectChannelsTable getProjectChannel(long projectId, long channelId) {
        return channelDao.get().getProjectChannel(projectId, null, channelId);
    }

    public List<ProjectChannelsTable> getProjectChannels(long projectId) {
        return channelDao.get().getProjectChannels(projectId);
    }

    public Map<ProjectChannelsTable, Integer> getChannelsWithVersionCount(long projectId) {
        return channelDao.get().getChannelsWithVersionCount(projectId);
    }

    public ProjectChannelsTable addProjectChannel(long projectId, String channelName, Color color) {
        InvalidChannelCreationReason reason = channelDao.get().validateChannelCreation(projectId, channelName, color.getValue(), hangarConfig.projects.getMaxChannels());
        checkInvalidChannelCreationReason(reason);
        return channelFactory.createChannel(projectId, channelName, color);
    }

    public void updateProjectChannel(long projectId, String oldChannel, String channelName, Color color) {
        if (!hangarConfig.channels.isValidChannelName(channelName)) {
            throw new HangarException("error.channel.invalidName", channelName);
        }

        InvalidChannelCreationReason reason = channelDao.get().validateChanneUpdate(projectId, oldChannel, channelName, color.getValue());
        checkInvalidChannelCreationReason(reason);
        channelDao.get().update(projectId, oldChannel, channelName, color);
    }

    private void checkInvalidChannelCreationReason(@Nullable InvalidChannelCreationReason reason) {
        if (reason != null) {
            if (reason == InvalidChannelCreationReason.MAX_CHANNELS) {
                throw new HangarException(reason.reason, String.valueOf(hangarConfig.projects.getMaxChannels()));
            } else {
                throw new HangarException(reason.reason);
            }
        }
    }

    public enum InvalidChannelCreationReason {
        MAX_CHANNELS("error.channel.maxChannels"),
        DUPLICATE_NAME("error.channel.duplicateName"),
        UNIQUE_COLOR("error.channel.duplicateColor");

        private final String reason;

        InvalidChannelCreationReason(String reason) {
            this.reason = reason;
        }
    }
}
