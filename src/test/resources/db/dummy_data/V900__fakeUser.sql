INSERT INTO users (id, uuid, created_at, full_name, name, email, tagline, join_date, read_prompts, locked, language, theme)
VALUES (1, '8fb45b4e-6b1f-4e75-a096-98d73b755cd3', '2022-07-26 07:35:29.136992 +00:00', 'test', 'test', 'test@papermc.io', null, '2022-07-26 07:35:29.136992 +00:00', '{}', false, 'en', 'white');

INSERT INTO roles (id, created_at, name, category, title, color, assignable, rank, permission) VALUES (1, '2022-06-21 20:04:40.308713 +00:00', 'Hangar_Admin', 'global', 'Hangar Admin', '#DC0000', false, null, '0000111111111111111111111111111111111111111111111111111111111111');
INSERT INTO user_global_roles (user_id, role_id) VALUES (1, 1);
