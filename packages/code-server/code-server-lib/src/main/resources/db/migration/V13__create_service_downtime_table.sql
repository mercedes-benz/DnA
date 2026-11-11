CREATE TABLE IF NOT EXISTS service_downtime (
    id           TEXT PRIMARY KEY,
    service_name TEXT NOT NULL UNIQUE,
    message      TEXT NOT NULL,
    is_active    BOOLEAN NOT NULL DEFAULT true,
    detected_at  TIMESTAMP NOT NULL DEFAULT now(),
    resolved_at  TIMESTAMP,
    last_checked TIMESTAMP
);
