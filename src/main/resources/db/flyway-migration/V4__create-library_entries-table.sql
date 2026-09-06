CREATE TABLE library_entries
(
    id         UUID                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    movie_id   UUID                     NOT NULL,
    user_id    UUID                     NOT NULL,
    watched    BOOLEAN                  NOT NULL DEFAULT FALSE,
    rating     NUMERIC(3,1),
    CONSTRAINT PK_library_entries PRIMARY KEY (id)
);
ALTER TABLE library_entries
    ADD CONSTRAINT UX_library_entries_userId_movieId UNIQUE (user_id, movie_id);

ALTER TABLE library_entries
    ADD CONSTRAINT FK_library_entries_movieId FOREIGN KEY (movie_id) REFERENCES movies (id);

ALTER TABLE library_entries
    ADD CONSTRAINT FK_library_entries_userId FOREIGN KEY (user_id) REFERENCES users (id);
