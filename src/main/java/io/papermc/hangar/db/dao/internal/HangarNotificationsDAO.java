package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.internal.user.notifications.HangarInvite;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HangarNotificationsDAO {

    @RegisterConstructorMapper(HangarNotification.class)
    @SqlQuery("SELECT n.id, n.action, n.message_args message, n.read, u.name as origin_user_name" +
            "   FROM notifications n" +
            "   LEFT OUTER JOIN users u ON u.id = n.origin_id" +
            "   WHERE n.user_id = :userId" +
            "   ORDER BY n.created_at")
    List<HangarNotification> getNotifications(long userId);

    @RegisterConstructorMapper(HangarInvite.HangarProjectInvite.class)
    @SqlQuery("SELECT upr.id roleTableId," +
            "   upr.role_type AS role," +
            "   p.name," +
            "   '/' || p.owner_name || '/' || p.slug url" +
            "   FROM user_project_roles upr" +
            "   JOIN projects p ON p.id = upr.project_id" +
            "   WHERE upr.user_id = :userId " +
            "       AND upr.accepted = FALSE" +
            "   ORDER BY upr.created_at")
    List<HangarInvite.HangarProjectInvite> getProjectInvites(long userId);

    @RegisterConstructorMapper(HangarInvite.HangarOrganizationInvite.class)
    @SqlQuery("SELECT uor.id roleTableId," +
            "   uor.role_type AS role," +
            "   o.name," +
            "   '/' || o.name url" +
            "   FROM user_organization_roles uor" +
            "   JOIN organizations o ON o.id = uor.organization_id" +
            "   WHERE uor.user_id = :userId" +
            "       AND uor.accepted = FALSE" +
            "   ORDER BY uor.created_at")
    List<HangarInvite.HangarOrganizationInvite> getOrganizationInvites(long userId);
}
