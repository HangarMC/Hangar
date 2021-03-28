package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectFlagsDAO;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import io.papermc.hangar.model.internal.projects.HangarProjectFlag;
import io.papermc.hangar.service.HangarService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class FlagService extends HangarService {

    private final HangarProjectFlagsDAO hangarProjectFlagsDAO;

    public FlagService(HangarDao<HangarProjectFlagsDAO> hangarProjectFlagsDAO) {
        this.hangarProjectFlagsDAO = hangarProjectFlagsDAO.get();
    }

    public void createFlag(long projectId, FlagReason reason, String comment) {
        // TODO idk, we prolly need more checking here, plus notification? logs?
        hangarProjectFlagsDAO.insert(new ProjectFlagTable( projectId, getHangarPrincipal().getId(), reason, comment));
    }

    public ProjectFlagTable markAsResolved(long flagId, boolean resolved) {
        Long resolvedBy = resolved ? getHangarPrincipal().getId() : null;
        OffsetDateTime resolvedAt = resolved ? OffsetDateTime.now() : null;
        return hangarProjectFlagsDAO.markAsResolved(flagId, resolved, resolvedBy, resolvedAt);
    }

    public List<HangarProjectFlag> getFlags(String author, String slug) {
        return hangarProjectFlagsDAO.getFlags(author, slug);
    }

    public List<HangarProjectFlag> getFlags() {
        return hangarProjectFlagsDAO.getFlags();
    }
}
