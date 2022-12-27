package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.db.mappers.factories.JoinableRowMapperFactory;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.user.JoinableMember;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

@Repository
public interface HangarOrganizationsDAO {

    @RegisterRowMapperFactory(JoinableRowMapperFactory.class)
    @RegisterConstructorMapper(UserTable.class)
    @RegisterConstructorMapper(value = OrganizationRoleTable.class, prefix = "uor_")
    @UseStringTemplateEngine
    @SqlQuery("SELECT u.*," +
        "       uor.id uor_id," +
        "       uor.created_at uor_created_at," +
        "       uor.user_id uor_user_id," +
        "       uor.role_type uor_role_type," +
        "       uor.organization_id uor_organization_id," +
        "       uor.accepted uor_accepted," +
        "       om.hidden hidden" +
        "   FROM user_organization_roles uor" +
        "       JOIN users u ON uor.user_id = u.id" +
        "       LEFT JOIN organization_members om ON om.user_id = u.id AND om.organization_id = uor.organization_id" +
        "   WHERE uor.organization_id = :orgId <if(!canSeePending)>AND (uor.accepted IS TRUE OR uor.user_id = :userId) AND (om.hidden IS FALSE OR uor.user_id = :userId)<endif>")
    List<JoinableMember<OrganizationRoleTable>> getOrganizationMembers(long orgId, Long userId, @Define boolean canSeePending);
}
