INSERT INTO users (id, uuid, created_at, full_name, name, email, tagline, join_date, read_prompts, locked, language, theme)
VALUES (1,
        '8fb45b4e-6b1f-4e75-a096-98d73b755cd3',
        '2022-07-26 07:35:29.136992 +00:00',
        'test',
        'test',
        'test@papermc.io',
        NULL,
        '2022-07-26 07:35:29.136992 +00:00',
        '{}',
        FALSE,
        'en',
        'white');

INSERT INTO roles (id, created_at, name, category, title, color, assignable, rank, permission)
VALUES (1,
        '2022-06-21 20:04:40.308713 +00:00',
        'Hangar_Admin',
        'global',
        'Hangar Admin',
        '#DC0000',
        FALSE,
        NULL,
        '0000111111111111111111111111111111111111111111111111111111111111');

INSERT INTO user_global_roles (user_id, role_id)
VALUES (1, 1);

INSERT INTO users (id, uuid, created_at, full_name, name, email, tagline, join_date, read_prompts, locked, language, theme)
VALUES (2,
        '00000000-38c3-7225-ffff-ffa873e811b0',
        '2023-03-21 17:44:15.680073 +00:00',
        NULL,
        'JarScanner',
        'automated@test.test',
        NULL,
        '2023-03-21 17:44:15.680073 +00:00',
        '{}',
        FALSE,
        'en',
        'white');
