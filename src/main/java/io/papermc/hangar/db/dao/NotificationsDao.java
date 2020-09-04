package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.model.NotificationsTable;
import io.papermc.hangar.db.model.UsersTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;

@Repository
@RegisterBeanMapper(NotificationsTable.class)
public interface NotificationsDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO notifications (created_at, user_id, notification_type, action, origin_id, message_args) VALUES (:now, :userId, :notificationType, :action, :originId, :messageArgs)")
    NotificationsTable insert(@BindBean NotificationsTable notificationsTable);

    @SqlUpdate("UPDATE notifications SET read = true WHERE id = :notificationId AND user_id = :userId")
    boolean markAsRead(long notificationId, long userId);

    @RegisterBeanMapper(value = UsersTable.class, prefix = "u")
    @UseStringTemplateEngine
    @SqlQuery("SELECT n.*, " +
            "u.id u_id, u.created_at u_created_at, full_name u_full_name, name u_name, email u_email, tagline u_tagline, join_date u_join_date, read_prompts u_read_prompts, is_locked u_is_locked, language u_language " +
            "FROM notifications n " +
            "LEFT OUTER JOIN users u on n.origin_id = u.id " +
            "WHERE n.user_id = :userId AND <ifread> " +
            "ORDER BY n.created_at")
    LinkedHashMap<NotificationsTable, UsersTable> getUserNotifications(long userId, @Define("ifread") String read);
}
