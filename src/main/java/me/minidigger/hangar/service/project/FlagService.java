package me.minidigger.hangar.service.project;

import me.minidigger.hangar.db.dao.FlagDao;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.model.ProjectFlagsTable;
import me.minidigger.hangar.model.FlagReason;
import me.minidigger.hangar.model.viewhelpers.ProjectFlag;
import me.minidigger.hangar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlagService {

    private final UserService userService;
    private final HangarDao<FlagDao> flagDao;

    @Autowired
    public FlagService(UserService userService, HangarDao<FlagDao> flagDao) {
        this.userService = userService;
        this.flagDao = flagDao;
    }

    public boolean hasUnresolvedFlag(long projectId) {
        return flagDao.get().getUnresolvedFlag(projectId, userService.getCurrentUser().getId()) != null;
    }

    public void flagProject(long projectId, FlagReason flagReason, String comment) {
        ProjectFlagsTable flag = new ProjectFlagsTable(
                projectId,
                userService.getCurrentUser().getId(),
                flagReason,
                comment
        );
        flagDao.get().insert(flag);
    }

    public ProjectFlagsTable markAsResolved(long flagId, boolean resolved) {
        Long resolvedBy = resolved ? userService.getCurrentUser().getId() : null;
        OffsetDateTime resolvedAt = resolved ? OffsetDateTime.now() : null;
        return flagDao.get().markAsResolved(flagId, resolved, resolvedBy, resolvedAt);
    }

    public List<ProjectFlag> getProjectFlags(long projectId) {
        return flagDao.get().getProjectFlags(projectId).entrySet().stream().map(entry -> entry.getKey().with(entry.getValue())).collect(Collectors.toList());
    }

    public ProjectFlag getProjectFlag(long flagId) {
        List<ProjectFlag> flags = flagDao.get().getById(flagId).entrySet().stream().map(entry -> entry.getKey().with(entry.getValue())).collect(Collectors.toList());
        if (flags.size() != 1) return null;
        return flags.get(0);
    }

    public List<ProjectFlag> getAllProjectFlags() {
        return flagDao.get().getFlags().entrySet().stream().map(entry -> entry.getKey().with(entry.getValue())).collect(Collectors.toList());
    }
}
