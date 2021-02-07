package io.papermc.hangar.db.dao.session;

import io.papermc.hangar.controller.extras.HangarApiRequest;
import io.papermc.hangar.db.mappers.PermissionMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(HangarApiRequest.class)
@RegisterColumnMapper(PermissionMapper.class)
@Deprecated
public interface HangarRequestDAO {

    @SqlQuery("SELECT u.id u_id," +
            "       u.created_at u_created_at," +
            "       u.full_name u_full_name," +
            "       u.name u_name," +
            "       u.email u_email," +
            "       u.tagline u_tagline," +
            "       u.join_date u_join_date," +
            "       u.read_prompts u_read_prompts," +
            "       u.is_locked u_is_locked," +
            "       u.language u_language," +
            "       ak.id ak_id," +
            "       ak.created_at ak_created_at," +
            "       ak.name ak_name," +
            "       ak.owner_id ak_owner_id," +
            "       ak.token_identifier ak_token_identifier," +
            "       ak.token ak_token," +
            "       ak.raw_key_permissions::BIGINT ak_permissions," +
            "       aks.token \"session\"," +
            "       aks.expires," +
            "       CASE" +
            "           WHEN u.id IS NULL THEN 1::BIT(64)::BIGINT" +
            "           ELSE ((coalesce(gt.permission, B'0'::BIT(64)) | 1::BIT(64) | (1::BIT(64) << 1) | (1::BIT(64) << 2)) &" +
            "                coalesce(ak.raw_key_permissions, (-1)::BIT(64)))::BIGINT" +
            "           END AS global_permissions" +
            "    FROM api_sessions aks" +
            "             LEFT JOIN api_keys ak ON aks.key_id = ak.id" +
            "             LEFT JOIN users u ON aks.user_id = u.id" +
            "             LEFT JOIN global_trust gt ON gt.user_id = u.id" +
            "  WHERE aks.token = :token")
    HangarApiRequest createHangarRequest(String token);
}
