ALTER TABLE jar_scan_result DROP COLUMN data;
CREATE TABLE jar_scan_result_entry
(
    id        bigserial NOT NULL
        CONSTRAINT jar_scan_result_entry_pkey
            PRIMARY KEY,
    result_id integer   NOT NULL
        CONSTRAINT jar_scan_result_entry_result_id_fk
            REFERENCES jar_scan_result
            ON DELETE CASCADE,
    location  text      NOT NULL,
    message   text      NOT NULL,
    severity  text      NOT NULL
);
