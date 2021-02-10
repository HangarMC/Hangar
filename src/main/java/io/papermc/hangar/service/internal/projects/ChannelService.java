package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectChannelsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.service.HangarService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ChannelService extends HangarService {

    private final ProjectChannelsDAO projectChannelsDAO;

    public ChannelService(HangarDao<ProjectChannelsDAO> projectChannelsDAO) {
        this.projectChannelsDAO = projectChannelsDAO.get();
    }

    public ProjectChannelTable createProjectChannel(String name, Color color, long projectId, boolean nonReviewed) {
        if (!hangarConfig.channels.isValidChannelName(name)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "error.channel.invalidName");
        }

        return projectChannelsDAO.insert(new ProjectChannelTable(name, color, projectId, nonReviewed));
    }

    public ProjectChannelTable getFirstChannel(long projectId) {
        return projectChannelsDAO.getFirstChannel(projectId);
    }
}
