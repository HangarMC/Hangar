INSERT INTO api_keys (id, created_at, name, owner_id, token_identifier, token, raw_key_permissions)
VALUES (1, '2022-07-30 18:14:43.016556 +00:00', 'all', 1, 'a02f13bb-4329-4c85-984a-a817daacedcd', '92453bc498244ba555e4533894cee3c764a4f0a1daec18ca77cb3712dda1d888', '0000000000000000000011110000111100001111001100001111011111110111');
INSERT INTO api_keys (id, created_at, name, owner_id, token_identifier, token, raw_key_permissions)
VALUES (2, '2022-07-30 18:19:52.775045 +00:00', 'onlyProject', 1, 'b28dbeee-e1b6-44db-aa0d-a641208517ea', '1bbdf12a58684e947d020d4a6856fae0025a037428c8d72429bdef3af1177f5b', '0000000000000000000000000000000000000000000000000000000100000000');
INSERT INTO api_keys (id, created_at, name, owner_id, token_identifier, token, raw_key_permissions)
VALUES (3, '2022-07-30 18:42:24.280005 +00:00', 'seeHidden', 1, 'bcbca881-87ba-4136-880d-4b387cb6cf03', '4c903e22e01448afc8ab50becc9d15152ec5131b6d54b24b443b3453fc85e5cb', '0000000000000000000000000000000000000010000000000000000000000000');

INSERT INTO projects (id, created_at, name, slug, owner_name, owner_id, topic_id, post_id, category, description, visibility, keywords, homepage, issues, source, support, license_type, license_name, license_url, forum_sync, donation_enabled, donation_subject, sponsors, wiki)
VALUES (2, '2022-07-26 07:35:56.341943 +00:00', 'Test', 'Test', 'paper', 3, null, null, 0, 'Test', 1, '', null, null, null, null, 'Unspecified', null, null, true, false, null, '', null);
