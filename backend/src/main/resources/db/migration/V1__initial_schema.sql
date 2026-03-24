-- Users
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    agb_accepted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

-- Groups
CREATE TABLE groups (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    created_by      BIGINT       NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    invite_token    VARCHAR(255) UNIQUE
);

-- Group Members (User <-> Group mit Rolle + Status)
CREATE TABLE group_members (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id        BIGINT       NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    role            VARCHAR(20)  NOT NULL DEFAULT 'MITGLIED',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    joined_at       TIMESTAMP,
    UNIQUE (user_id, group_id)
);

-- Activities
CREATE TABLE activities (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    location        VARCHAR(255),
    start_time      TIMESTAMP    NOT NULL,
    end_time        TIMESTAMP,
    created_by      BIGINT       NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Group <-> Activity (m:n)
CREATE TABLE group_activities (
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT       NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    activity_id     BIGINT       NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    UNIQUE (group_id, activity_id)
);

-- Activity Participants (User <-> Activity mit Zu-/Absage)
CREATE TABLE activity_participants (
    id              BIGSERIAL PRIMARY KEY,
    activity_id     BIGINT       NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    responded_at    TIMESTAMP,
    UNIQUE (activity_id, user_id)
);

-- Indexes
CREATE INDEX idx_group_members_user_id ON group_members(user_id);
CREATE INDEX idx_group_members_group_id ON group_members(group_id);
CREATE INDEX idx_group_activities_group_id ON group_activities(group_id);
CREATE INDEX idx_group_activities_activity_id ON group_activities(activity_id);
CREATE INDEX idx_activity_participants_activity_id ON activity_participants(activity_id);
CREATE INDEX idx_activity_participants_user_id ON activity_participants(user_id);
CREATE INDEX idx_activities_start_time ON activities(start_time);
