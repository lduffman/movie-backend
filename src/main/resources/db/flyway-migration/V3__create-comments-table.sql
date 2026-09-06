CREATE TABLE comments
(
    id         UUID                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    movie_id   UUID                     NOT NULL,
    user_id    UUID                     NOT NULL,
    title      VARCHAR(255)             NOT NULL,
    content    TEXT                     NOT NULL,
    CONSTRAINT PK_comments PRIMARY KEY (id)
);

ALTER TABLE comments
    ADD CONSTRAINT FK_comments_movieId FOREIGN KEY (movie_id) REFERENCES movies (id);

ALTER TABLE comments
    ADD CONSTRAINT FK_comments_userId FOREIGN KEY (user_id) REFERENCES users (id);
