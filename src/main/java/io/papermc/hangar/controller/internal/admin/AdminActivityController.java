package io.papermc.hangar.controller.internal.admin;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.admin.activity.FlagActivity;
import io.papermc.hangar.model.internal.admin.activity.ReviewActivity;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.service.internal.admin.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@ResponseBody
@PermissionRequired(NamedPermission.REVIEWER)
@RequestMapping(path = "/api/internal/admin/activity/{user}", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminActivityController extends HangarComponent {

    private final ActivityService activityService;

    @Autowired
    public AdminActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/flags")
    public List<FlagActivity> getFlagActivity(@PathVariable UserTable user) {
        if (user.isOrganization()) {
            throw new HangarApiException("userActivity.error.isOrg");
        }
        return activityService.getFlagActivity(user);
    }

    @GetMapping("/reviews")
    public List<ReviewActivity> getReviewActivity(@PathVariable UserTable user) {
        if (user.isOrganization()) {
            throw new HangarApiException("userActivity.error.isOrg");
        }
        return activityService.getReviewActivity(user);
    }
}
