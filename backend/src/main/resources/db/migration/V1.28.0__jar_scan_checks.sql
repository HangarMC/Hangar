ALTER TABLE jar_scan_result_entry
    ADD COLUMN hash       varchar(64),
    ADD COLUMN checked    boolean DEFAULT FALSE,
    ADD COLUMN checked_by bigint  DEFAULT NULL,
    ADD COLUMN checked_at timestamp with time zone;
