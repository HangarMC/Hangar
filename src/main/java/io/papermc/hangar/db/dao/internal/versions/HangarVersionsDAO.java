package io.papermc.hangar.db.dao.internal.versions;

import io.papermc.hangar.model.internal.versions.HangarVersion;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@UseStringTemplateEngine
@UseEnumStrategy(EnumStrategy.BY_ORDINAL)
@RegisterConstructorMapper(HangarVersion.class)
public interface HangarVersionsDAO {

    @SqlQuery("SELECT pv.id," +
            "       pv.created_at," +
            "       pv.version_string," +
            "       pv.visibility," +
            "       pv.description," +
            "       coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_downloads," +
            "       pv.file_name fi_name," +
            "       pv.file_size fi_size_bytes," +
            "       pv.hash fi_md5_hash," +
            "       pv.external_url," +
            "       u.name author," +
            "       pv.review_state," +
            "       exists(SELECT 1 FROM recommended_project_versions rpv WHERE rpv.version_id = pv.id) as recommended," +
            "       ru.name approved_by" +
            "   FROM project_versions pv" +
            "       JOIN projects p ON pv.project_id = p.id" +
            "       LEFT JOIN users u ON pv.author_id = u.id" +
            "       LEFT JOIN users ru ON pv.reviewer_id = ru.id" +
            "   WHERE " +
            "       <if(!canSeeHidden)>" +
            "           (pv.visibility = 0 " +
            "           <if(userId)>" +
            "               OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) " +
            "           <endif>)" +
            "           AND" +
            "       <endif>" +
            "       pv.id = :versionId" +
            "   ORDERED BY pv.created_at DESC"
    )
    HangarVersion getVersion(long versionId, @Define boolean canSeeHidden, @Define Long userId);

    @SqlQuery("SELECT pv.id," +
            "       pv.created_at," +
            "       pv.version_string," +
            "       pv.visibility," +
            "       pv.description," +
            "       coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_downloads," +
            "       pv.file_name fi_name," +
            "       pv.file_size fi_size_bytes," +
            "       pv.hash fi_md5_hash," +
            "       pv.external_url," +
            "       u.name author," +
            "       pv.review_state," +
            "       exists(SELECT 1 FROM recommended_project_versions rpv WHERE rpv.version_id = pv.id) as recommended," +
            "       ru.name approved_by" +
            "   FROM project_versions pv" +
        "           JOIN projects p ON pv.project_id = p.id" +
            "       LEFT JOIN users u ON pv.author_id = u.id" +
            "       LEFT JOIN users ru ON pv.reviewer_id = ru.id" +
            "   WHERE " +
            "       <if(!canSeeHidden)>" +
            "           (pv.visibility = 0 " +
            "           <if(userId)>" +
            "               OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) " +
            "           <endif>)" +
            "           AND" +
            "       <endif>" +
            "       lower(p.owner_name) = lower(:author) AND" +
            "       lower(p.slug) = lower(:slug) AND" +
            "       pv.version_string = :versionString" +
            "   ORDER BY pv.created_at DESC"
    )
    List<HangarVersion> getVersionsWithVersionString(String author, String slug, String versionString, @Define boolean canSeeHidden, @Define Long userId);
}
