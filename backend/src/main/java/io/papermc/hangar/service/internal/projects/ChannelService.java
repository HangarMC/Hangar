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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelService extends HangarComponent {

    private final ProjectChannelsDAO projectChannelsDAO;
    private final HangarProjectsDAO hangarProjectsDAO;

    public ChannelService(final ProjectChannelsDAO projectChannelsDAO, final HangarProjectsDAO hangarProjectsDAO) {
        this.projectChannelsDAO = projectChannelsDAO;
        this.hangarProjectsDAO = hangarProjectsDAO;
    }

    public void checkName(final long projectId, final String name, final @Nullable String existingName) {
        this.checkName(projectId, name, existingName, this.projectChannelsDAO::getProjectChannels);
    }

    public void checkColor(final long projectId, final Color color, final @Nullable Color existingColor) {
        this.checkColor(projectId, color, existingColor, this.projectChannelsDAO::getProjectChannels);
    }

    private void checkName(final long projectId, final String name, final @Nullable String existingName, final LongFunction<List<ProjectChannelTable>> channelTableFunction) {
        if (channelTableFunction.apply(projectId).stream().filter(ch -> !ch.getName().equals(existingName)).anyMatch(ch -> ch.getName().equalsIgnoreCase(name))) {
            throw new HangarApiException(HttpStatus.CONFLICT, "channel.modal.error.duplicateName");
        }
    }

    private void checkColor(final long projectId, final Color color, final @Nullable Color existingColor, final LongFunction<List<ProjectChannelTable>> channelTableFunction) {
        if (channelTableFunction.apply(projectId).stream().filter(ch -> ch.getColor() != existingColor).anyMatch(ch -> ch.getColor() == color)) {
            throw new HangarApiException(HttpStatus.CONFLICT, "channel.modal.error.duplicateColor");
        }
    }

    private void validateChannel(final String name, final Color color, final long projectId, final List<ProjectChannelTable> existingChannels) {
        if (!this.config.channels.isValidChannelName(name)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.invalidName");
        }

        if (existingChannels.size() >= this.config.projects.maxChannels()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.maxChannels", this.config.projects.maxChannels());
        }

        this.checkName(projectId, name, null, ignored -> existingChannels);
        this.checkColor(projectId, color, null, ignored -> existingChannels);
    }

    @Transactional
    public ProjectChannelTable createProjectChannel(final String name, final String description, final Color color, final long projectId, final Set<ChannelFlag> flags) {
        this.validateChannel(name, color, projectId, this.projectChannelsDAO.getProjectChannels(projectId));
        final ProjectChannelTable channelTable = this.projectChannelsDAO.insert(new ProjectChannelTable(name, description, color, projectId, flags));
        this.actionLogger.project(LogAction.PROJECT_CHANNEL_CREATED.create(ProjectContext.of(projectId), formatChannelChange(channelTable), ""));
        return channelTable;
    }

    @Transactional
    public void editProjectChannel(final long channelId, final String name, final String description, final Color color, final long projectId, final Set<ChannelFlag> flags) {
        final ProjectChannelTable projectChannelTable = this.getProjectChannel(channelId);
        if (projectChannelTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        if (projectChannelTable.getFlags().contains(ChannelFlag.FROZEN)) {
            boolean updated = false;
            final Iterator<ChannelFlag> currentIter = projectChannelTable.getFlags().iterator();
            while (currentIter.hasNext()) {
                final ChannelFlag existingFlag = currentIter.next();
                if (existingFlag == ChannelFlag.FROZEN) continue;
                if (!flags.contains(existingFlag)) {
                    if (!existingFlag.isAlwaysEditable()) {
                        throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.cannotEdit");
                    }
                    updated = true;
                    currentIter.remove();
                } else {
                    flags.remove(existingFlag);
                }
            }
            for (final ChannelFlag newFlag : flags) { // should be all "new" flags here
                if (!newFlag.isAlwaysEditable()) {
                    throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.cannotEdit");
                }
                projectChannelTable.getFlags().add(newFlag);
                updated = true;
            }

            if (updated) {
                final String old = formatChannelChange(projectChannelTable);
                this.projectChannelsDAO.update(projectChannelTable);
                this.actionLogger.project(LogAction.PROJECT_CHANNEL_EDITED.create(ProjectContext.of(projectId), formatChannelChange(projectChannelTable), old));
            }
            return;
        }

        this.validateChannel(name, color, projectId, this.projectChannelsDAO.getProjectChannels(projectId).stream().filter(ch -> ch.getId() != channelId).collect(Collectors.toList()));
        final String old = formatChannelChange(projectChannelTable);
        projectChannelTable.setName(name);
        projectChannelTable.setDescription(description);
        projectChannelTable.setColor(color);
        projectChannelTable.setFlags(flags);
        this.projectChannelsDAO.update(projectChannelTable);
        this.actionLogger.project(LogAction.PROJECT_CHANNEL_EDITED.create(ProjectContext.of(projectId), formatChannelChange(projectChannelTable), old));
    }

    @Transactional
    public void deleteProjectChannel(final long projectId, final long channelId) {
        final HangarChannel hangarChannel = this.hangarProjectsDAO.getHangarChannel(channelId);
        if (hangarChannel == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (hangarChannel.getFlags().contains(ChannelFlag.FROZEN)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.cannotDelete");
        }
        if (hangarChannel.getVersionCount() != 0 || this.getProjectChannels(projectId).size() == 1) {
            // Cannot delete channels with versions or if it's the last channel
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "channel.modal.error.cannotDelete");
        }
        this.projectChannelsDAO.delete(hangarChannel);
        this.actionLogger.project(LogAction.PROJECT_CHANNEL_DELETED.create(ProjectContext.of(projectId), "<i>Deleted</i>", formatChannelChange(hangarChannel)));
    }

    public List<HangarChannel> getProjectChannels(final long projectId) {
        return this.hangarProjectsDAO.getHangarChannels(projectId);
    }

    public ProjectChannelTable getProjectChannel(final long projectId, final String name, final Color color) {
        return this.projectChannelsDAO.getProjectChannel(projectId, name, color);
    }

    public ProjectChannelTable getProjectChannel(final long projectId, final String name) {
        return this.projectChannelsDAO.getProjectChannel(projectId, name);
    }

    public ProjectChannelTable getProjectChannel(final long channelId) {
        return this.projectChannelsDAO.getProjectChannel(channelId);
    }

    public ProjectChannelTable getProjectChannelForVersion(final long versionId) {
        return this.projectChannelsDAO.getProjectChannelForVersion(versionId);
    }

    public ProjectChannelTable getFirstChannel(final long projectId) {
        return this.projectChannelsDAO.getFirstChannel(projectId);
    }

    private static String formatChannelChange(final ProjectChannelTable channelTable) {
        return "Name: " + channelTable.getName() + " Color: " + channelTable.getColor().getHex() + " Flags: " + channelTable.getFlags();
    }
}
