package io.papermc.hangar.controller.internal;

import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.logs.HangarLoggedAction;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.service.internal.UserActionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal/actionlog", produces = MediaType.APPLICATION_JSON_VALUE)
public class ActionLogController {

    private final UserActionLogService userActionLogService;

    @Autowired
    public ActionLogController(UserActionLogService userActionLogService) {
        this.userActionLogService = userActionLogService;
    }

    // TODO use request pagination
    @ResponseBody
    @GetMapping("/")
    @PermissionRequired(perms = NamedPermission.REVIEWER)
    public PaginatedResult<HangarLoggedAction> actionlog() {
        List<HangarLoggedAction> log = userActionLogService.getLog(0, null, null, null, null, null, null);
        return new PaginatedResult<>(new Pagination(0, 0, (long) log.size()), log);
    }
}
