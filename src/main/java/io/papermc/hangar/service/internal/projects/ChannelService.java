package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectChannelsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.ChannelFlag;
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
import java.util.Set;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

@Service
public class ChannelService extends HangarComponent {

    private final ProjectChannelsDAO projectChannelsDAO;
    private final HangarProjectsDAO hangarProjectsDAO;

    public ChannelService(ProjectChannelsDAO projectChannelsDAO, HangarProjectsDAO hangarProjectsDAO) {
        this.projectChannelsDAO = projectChannelsDAO;
        this.hangarProjectsDAO = hangarProjectsDAO;
    }

    public void checkName(long projectId, String name, @Nullable String existingName) {
        checkName(projectId, name, existingName, projectChannelsDAO::getProjectChannels);
    }

    public void checkColor(long projectId, Color color, @Nullable Color existingColor) {
        checkColor(projectId, color, existingColor, projectChannelsDAO::getProjectChannels);
    }

    private void checkName(long projectId, String name, @Nullable String existingName, LongFunction<List<ProjectChannelTable>> channelTableFunction) {
        if (channelTableFunction.apply(projectId).stream().filter(ch -> !ch.getName().equals(existingName)).anyMatch(ch -> ch.getName().equalsIgnoreCase(name))) {
            throw new HangarApiException(HttpStatus.CONFLICT, "channel.modal.error.duplicateName");
        }
    }

    private void checkColor(long projectId, Color color, @Nullable Color existingColor, LongFunction<List<ProjectChannelTable>> channelTableFunction) {
        if (channelTableFunction.apply(projectId).stream().filter(ch -> ch.getColor() != existingColor).anyMatch(ch -> ch.getColor() == color)) {
            throw new HangarApiException(HttpStatus.CONFLICT, "channel.modal.error.duplicateColor");
        }
    }

    private void validateChannel(final String name, final Color color, final long projectId, final List<ProjectChannelTable> existingChannels) {
        if (!this.config.channels.isValidChannelName(name)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.invalidName");
        }

        if (existingChannels.size() >= this.config.projects.getMaxChannels()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.maxChannels", this.config.projects.getMaxChannels());
        }

        this.checkName(projectId, name, null, ignored -> existingChannels);
        this.checkColor(projectId, color, null, ignored -> existingChannels);
    }

    @Transactional
    public ProjectChannelTable createProjectChannel(final String name, final Color color, final long projectId, final Set<ChannelFlag> flags) {
        this.validateChannel(name, color, projectId, this.projectChannelsDAO.getProjectChannels(projectId));
        ProjectChannelTable channelTable = this.projectChannelsDAO.insert(new ProjectChannelTable(name, color, projectId, flags));
        this.actionLogger.project(LogAction.PROJECT_CHANNEL_CREATED.create(ProjectContext.of(projectId), formatChannelChange(channelTable), ""));
        return channelTable;
    }

    @Transactional
    public void editProjectChannel(final long channelId, final String name, final Color color, final long projectId, final Set<ChannelFlag> flags) {
        ProjectChannelTable projectChannelTable = this.getProjectChannel(channelId);
        if (projectChannelTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (projectChannelTable.getFlags().contains(ChannelFlag.FROZEN)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.cannotEdit");
        }
        this.validateChannel(name, color, projectId, this.projectChannelsDAO.getProjectChannels(projectId).stream().filter(ch -> ch.getId() != channelId).collect(Collectors.toList()));
        String old = formatChannelChange(projectChannelTable);
        projectChannelTable.setName(name);
        projectChannelTable.setColor(color);
        projectChannelTable.setFlags(flags);
        this.projectChannelsDAO.update(projectChannelTable);
        this.actionLogger.project(LogAction.PROJECT_CHANNEL_EDITED.create(ProjectContext.of(projectId), formatChannelChange(projectChannelTable), old));
    }

    @Transactional
    public void deleteProjectChannel(final long projectId, final long channelId) {
        HangarChannel hangarChannel = this.hangarProjectsDAO.getHangarChannel(channelId);
        if (hangarChannel == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (hangarChannel.getFlags().contains(ChannelFlag.FROZEN)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.cannotDelete");
        }
        if (hangarChannel.getVersionCount() != 0 || this.getProjectChannels(projectId).size() == 1) {
            // Cannot delete channels with versions or if its the last channel
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.cannotDelete");
        }
        this.projectChannelsDAO.delete(hangarChannel);
        this.actionLogger.project(LogAction.PROJECT_CHANNEL_DELETED.create(ProjectContext.of(projectId), "<i>Deleted</i>", formatChannelChange(hangarChannel)));
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

    private static String formatChannelChange(final ProjectChannelTable channelTable) {
        return "Name: " + channelTable.getName() + " Color: " + channelTable.getColor().getHex() + " Flags: " + channelTable.getFlags();
    }
}
