package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.ActivityDAO;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.admin.activity.FlagActivity;
import io.papermc.hangar.model.internal.admin.activity.ReviewActivity;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityService extends HangarComponent {

    private final ActivityDAO activityDAO;

    @Autowired
    public ActivityService(final ActivityDAO activityDAO) {
        this.activityDAO = activityDAO;
    }

    public List<FlagActivity> getFlagActivity(final UserTable userTable) {
        return this.activityDAO.getFlagActivity(userTable.getId());
    }

    public List<ReviewActivity> getReviewActivity(final UserTable userTable) {
        return this.activityDAO.getReviewActivity(userTable.getId());
    }
}
