package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(HangarNotification.class)
public interface HangarNotificationsDAO {

    @SqlQuery("SELECT n.created_at, n.id, n.type, n.action, n.message_args message, n.read, u.name AS origin_user_name" +
        "   FROM notifications n" +
        "   LEFT OUTER JOIN users u ON u.id = n.origin_id" +
        "   WHERE n.user_id = :userId" +
        "   ORDER BY n.created_at DESC" +
        "   LIMIT :amount")
    List<HangarNotification> getNotifications(long userId, int amount);

    @SqlQuery("SELECT n.created_at, n.id, n.type, n.action, n.message_args message, n.read, u.name AS origin_user_name" +
        "   FROM notifications n" +
        "   LEFT OUTER JOIN users u ON u.id = n.origin_id" +
        "   WHERE n.user_id = :userId" +
        "   ORDER BY n.created_at DESC" +
        "   <offsetLimit>")
    List<HangarNotification> getNotifications(long userId, @BindPagination RequestPagination pagination);

    @SqlQuery("SELECT n.created_at, n.id, n.type, n.action, n.message_args message, n.read, u.name AS origin_user_name" +
        "   FROM notifications n" +
        "   LEFT OUTER JOIN users u ON u.id = n.origin_id" +
        "   WHERE n.user_id = :userId AND n.read = :read" +
        "   ORDER BY n.created_at DESC" +
        "   <offsetLimit>")
    List<HangarNotification> getNotifications(long userId, boolean read, @BindPagination RequestPagination pagination);

    @RegisterConstructorMapper(HangarInvite.HangarProjectInvite.class)
    @SqlQuery("SELECT upr.id roleId," +
        "   upr.role_type AS role," +
        "   p.name," +
        "   o.name as representingOrg," +
        "   '/' || p.owner_name || '/' || p.slug url" +
        "   FROM user_project_roles upr" +
        "   JOIN projects p ON p.id = upr.project_id" +
        "   LEFT JOIN organizations o ON o.user_id = upr.user_id" +
        "   WHERE (upr.user_id = :userId OR o.owner_id = :userId)" +
        "       AND upr.accepted = FALSE" +
        "   ORDER BY upr.created_at DESC")
    List<HangarInvite.HangarProjectInvite> getProjectInvites(long userId);

    @RegisterConstructorMapper(HangarInvite.HangarOrganizationInvite.class)
    @SqlQuery("SELECT uor.id AS roleId," +
        "   uor.role_type AS role," +
        "   o.name," +
        "   '/' || o.name url" +
        "   FROM user_organization_roles uor" +
        "   JOIN organizations o ON o.id = uor.organization_id" +
        "   WHERE uor.user_id = :userId" +
        "       AND uor.accepted = FALSE" +
        "   ORDER BY uor.created_at DESC")
    List<HangarInvite.HangarOrganizationInvite> getOrganizationInvites(long userId);
}
