CREATE TABLE health_reports
(
    id          serial PRIMARY KEY,
    report     jsonb,
    queued_by   varchar(255),
    queued_at   timestamp DEFAULT current_timestamp,
    finished_at timestamp DEFAULT NULL,
    status      varchar(50)
)
