package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectChannelsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.HangarChannel;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ChannelService extends HangarComponent {

    private final ProjectChannelsDAO projectChannelsDAO;
    private final HangarProjectsDAO hangarProjectsDAO;

    public ChannelService(HangarDao<ProjectChannelsDAO> projectChannelsDAO, HangarDao<HangarProjectsDAO> hangarProjectsDAO) {
        this.projectChannelsDAO = projectChannelsDAO.get();
        this.hangarProjectsDAO = hangarProjectsDAO.get();
    }

    public void checkName(long projectId, String name, @Nullable String existingName) {
        checkName(projectId, name, existingName, projectChannelsDAO::getProjectChannels);
    }

    public void checkColor(long projectId, Color color, @Nullable Color existingColor) {
        checkColor(projectId, color, existingColor, projectChannelsDAO::getProjectChannels);
    }

    private void checkName(long projectId, String name, @Nullable String existingName, Function<Long, List<ProjectChannelTable>> channelTableFunction) {
        if (channelTableFunction.apply(projectId).stream().filter(ch -> !ch.getName().equals(existingName)).anyMatch(ch -> ch.getName().equalsIgnoreCase(name))) {
            throw new HangarApiException(HttpStatus.CONFLICT, "channel.modal.error.duplicateName");
        }
    }

    private void checkColor(long projectId, Color color, @Nullable Color existingColor, Function<Long, List<ProjectChannelTable>> channelTableFunction) {
        if (channelTableFunction.apply(projectId).stream().filter(ch -> ch.getColor() != existingColor).anyMatch(ch -> ch.getColor() == color)) {
            throw new HangarApiException(HttpStatus.CONFLICT, "channel.modal.error.duplicateColor");
        }
    }

    private void validateChannel(String name, Color color, long projectId, boolean nonReviewed, List<ProjectChannelTable> existingChannels) {
        if (!config.channels.isValidChannelName(name)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.invalidName");
        }

        if (existingChannels.size() >= config.projects.getMaxChannels()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.maxChannels", config.projects.getMaxChannels());
        }

        checkName(projectId, name, null, (ignored) -> existingChannels);
        checkColor(projectId, color, null, (ignored) -> existingChannels);
    }

    @Transactional
    public ProjectChannelTable createProjectChannel(String name, Color color, long projectId, boolean nonReviewed) {
        validateChannel(name, color, projectId, nonReviewed, projectChannelsDAO.getProjectChannels(projectId));
        ProjectChannelTable channelTable = projectChannelsDAO.insert(new ProjectChannelTable(name, color, projectId, nonReviewed));
        actionLogger.project(LogAction.PROJECT_CHANNEL_CREATED.create(ProjectContext.of(projectId), formatChannelChange(channelTable), ""));
        return channelTable;
    }

    @Transactional
    public void editProjectChannel(long channelId, String name, Color color, long projectId, boolean nonReviewed) {
        ProjectChannelTable projectChannelTable = getProjectChannel(channelId);
        if (projectChannelTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        validateChannel(name, color, projectId, nonReviewed, projectChannelsDAO.getProjectChannels(projectId).stream().filter(ch -> ch.getId() != channelId).collect(Collectors.toList()));
        String old = formatChannelChange(projectChannelTable);
        projectChannelTable.setName(name);
        projectChannelTable.setColor(color);
        projectChannelTable.setNonReviewed(nonReviewed);
        projectChannelsDAO.update(projectChannelTable);
        actionLogger.project(LogAction.PROJECT_CHANNEL_EDITED.create(ProjectContext.of(projectId), formatChannelChange(projectChannelTable), old));
    }

    @Transactional
    public void deleteProjectChannel(long projectId, long channelId) {
        HangarChannel hangarChannel = hangarProjectsDAO.getHangarChannel(channelId);
        if (hangarChannel == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (hangarChannel.getVersionCount() != 0 || getProjectChannels(projectId).size() == 1) {
            // Cannot delete channels with versions or if its the last channel
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.cannotDelete");
        }
        projectChannelsDAO.delete(hangarChannel);
        actionLogger.project(LogAction.PROJECT_CHANNEL_DELETED.create(ProjectContext.of(projectId), "<i>Deleted</i>", formatChannelChange(hangarChannel)));
    }

    private String formatChannelChange(ProjectChannelTable channelTable) {
        return "Name: " + channelTable.getName() + " Color: " + channelTable.getColor().getHex() + " NonReviewed: " + channelTable.isNonReviewed();
    }

    public List<HangarChannel> getProjectChannels(long projectId) {
        return hangarProjectsDAO.getHangarChannels(projectId);
    }

    public ProjectChannelTable getProjectChannel(long projectId, String name, Color color) {
        return projectChannelsDAO.getProjectChannel(projectId, name, color);
    }

    public ProjectChannelTable getProjectChannel(long channelId) {
        return projectChannelsDAO.getProjectChannel(channelId);
    }

    public ProjectChannelTable getProjectChannelForVersion(long versionId) {
        return projectChannelsDAO.getProjectChannelForVersion(versionId);
    }

    public ProjectChannelTable getFirstChannel(long projectId) {
        return projectChannelsDAO.getFirstChannel(projectId);
    }
}
