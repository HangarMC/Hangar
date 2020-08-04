package me.minidigger.hangar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectChannelDao;
import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.db.model.ProjectsTable;

import java.util.List;

@Service
public class ChannelService {

    private final HangarDao<ProjectChannelDao> channelDao;

    @Autowired
    public ChannelService(HangarDao<ProjectChannelDao> channelDao) {
        this.channelDao = channelDao;
    }

    public ProjectChannelsTable getFirstChannel(ProjectsTable project) {
        // TODO get first channel

        return null;
    }

    public List<ProjectChannelsTable> getProjectChannels(long projectId) {
        return channelDao.get().getProjectChannels(projectId);
    }
}
