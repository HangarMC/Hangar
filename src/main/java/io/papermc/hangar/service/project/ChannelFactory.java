package io.papermc.hangar.service.project;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectChannelDao;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelFactory {

    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectChannelDao> channelDao;

    @Autowired
    public ChannelFactory(HangarConfig hangarConfig, HangarDao<ProjectChannelDao> channelDao) {
        this.hangarConfig = hangarConfig;
        this.channelDao = channelDao;
    }

    public ProjectChannelsTable createChannel(long projectId, String channelName, Color color, boolean isNonReviewed) {
        if (!hangarConfig.channels.isValidChannelName(channelName)) {
            throw new HangarException("error.channel.invalidName", channelName);
        }

        ProjectChannelsTable channel = new ProjectChannelsTable(channelName, color, projectId, isNonReviewed);
        channelDao.get().insert(channel);
        return channel;
    }
}
