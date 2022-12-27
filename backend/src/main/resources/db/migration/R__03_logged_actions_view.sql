CREATE OR REPLACE VIEW v_logged_actions
        (id, created_at, user_id, user_name, address, action, context_type, new_state, old_state, p_id, p_slug, p_owner_name, pv_id, pv_version_string,
         pv_platforms, pp_id, pp_name, pp_slug, s_id, s_name)
AS
    SELECT a.id,
           a.created_at,
           a.user_id,
           u.name                       AS user_name,
           a.address,
           a.action,
           0                            AS context_type,
           a.new_state,
           a.old_state,
           p.id                         AS p_id,
           p.slug                       AS p_slug,
           ou.name                      AS p_owner_name,
           NULL::bigint                 AS pv_id,
           NULL::character varying(255) AS pv_version_string,
           NULL::bigint[]               AS pv_platforms,
           NULL::bigint                 AS pp_id,
           NULL::character varying(255) AS pp_name,
           NULL::character varying(255) AS pp_slug,
           NULL::bigint                 AS s_id,
           NULL::character varying(255) AS s_name
    FROM logged_actions_project a
        LEFT JOIN users u ON a.user_id = u.id
        LEFT JOIN projects p ON a.project_id = p.id
        LEFT JOIN users ou ON p.owner_id = ou.id
    UNION ALL
    SELECT a.id,
           a.created_at,
           a.user_id,
           u.name                  AS user_name,
           a.address,
           a.action,
           1                       AS context_type,
           a.new_state,
           a.old_state,
           p.id                    AS p_id,
           p.slug                  AS p_slug,
           ou.name                 AS p_owner_name,
           pv.id                   AS pv_id,
           pv.version_string       AS pv_version_string,
           array(SELECT DISTINCT plv.platform
                 FROM project_version_platform_dependencies pvpd
                     JOIN platform_versions plv ON pvpd.platform_version_id = plv.id
                 WHERE pv.id = pvpd.version_id
                 ORDER BY plv.platform
               )                   AS pv_platforms,
           NULL::bigint            AS pp_id,
           NULL::character varying AS pp_name,
           NULL::character varying AS pp_slug,
           NULL::bigint            AS s_id,
           NULL::character varying AS s_name
    FROM logged_actions_version a
        LEFT JOIN users u ON a.user_id = u.id
        LEFT JOIN project_versions pv ON a.version_id = pv.id
        LEFT JOIN projects p ON a.project_id = p.id
        LEFT JOIN users ou ON p.owner_id = ou.id
    UNION ALL
    SELECT a.id,
           a.created_at,
           a.user_id,
           u.name                  AS user_name,
           a.address,
           a.action,
           2                       AS context_type,
           a.new_state,
           a.old_state,
           p.id                    AS p_id,
           p.slug                  AS p_slug,
           ou.name                 AS p_owner_name,
           NULL::bigint            AS pv_id,
           NULL::character varying AS pv_version_string,
           NULL::bigint[]          AS pv_platforms,
           pp.id                   AS pp_id,
           pp.name                 AS pp_name,
           pp.slug                 AS pp_slug,
           NULL::bigint            AS s_id,
           NULL::character varying AS s_name
    FROM logged_actions_page a
        LEFT JOIN users u ON a.user_id = u.id
        LEFT JOIN project_pages pp ON a.page_id = pp.id
        LEFT JOIN projects p ON a.project_id = p.id
        LEFT JOIN users ou ON p.owner_id = ou.id
    UNION ALL
    SELECT a.id,
           a.created_at,
           a.user_id,
           u.name                  AS user_name,
           a.address,
           a.action,
           3                       AS context_type,
           a.new_state,
           a.old_state,
           NULL::bigint            AS p_id,
           NULL::character varying AS p_slug,
           NULL::character varying AS p_owner_name,
           NULL::bigint            AS pv_id,
           NULL::character varying AS pv_version_string,
           NULL::bigint[]          AS pv_platforms,
           NULL::bigint            AS pp_id,
           NULL::character varying AS pp_name,
           NULL::character varying AS pp_slug,
           s.id                    AS s_id,
           s.name                  AS s_name
    FROM logged_actions_user a
        LEFT JOIN users u ON a.user_id = u.id
        LEFT JOIN users s ON a.subject_id = s.id
    UNION ALL
    SELECT a.id,
           a.created_at,
           a.user_id,
           u.name                  AS user_name,
           a.address,
           a.action,
           4                       AS context_type,
           a.new_state,
           a.old_state,
           NULL::bigint            AS p_id,
           NULL::character varying AS p_slug,
           NULL::character varying AS p_owner_name,
           NULL::bigint            AS pv_id,
           NULL::character varying AS pv_version_string,
           NULL::bigint[]          AS pv_platforms,
           NULL::bigint            AS pp_id,
           NULL::character varying AS pp_name,
           NULL::character varying AS pp_slug,
           s.id                    AS s_id,
           s.name                  AS s_name
    FROM logged_actions_organization a
        LEFT JOIN organizations o ON a.organization_id = o.id
        LEFT JOIN users u ON a.user_id = u.id
        LEFT JOIN users s ON o.user_id = s.id;
