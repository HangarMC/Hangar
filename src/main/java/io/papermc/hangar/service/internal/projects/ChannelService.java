package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectChannelsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.internal.projects.HangarChannel;
import io.papermc.hangar.service.HangarService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService extends HangarService {

    private final ProjectChannelsDAO projectChannelsDAO;
    private final HangarProjectsDAO hangarProjectsDAO;

    public ChannelService(HangarDao<ProjectChannelsDAO> projectChannelsDAO, HangarDao<HangarProjectsDAO> hangarProjectsDAO) {
        this.projectChannelsDAO = projectChannelsDAO.get();
        this.hangarProjectsDAO = hangarProjectsDAO.get();
    }

    public ProjectChannelTable createProjectChannel(String name, Color color, long projectId, boolean nonReviewed) {
        if (!config.channels.isValidChannelName(name)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.invalidName");
        }

        List<ProjectChannelTable> existingTables = projectChannelsDAO.getProjectChannels(projectId);
        if (existingTables.size() >= config.projects.getMaxChannels()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.maxChannels", config.projects.getMaxChannels());
        }

        if (existingTables.stream().anyMatch(ch -> ch.getColor() == color)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.duplicateColor");
        }

        if (existingTables.stream().anyMatch(ch -> ch.getName().equalsIgnoreCase(name))) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.duplicateName");
        }

        return projectChannelsDAO.insert(new ProjectChannelTable(name, color, projectId, nonReviewed));
    }

    public List<HangarChannel> getProjectChannels(long projectId) {
        return hangarProjectsDAO.getHangarChannels(projectId);
    }

    public ProjectChannelTable getProjectChannel(long projectId, String name, Color color) {
        return projectChannelsDAO.getProjectChannel(projectId, name, color);
    }

    public ProjectChannelTable getProjectChannelForVersion(long versionId) {
        return projectChannelsDAO.getProjectChannelForVersion(versionId);
    }

    public ProjectChannelTable getFirstChannel(long projectId) {
        return projectChannelsDAO.getFirstChannel(projectId);
    }
}
