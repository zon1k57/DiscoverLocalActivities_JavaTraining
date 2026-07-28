CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Emails should be unique so we don't insert duplicate accounts
ALTER TABLE users
    ADD CONSTRAINT uq_users_email UNIQUE (email);

-- Speeds up lookups/filtering by destination (e.g. "all users headed to Skopje")
CREATE INDEX idx_users_destination ON users (destination);