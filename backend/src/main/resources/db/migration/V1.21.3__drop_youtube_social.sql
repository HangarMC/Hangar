UPDATE users SET socials = socials - 'youtube' WHERE jsonb_exists(socials, 'youtube');
