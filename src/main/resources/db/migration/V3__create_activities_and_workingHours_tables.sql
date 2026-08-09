-- V1__create_activity_and_working_hours.sql

CREATE TABLE activity (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    phone_number        VARCHAR(50),
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    rating              DOUBLE PRECISION,
    user_rating_count   INTEGER NOT NULL DEFAULT 0,
    type                VARCHAR(100)
);

CREATE TABLE working_hours (
    id                  BIGSERIAL PRIMARY KEY,
    activity_id         BIGINT NOT NULL REFERENCES activity(id) ON DELETE CASCADE,
    day_of_week         VARCHAR(10) NOT NULL
        CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    open_time           TIME,
    closed_time         TIME,
    break_time_start    TIME,
    break_time_end      TIME,
    is_24h              BOOLEAN NOT NULL DEFAULT FALSE,
    is_closed           BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_working_hours_activity_id ON working_hours(activity_id);
CREATE INDEX idx_activity_type ON activity(type);