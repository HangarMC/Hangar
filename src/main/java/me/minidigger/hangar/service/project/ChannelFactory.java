package me.minidigger.hangar.service.project;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectChannelDao;
import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.model.Color;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.util.HangarException;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
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

    public ProjectChannelsTable createChannel(long projectId, String channelName, Color color) {
        if (!hangarConfig.channels.isValidChannelName(channelName)) {
            throw new HangarException("error.channel.invalidName", channelName);
        }
        ProjectChannelsTable channel = new ProjectChannelsTable(channelName, color, projectId);
        channelDao.get().insert(channel);
        return channel;
    }
}
