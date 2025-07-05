package io.papermc.hangar.components.globaldata.dao;

import java.util.List;
import java.util.Map;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindBeanList;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(GlobalNotificationTable.class)
@RegisterConstructorMapper(AnnouncementTable.class)
public interface GlobalDataDAO extends SqlObject {

    @Timestamped
    @SqlUpdate("INSERT INTO announcements (created_at, text, color, created_by) VALUES (:now, :text, :color, :createdBy)")
    void insertAnnouncement(@BindBean AnnouncementTable announcementTable);

    @SqlQuery("SELECT * FROM announcements")
    List<AnnouncementTable> getAnnouncements();

    @SqlUpdate("INSERT INTO announcements (id, created_at, text, color, created_by) VALUES <announcements> " +
        "ON CONFLICT (id) DO UPDATE SET text = EXCLUDED.text, color = EXCLUDED.color, created_by = EXCLUDED.created_by")
    void updateAnnouncements(@BindBeanList(propertyNames = {"id", "createdAt", "text", "color", "createdBy"}) List<AnnouncementTable> announcements);

    @SqlQuery("SELECT * FROM global_notifications")
    List<GlobalNotificationTable> getGlobalNotifications();

    @KeyColumn("key")
    @ValueColumn("content")
    @SqlQuery("SELECT key, content FROM global_notifications WHERE now() > active_from AND now() < active_to ")
    Map<String, String> getActiveGlobalNotifications();

    @SqlUpdate("INSERT INTO global_notifications (id, created_at, key, content, color, active_from, active_to, created_by) VALUES <globalNotifications> " +
        "ON CONFLICT (id) DO UPDATE SET key = EXCLUDED.key, content = EXCLUDED.content, active_from = EXCLUDED.active_from, active_to = EXCLUDED.active_to")
    void updateGlobalNotifications(@BindBeanList(propertyNames = {"id", "createdAt", "key", "content", "color", "activeFrom", "activeTo", "createdBy", }) List<GlobalNotificationTable> globalNotifications);
}
