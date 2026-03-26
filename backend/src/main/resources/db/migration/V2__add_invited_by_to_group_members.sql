ALTER TABLE group_members ADD COLUMN invited_by BIGINT REFERENCES users(id);
