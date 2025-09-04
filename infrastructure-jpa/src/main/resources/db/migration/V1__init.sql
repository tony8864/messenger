CREATE TABLE chats (
    id UUID PRIMARY KEY
);

CREATE TABLE users (
    id UUID         PRIMARY key,
    username        VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(320) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE messages (
    id          UUID PRIMARY KEY,
    chat_id     UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL,
    content     TEXT NOT NULL,
    status      VARCHAR(50) NOT NULL,
    updated_at  TIMESTAMPTZ
);

CREATE TABLE direct_chats (
    id          UUID PRIMARY KEY REFERENCES chats(id),
    user1_id    UUID NOT NULL REFERENCES users(id),
    user2_id    UUID NOT NULL REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL,
    last_message_id UUID NULL REFERENCES messages(id)
);

CREATE TABLE group_chats (
    id UUID     PRIMARY KEY REFERENCES chats(id),
    group_name  VARCHAR(255) NOT NULL,
    state       VARCHAR(50) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    last_message_id UUID NULL REFERENCES messages(id)
);

CREATE TABLE group_chat_participants (
    chat_id     UUID NOT NULL REFERENCES group_chats(id)    ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id)          ON DELETE CASCADE,
    role        VARCHAR(50) NOT NULL,
    PRIMARY     KEY (chat_id, user_id)
);