package me.minidigger.hangar.service.project;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.model.Color;
import me.minidigger.hangar.util.HangarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectChannelDao;
import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.db.model.ProjectsTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public ProjectChannelsTable addProjectChannel(long projectId, String channelName, Color color) {
        InvalidChannelCreationReason reason = channelDao.get().validateChannelCreation(projectId, channelName, color.getValue(), hangarConfig.projects.getMaxChannels());
        if (reason != null) {
            if (reason == InvalidChannelCreationReason.MAX_CHANNELS) {
                throw new HangarException(reason.reason, String.valueOf(hangarConfig.projects.getMaxChannels()));
            } else {
                throw new HangarException(reason.reason);
            }
        }

        return channelFactory.createChannel(projectId, channelName, color);
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
