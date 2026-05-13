-- Teams table
CREATE TABLE teams (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    invite_code VARCHAR(255) NOT NULL UNIQUE,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(1024),
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    team_id UUID REFERENCES teams(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add FK from teams.created_by -> users.id (after users table exists)
ALTER TABLE teams ADD CONSTRAINT fk_teams_created_by FOREIGN KEY (created_by) REFERENCES users(id);

-- Standups table
CREATE TABLE standups (
    id UUID PRIMARY KEY,
    author_id UUID NOT NULL REFERENCES users(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    yesterday TEXT NOT NULL,
    today TEXT NOT NULL,
    blockers TEXT,
    mood INT NOT NULL CHECK (mood BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common queries
CREATE INDEX idx_standups_team_id ON standups(team_id);
CREATE INDEX idx_standups_author_id ON standups(author_id);
CREATE INDEX idx_standups_created_at ON standups(created_at);
CREATE INDEX idx_standups_team_date ON standups(team_id, created_at);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_team_id ON users(team_id);
CREATE INDEX idx_teams_invite_code ON teams(invite_code);
