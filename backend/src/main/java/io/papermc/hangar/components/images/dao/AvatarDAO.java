package io.papermc.hangar.components.images.dao;

import io.papermc.hangar.components.images.model.AvatarTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(AvatarTable.class)
public interface AvatarDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO avatars (type, subject, created_at, optimized_hash, unoptimized_hash, version) VALUES (:type, :subject, :now, :optimizedHash, :unoptimizedHash, :version)")
    void createAvatar(@BindBean AvatarTable table);

    @SqlQuery("SELECT * FROM avatars WHERE type = :type AND subject = :subject")
    AvatarTable getAvatar(String type, String subject);

    @Timestamped
    @SqlUpdate("UPDATE avatars SET optimized_hash = :optimizedHash, unoptimized_hash = :unoptimizedHash, created_at = :now, version = :version WHERE type = :type AND subject = :subject")
    void updateAvatar(@BindBean AvatarTable table);

    @SqlUpdate("DELETE FROM avatars WHERE type = :type AND subject = :subject")
    void deleteAvatar(String type, String subject);
}
