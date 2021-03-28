package io.papermc.hangar.serviceold.project;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ProjectChannelDao;
import io.papermc.hangar.db.modelold.ProjectChannelsTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("oldChannelService")
@Deprecated(forRemoval = true)
public class ChannelService {

    private final HangarDao<ProjectChannelDao> channelDao;

    @Autowired
    public ChannelService(HangarDao<ProjectChannelDao> channelDao) {
        this.channelDao = channelDao;
    }

    public ProjectChannelsTable getProjectChannel(long projectId, long channelId) {
        return channelDao.get().getProjectChannel(projectId, null, channelId);
    }

    public ProjectChannelsTable getVersionsChannel(long projectId, long versionId) {
        return channelDao.get().getVersionsChannel(projectId, versionId);
    }

}
