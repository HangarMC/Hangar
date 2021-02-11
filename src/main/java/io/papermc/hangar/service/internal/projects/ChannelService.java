package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectChannelsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.service.HangarService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService extends HangarService {

    private final ProjectChannelsDAO projectChannelsDAO;

    public ChannelService(HangarDao<ProjectChannelsDAO> projectChannelsDAO) {
        this.projectChannelsDAO = projectChannelsDAO.get();
    }

    public ProjectChannelTable createProjectChannel(String name, Color color, long projectId, boolean nonReviewed) {
        if (!hangarConfig.channels.isValidChannelName(name)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.new.error.invalidName");
        }

        List<ProjectChannelTable> existingTables = projectChannelsDAO.getProjectChannels(projectId);
        if (existingTables.size() >= hangarConfig.projects.getMaxChannels()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.new.error.maxChannels", hangarConfig.projects.getMaxChannels());
        }

        if (existingTables.stream().anyMatch(ch -> ch.getColor() == color)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.new.error.duplicateColor");
        }

        if (existingTables.stream().anyMatch(ch -> ch.getName().equalsIgnoreCase(name))) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.new.error.duplicateName");
        }

        return projectChannelsDAO.insert(new ProjectChannelTable(name, color, projectId, nonReviewed));
    }

    public List<ProjectChannelTable> getProjectChannels(long projectId) {
        return projectChannelsDAO.getProjectChannels(projectId);
    }

    public ProjectChannelTable getProjectChannel(long projectId, String name, Color color) {
        return projectChannelsDAO.getProjectChannel(projectId, name, color);
    }

    public ProjectChannelTable getFirstChannel(long projectId) {
        return projectChannelsDAO.getFirstChannel(projectId);
    }
}
